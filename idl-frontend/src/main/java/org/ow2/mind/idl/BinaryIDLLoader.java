/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.idl;

import static java.lang.System.currentTimeMillis;
import static org.ow2.mind.InputResourcesHelper.getTimestamp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.io.NodeInputStream;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader.AbstractDelegatingIDLLoader;
import org.ow2.mind.idl.ast.IDL;

import com.google.inject.Inject;

public class BinaryIDLLoader extends AbstractDelegatingIDLLoader {

  protected static Logger        logger = FractalADLLogManager
                                            .getLogger("loader.BinaryLoader");

  @Inject
  protected ErrorManager         errorManagerItf;

  @Inject
  protected IDLLocator           idlLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {

    if (ForceRegenContextHelper.getForceRegen(context)) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". Forced mode, load source");
      return loadSourceIDL(name, context);
    }

    final URL binIDL;
    if (name.startsWith("/"))
      binIDL = idlLocatorItf.findBinaryHeader(name, context);
    else
      binIDL = idlLocatorItf.findBinaryItf(name, context);
    if (binIDL == null) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". binary IDL not found, load source");
      return loadSourceIDL(name, context);
    }

    final URL srcIDL;
    if (name.startsWith("/"))
      srcIDL = idlLocatorItf.findSourceHeader(name, context);
    else
      srcIDL = idlLocatorItf.findSourceItf(name, context);
    if (srcIDL == null) {
      // only binary file is available, load from binary file.
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". source unavailable, load binary");
      return loadBinaryIDL(name, binIDL, context);
    }

    // both binary and source file are available, check timestamps:
    boolean outOfDate;
    long binTimestamp = 0;
    try {
      binTimestamp = getTimestamp(binIDL);
      outOfDate = getTimestamp(srcIDL) >= binTimestamp;
    } catch (final MalformedURLException e) {
      if (logger.isLoggable(Level.WARNING))
        logger.log(Level.WARNING, "Load IDL \"" + name
            + "\". can't determine file timestamps");
      outOfDate = true;
    }
    if (!outOfDate) {
      // if binary file is more recent than source file, check dependencies.

      // load binary IDL to retrieve list of input resources.
      final IDL binAST = loadBinaryIDL(name, binIDL, context);

      final Set<InputResource> dependencies = InputResourcesHelper
          .getInputResources(binAST);
      if (logger.isLoggable(Level.FINEST))
        logger.log(Level.FINEST, "Load IDL \"" + name
            + "\". check dependencies=" + dependencies);
      if (dependencies != null
          && inputResourceLocatorItf.isUpToDate(binTimestamp, dependencies,
              context)) {
        if (logger.isLoggable(Level.FINEST))
          logger.log(Level.FINEST, "Load IDL \"" + name
              + "\". Binary version is up-to-date");

        // binary version is up to date, return it
        return binAST;
      }
    }

    if (logger.isLoggable(Level.FINE))
      logger.log(Level.FINE, "Load IDL \"" + name
          + "\". Binary IDL out of date, load source");
    // binary version is older than source file, load from source
    return loadSourceIDL(name, context);

  }

  protected IDL loadSourceIDL(final String name,
      final Map<Object, Object> context) throws ADLException {
    long t = 0;
    if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

    final IDL idl = clientIDLLoaderItf.load(name, context);

    if (logger.isLoggable(Level.FINER)) {
      t = currentTimeMillis() - t;
      logger.log(Level.FINER, "IDL \"" + name + "\" loaded from source in " + t
          + "ms.");
    }
    return idl;
  }

  protected IDL loadBinaryIDL(final String name, final URL location,
      final Map<Object, Object> context) throws ADLException {
    try {
      final InputStream is = location.openStream();
      final NodeInputStream nis = new NodeInputStream(is);
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name + "\". Read IDL from "
            + location);

      long t = 0;
      if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

      final IDL idl = NodeUtil.castNodeError(nis.readObject(), IDL.class);

      if (logger.isLoggable(Level.FINER)) {
        t = currentTimeMillis() - t;
        logger.log(Level.FINER, "Load IDL \"" + name
            + "\".  read from binary file in " + t + "ms.");
      }

      nis.close();

      return idl;
    } catch (final IOException e) {
      errorManagerItf.logFatal(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary IDL " + location);
      return null;
    } catch (final ClassNotFoundException e) {
      errorManagerItf.logFatal(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary IDL " + location);
      return null;
    }
  }
}
