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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.adl;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.st.AbstractStringTemplateProcessor;

import com.google.inject.Inject;

/**
 * @author ozcane
 */
public abstract class AbstractSourceGenerator
    extends
      AbstractStringTemplateProcessor {

  // The dep logger
  protected static Logger        depLogger = FractalADLLogManager
                                               .getLogger("dep");

  @Inject
  protected OutputFileLocator    outputFileLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  protected AbstractSourceGenerator(final String templateGroupName) {
    super(templateGroupName);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected boolean regenerate(final File outputFile,
      final Definition definition, final Map<Object, Object> context) {
    if (ForceRegenContextHelper.getForceRegen(context)) return true;

    if (!outputFile.exists()) {
      if (depLogger.isLoggable(Level.FINE)) {
        depLogger.fine("Generated source file '" + outputFile
            + "' does not exist, generate.");
      }
      return true;
    }

    if (!inputResourceLocatorItf.isUpToDate(outputFile,
        InputResourcesHelper.getInputResources(definition), context)) {
      if (depLogger.isLoggable(Level.FINE)) {
        depLogger.fine("Generated source file '" + outputFile
            + "' is out-of-date, regenerate.");
      }
      return true;
    } else {
      if (depLogger.isLoggable(Level.FINE)) {
        depLogger.fine("Generated source file '" + outputFile
            + "' is up-to-date, do not regenerate.");
      }
      return false;
    }

  }
}
