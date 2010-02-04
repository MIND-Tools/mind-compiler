/**
 * Copyright (C) 2009 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl;

import static java.lang.System.currentTimeMillis;
import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.InputResourcesHelper.getTimestamp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.io.NodeInputStream;
import org.objectweb.fractal.adl.io.NodeOutputStream;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;

/**
 * Delegating loader that tries to load ADL definition from binary files (if
 * presents and up to date).
 */
public class BinaryADLLoader extends AbstractLoader {

  protected static Logger     logger = FractalADLLogManager
                                         .getLogger("loader.BinaryLoader");

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /**
   * The {@link ADLLocator} client interface used to locate binary and source
   * ADL files.
   */
  public ADLLocator           adlLocatorItf;

  /**
   * The {@link InputResourceLocator} client interface used to locate and check
   * timestamps of dependencies of ADL AST.
   */
  public InputResourceLocator inputResourceLocatorItf;

  /**
   * The {@link NodeFactory} used to de-serialize binary ADL.
   * 
   * @see NodeInputStream
   */
  public NodeFactory          nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {

    final URL binADL = adlLocatorItf.findBinaryADL(name, context);
    if (binADL == null) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load \"" + name
            + "\". binary ADL not found, load source");
      return loadSourceADL(name, context);
    }

    final URL srcADL = adlLocatorItf.findSourceADL(name, context);
    if (srcADL == null) {
      // only binary file is available, load from binary file.
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load \"" + name
            + "\". source unavailable, load binary");
      return loadBinaryADL(binADL, context);
    }

    // both binary and source file are available, check timestamps:
    boolean outOfDate;
    long binTimestamp = 0;
    try {
      binTimestamp = getTimestamp(binADL);
      outOfDate = getTimestamp(srcADL) >= binTimestamp;
    } catch (final MalformedURLException e) {
      if (logger.isLoggable(Level.WARNING))
        logger.log(Level.WARNING, "Load \"" + name
            + "\". can't determine file timestamps");
      outOfDate = true;
    }
    if (!outOfDate) {
      // if binary file is more recent than source file, check dependencies.

      // load binary ADL to retrieve list of input resources.
      final Definition binDef = loadBinaryADL(binADL, context);

      final Set<InputResource> dependencies = InputResourcesHelper
          .getInputResources(binDef);
      if (dependencies != null
          && inputResourceLocatorItf.isUpToDate(binTimestamp, dependencies,
              context)) {
        // binary version is up to date, return it
        return binDef;
      }
    }

    if (logger.isLoggable(Level.FINE))
      logger.log(Level.FINE, "Load \"" + name
          + "\". binary ADL out of date, load source");
    // binary version is older than source file, load from source
    return loadSourceADL(name, context);

  }

  protected Definition loadSourceADL(final String name,
      final Map<Object, Object> context) throws ADLException {
    long t = 0;
    if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

    final Definition definition = clientLoader.load(name, context);

    if (logger.isLoggable(Level.FINER)) {
      t = currentTimeMillis() - t;
      logger.log(Level.FINER, "\"" + name + "\" loaded from source in " + t
          + "ms.");
    }
    final File outDir = (File) context.get("binADLdir");
    if (outDir != null) {
      final File binADLFile = new File(outDir, name.replace('.',
          File.separatorChar)
          + BasicADLLocator.BINARY_EXTENSION);
      binADLFile.getParentFile().mkdirs();
      try {
        if (logger.isLoggable(Level.FINE))
          logger.log(Level.FINE, "Write ADL to " + binADLFile.toURI());
        final NodeOutputStream nos = new NodeOutputStream(new FileOutputStream(
            binADLFile));
        nos.writeNode(definition);
        nos.close();
      } catch (final FileNotFoundException e) {
        throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
            "Can't write binary ADL " + name);
      } catch (final IOException e) {
        throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
            "Can't write binary ADL " + name);
      }
    }
    return definition;
  }

  private Definition loadBinaryADL(final URL location,
      final Map<Object, Object> context) throws ADLException {
    try {
      final InputStream is = location.openStream();
      final NodeInputStream nis = new NodeInputStream(is, nodeFactoryItf);
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Read ADL from " + location);

      long t = 0;
      if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

      final Definition d = NodeUtil.castNodeError(nis.readNode(),
          Definition.class);

      if (logger.isLoggable(Level.FINER)) {
        t = currentTimeMillis() - t;
        logger.log(Level.FINER, "\"" + d.getName() + "\" read from file in "
            + t + "ms.");
      }

      nis.close();

      return d;
    } catch (final IOException e) {
      throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary ADL " + location);
    } catch (final ClassNotFoundException e) {
      throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary ADL " + location);
    }
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = (ADLLocator) value;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = (InputResourceLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), ADLLocator.ITF_NAME,
        InputResourceLocator.ITF_NAME, NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      return adlLocatorItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      return inputResourceLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
