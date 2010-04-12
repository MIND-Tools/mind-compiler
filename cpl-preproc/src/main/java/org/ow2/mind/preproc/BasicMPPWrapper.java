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
 * Contributors: Matthieu ANNE
 */

package org.ow2.mind.preproc;

import static org.ow2.mind.preproc.InvocationHelper.invokeMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.plugin.PluginManager;

public class BasicMPPWrapper implements MPPWrapper {

  protected static Logger     logger             = FractalADLLogManager
                                                     .getLogger("io");

  /** Plugin manager client interface name **/
  public static final String  PLUGIN_MANAGER_ITF = "plugin-manager";

  /** Plugin manager client interface **/
  public static PluginManager pluginManagerItf;

  public MPPCommand newMPPCommand(final Definition definition,
      final Map<Object, Object> context) {
    return new BasicMPPCommand(definition, context);
  }

  protected static class BasicMPPCommand implements MPPCommand {

    protected final Map<Object, Object> context;
    protected CPLChecker                cplChecker;
    protected boolean                   singletonMode = false;
    protected File                      inputFile;
    protected File                      outputFile;
    protected File                      headerOutputFile;

    private List<File>                  inputFiles;
    private List<File>                  outputFiles;

    BasicMPPCommand(final Definition definition,
        final Map<Object, Object> context) {
      this.cplChecker = new CPLChecker(definition);
      this.context = context;
    }

    public MPPCommand setSingletonMode() {
      singletonMode = true;
      return this;
    }

    public MPPCommand unsetSingletonMode() {
      singletonMode = false;
      return this;
    }

    public MPPCommand setInputFile(final File inputFile) {
      this.inputFile = inputFile;
      return this;
    }

    public MPPCommand setOutputFile(final File outputFile) {
      this.outputFile = outputFile;
      return this;
    }

    public MPPCommand setHeaderOutputFile(final File headerOutputFile) {
      this.headerOutputFile = headerOutputFile;
      return this;
    }

    public Collection<File> getInputFiles() {
      return inputFiles;
    }

    public Collection<File> getOutputFiles() {
      return outputFiles;
    }

    public boolean forceExec() {
      return false;
    }

    public void prepare() {
      inputFiles = Arrays.asList(inputFile);
      if (headerOutputFile != null) {
        outputFiles = Arrays.asList(outputFile, headerOutputFile);
      } else {
        outputFiles = Arrays.asList(outputFile);
      }
    }

    public void exec() throws ADLException, InterruptedException {
      final Lexer lex;
      try {
        lex = ExtensionHelper.getLexer(pluginManagerItf, inputFile.getPath(),
            context);
      } catch (final IOException e) {
        // TODO use a specific error
        throw new ADLException(GenericErrors.GENERIC_ERROR, e,
            "Can't open file \"" + inputFile.getPath() + "\".");
      }

      final CommonTokenStream tokens = new CommonTokenStream(lex);
      final Parser mpp = ExtensionHelper.getParser(pluginManagerItf, tokens,
          context);

      invokeMethod(mpp, "setCplChecker", new Class[]{CPLChecker.class},
          new Object[]{this.cplChecker});

      PrintStream outPS = null;
      PrintStream headerOutPS = null;
      try {
        try {
          outputFile.getParentFile().mkdirs();
          outPS = new PrintStream(new FileOutputStream(outputFile));
        } catch (final FileNotFoundException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e, "IO error");
        }
        invokeMethod(lex, "setOutPutStream", new Class[]{PrintStream.class},
            new Object[]{outPS});
        invokeMethod(mpp, "setOutputStream", new Class[]{PrintStream.class},
            new Object[]{outPS});

        if (headerOutputFile != null) {
          try {
            headerOutputFile.getParentFile().mkdirs();
            headerOutPS = new PrintStream(
                new FileOutputStream(headerOutputFile));
          } catch (final FileNotFoundException e) {
            throw new CompilerError(GenericErrors.INTERNAL_ERROR, e, "IO error");
          }
          invokeMethod(mpp, "setHeaderOutputStream",
              new Class[]{PrintStream.class}, new Object[]{headerOutPS});
        }

        invokeMethod(mpp, "setSingletonMode", new Class[]{boolean.class},
            new Object[]{singletonMode});

        if (logger.isLoggable(Level.INFO)) logger.info(getDescription());

        if (logger.isLoggable(Level.FINE))
          logger.fine("MPP: inputFile=" + inputFile.getPath() + " outputFile="
              + outputFile.getPath() + " singletonMode=" + singletonMode);

        try {
          invokeMethod(mpp, "parseFile", new Class[]{}, new Object[]{},
              RecognitionException.class);
        } catch (final RecognitionException e) {
          throw new ADLException(MPPErrors.PARSE_ERROR, e, inputFile.getPath(),
              "MPP parse error.");
        }

        final List<String> errors = (List<String>) invokeMethod(mpp,
            "getErrors", new Class[]{}, new Object[]{});
        if (errors != null && errors.size() > 0) {
          String errorMsg;
          if (errors.size() == 1) {
            errorMsg = errors.get(0);
          } else {
            final StringBuilder msg = new StringBuilder();
            for (final String error : errors) {
              msg.append("\n    ").append(error);
            }
            errorMsg = msg.toString();
          }

          throw new ADLException(MPPErrors.PARSE_ERROR, inputFile.getPath(),
              errorMsg.toString());
        }
      } finally {
        if (outPS != null) outPS.close();
        if (headerOutPS != null) headerOutPS.close();
      }
    }

    public String getDescription() {
      return "MPP: " + outputFile.getPath();
    }
  }
}
