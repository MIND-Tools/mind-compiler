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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.io.NodeOutputStream;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;

import com.google.inject.Inject;

public class BinaryIDLWriter implements IDLVisitor {

  protected static Logger        ioLogger  = FractalADLLogManager
                                               .getLogger("io");
  protected static Logger        depLogger = FractalADLLogManager
                                               .getLogger("dep");

  @Inject
  protected OutputFileLocator    outputFileLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    if (ForceRegenContextHelper.getNoBinaryAST(context)) {
      if (ioLogger.isLoggable(Level.FINE))
        ioLogger.log(Level.FINE,
            "No-binary-AST mode. Do not write binary IDL for " + idl.getName());
      return;
    }
    final File outputFile;
    if (idl.getName().startsWith("/"))
      outputFile = outputFileLocatorItf.getMetadataOutputFile(
          BasicIDLLocator.getHeaderBinaryName(idl.getName()), context);
    else
      outputFile = outputFileLocatorItf.getMetadataOutputFile(
          BasicIDLLocator.getItfBinaryName(idl.getName()), context);

    if (regenerate(outputFile, idl, context)) {

      NodeOutputStream nos = null;
      try {
        if (ioLogger.isLoggable(Level.FINE))
          ioLogger.log(Level.FINE, "Write binary IDL to " + outputFile);
        nos = new NodeOutputStream(new FileOutputStream(outputFile));
        nos.writeObject(idl);
      } catch (final IOException e) {
        throw new CompilerError(IOErrors.WRITE_ERROR, e,
            "Can't write binary ADL to file " + outputFile);
      } finally {
        if (nos != null)
          try {
            nos.close();
          } catch (final IOException e) {
            if (ioLogger.isLoggable(Level.WARNING))
              ioLogger
                  .warning("Unable to close stream use to write binary ADL \""
                      + outputFile + "\" : " + e.getMessage());
          }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected boolean regenerate(final File outputFile, final IDL idl,
      final Map<Object, Object> context) {
    if (ForceRegenContextHelper.getForceRegen(context)) return true;

    if (!outputFile.exists()) {
      if (depLogger.isLoggable(Level.FINE)) {
        depLogger.fine("Generated source file '" + outputFile
            + "' does not exist, generate.");
      }
      return true;
    }

    if (!inputResourceLocatorItf.isUpToDate(outputFile,
        InputResourcesHelper.getInputResources(idl), context)) {
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
