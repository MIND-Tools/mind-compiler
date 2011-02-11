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

package org.ow2.mind.idl;

import static org.ow2.mind.SourceFileWriter.writeToFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.PathHelper;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.st.AbstractStringTemplateProcessor;
import org.ow2.mind.st.BackendFormatRenderer;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * {@link IDLVisitor} component that generated {@value #IDT_FILE_EXT} and
 * {@value #ITF_FILE_EXT} files using the {@value #DEFAULT_TEMPLATE} template.
 */
public class IDLHeaderCompiler extends AbstractStringTemplateProcessor
    implements
      IDLVisitor {

  /** The name to be used to inject the templateGroupName used by this class. */
  public static final String     TEMPLATE_NAME    = "IDL2C";

  /** The default templateGroupName used by this class. */
  public static final String     DEFAULT_TEMPLATE = "st.interfaces.IDL2C";

  protected final static String  IDT_FILE_EXT     = "idt.h";
  protected final static String  ITF_FILE_EXT     = "itf.h";

  protected static Logger        depLogger        = FractalADLLogManager
                                                      .getLogger("dep");

  @Inject
  protected OutputFileLocator    outputFileLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  @Inject
  protected IDLHeaderCompiler(
      @Named(TEMPLATE_NAME) final String templateGroupName) {
    super(templateGroupName);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final IDL idl, final Map<Object, Object> context)
      throws ADLException {

    final String headerFileName;
    if (idl.getName().startsWith("/")) {
      headerFileName = PathHelper.replaceExtension(idl.getName(), IDT_FILE_EXT);
    } else {
      headerFileName = PathHelper.fullyQualifiedNameToPath(idl.getName(),
          ITF_FILE_EXT);
    }

    final File headerFile = outputFileLocatorItf.getCSourceOutputFile(
        headerFileName, context);
    if (regenerate(headerFile, idl, context)) {
      final StringTemplate st = getInstanceOf("idlFile");

      st.setAttribute("idl", idl);
      try {
        writeToFile(headerFile, st.toString());
      } catch (final IOException e) {
        throw new CompilerError(IOErrors.WRITE_ERROR, e,
            headerFile.getAbsolutePath());
      }
    }
  }

  private boolean regenerate(final File outputFile, final IDL idl,
      final Map<Object, Object> context) {
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

  @Override
  protected void registerCustomRenderer(final StringTemplateGroup templateGroup) {
    templateGroup.registerRenderer(String.class, new BackendFormatRenderer() {
      @Override
      public String toString(final Object o, final String formatName) {
        if ("toIncludePath".equals(formatName)) {
          final String s = o.toString();
          String path = s.substring(1, s.length() - 1);
          if (path.endsWith(".idt")) {
            path += ".h";
          }
          if (path.startsWith("/")) {
            path = path.substring(1);
          }
          return s.substring(0, 1) + path + s.substring(s.length() - 1);
        } else {
          return super.toString(o, formatName);
        }
      }
    });
  }
}
