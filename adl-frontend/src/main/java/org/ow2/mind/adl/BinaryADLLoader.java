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
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.io.NodeInputStream;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

/**
 * Delegating loader that tries to load ADL definition from binary files (if
 * presents and up to date).
 */
public class BinaryADLLoader extends AbstractDelegatingLoader {

  protected static Logger        logger = FractalADLLogManager
                                            .getLogger("loader.BinaryLoader");

  @Inject
  protected ErrorManager         errorManagerItf;

  @Inject
  protected ADLLocator           adlLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  @Inject
  protected NodeFactory          nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {

    if (ForceRegenContextHelper.getForceRegen(context)) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load ADL \"" + name
            + "\". Forced mode, load source");
      return loadSourceADL(name, context);
    }

    final URL binADL = adlLocatorItf.findBinaryADL(name, context);
    if (binADL == null) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load ADL \"" + name
            + "\". binary ADL not found, load source");
      return loadSourceADL(name, context);
    }

    final URL srcADL = adlLocatorItf.findSourceADL(name, context);
    if (srcADL == null) {
      // only binary file is available, load from binary file.
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load ADL \"" + name
            + "\". source unavailable, load binary");
      return loadBinaryADL(name, binADL, context);
    }

    // both binary and source file are available, check timestamps:
    boolean outOfDate;
    long binTimestamp = 0;
    try {
      binTimestamp = getTimestamp(binADL);
      outOfDate = getTimestamp(srcADL) >= binTimestamp;
    } catch (final MalformedURLException e) {
      if (logger.isLoggable(Level.WARNING))
        logger.log(Level.WARNING, "Load ADL \"" + name
            + "\". can't determine file timestamps");
      outOfDate = true;
    }
    if (outOfDate) {
      logger.log(Level.FINE, "Load ADL \"" + name
          + "\". Binary ADL older from source, load source");

    } else {
      // if binary file is more recent than source file, check dependencies.

      // load binary ADL to retrieve list of input resources.
      final Definition binDef = loadBinaryADL(name, binADL, context);

      final Set<InputResource> dependencies = InputResourcesHelper
          .getInputResources(binDef);
      if (logger.isLoggable(Level.FINEST))
        logger.log(Level.FINEST, "Load ADL \"" + name
            + "\". check dependencies=" + dependencies);
      if (dependencies != null
          && inputResourceLocatorItf.isUpToDate(binTimestamp, dependencies,
              context)) {
        if (logger.isLoggable(Level.FINEST))
          logger.log(Level.FINEST, "Load ADL \"" + name
              + "\". Binary version is up-to-date");

        // binary version is up to date, return it
        return binDef;
      }
    }

    if (logger.isLoggable(Level.FINE))
      logger.log(Level.FINE, "Load ADL \"" + name
          + "\". Binary ADL out of date, load source");
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
      logger.log(Level.FINER, "ADL \"" + name + "\" loaded from source in " + t
          + "ms.");
    }
    return definition;
  }

  protected Definition loadBinaryADL(final String name, final URL location,
      final Map<Object, Object> context) throws ADLException {
    try {
      final InputStream is = location.openStream();
      final NodeInputStream nis = new NodeInputStream(is,
          nodeFactoryItf.getClassLoader());
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load ADL \"" + name + "\". Read ADL from "
            + location);

      long t = 0;
      if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

      final Definition d = NodeUtil.castNodeError(nis.readObject(),
          Definition.class);

      if (logger.isLoggable(Level.FINER)) {
        t = currentTimeMillis() - t;
        logger.log(Level.FINER, "Load ADL \"" + name
            + "\".  read from binary file in " + t + "ms.");
      }

      nis.close();

      return d;
    } catch (final IOException e) {
      errorManagerItf.logFatal(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary ADL " + location);
      return null;
    } catch (final ClassNotFoundException e) {
      errorManagerItf.logFatal(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary ADL " + location);
      return null;
    }
  }
}
