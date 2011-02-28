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

package org.ow2.mind.adl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.io.NodeOutputStream;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.io.IOErrors;

public class BinaryADLWriter extends AbstractSourceGenerator
    implements
      DefinitionSourceGenerator {

  protected static Logger logger = FractalADLLogManager.getLogger("io");

  protected BinaryADLWriter() {
    super(null);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionSourceGenerator interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    if (ForceRegenContextHelper.getNoBinaryAST(context)) {
      if (logger.isLoggable(Level.FINE))
        logger.log(
            Level.FINE,
            "No-binary-AST mode. Do not write binary ADL for "
                + definition.getName());
      return;
    }

    final File outputFile = outputFileLocatorItf.getMetadataOutputFile(
        BasicADLLocator.getADLBinaryName(definition), context);

    if (regenerate(outputFile, definition, context)) {

      NodeOutputStream nos = null;
      try {
        if (logger.isLoggable(Level.FINE))
          logger.log(Level.FINE, "Write binary ADL to " + outputFile);
        nos = new NodeOutputStream(new FileOutputStream(outputFile));
        nos.writeObject(definition);
      } catch (final IOException e) {
        throw new CompilerError(IOErrors.WRITE_ERROR, e,
            "Can't write binary ADL to file " + outputFile);
      } finally {
        if (nos != null)
          try {
            nos.close();
          } catch (final IOException e) {
            if (logger.isLoggable(Level.WARNING))
              logger
                  .warning("Unable to close stream used to write binary ADL \""
                      + outputFile + "\" : " + e.getMessage());
          }
      }
    }
  }

}
