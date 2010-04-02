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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
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
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.PathHelper;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.st.AbstractStringTemplateProcessor;
import org.ow2.mind.st.BackendFormatRenderer;

/**
 * {@link IDLVisitor} component that generated {@value #IDT_FILE_EXT} and
 * {@value #ITF_FILE_EXT} files using the {@value #IDL2C_TEMPLATE_NAME}
 * template.
 */

public class IDLHeaderCompiler extends AbstractStringTemplateProcessor
    implements
      IDLVisitor {

  protected static final String IDL2C_TEMPLATE_NAME = "st.interfaces.IDL2C";
  protected final static String IDT_FILE_EXT        = "idt.h";
  protected final static String ITF_FILE_EXT        = "itf.h";

  protected static Logger       depLogger           = FractalADLLogManager
                                                        .getLogger("dep");

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** Client interface used to locate output files. */
  public OutputFileLocator      outputFileLocatorItf;

  /** client interface used to checks timestamps of input resources. */
  public InputResourceLocator   inputResourceLocatorItf;

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  /**
   * Public constructor.
   */
  public IDLHeaderCompiler() {
    super(IDL2C_TEMPLATE_NAME);
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
        throw new CompilerError(IOErrors.WRITE_ERROR, e, headerFile
            .getAbsolutePath());
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

    if (!inputResourceLocatorItf.isUpToDate(outputFile, InputResourcesHelper
        .getInputResources(idl), context)) {
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

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = (OutputFileLocator) value;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = (InputResourceLocator) value;
    } else {
      super.bindFc(itfName, value);
    }
  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), OutputFileLocator.ITF_NAME,
        InputResourceLocator.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      return outputFileLocatorItf;
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

    if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = null;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }

}
