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

package org.ow2.mind.compilation.gcc;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.compilation.CompilerContextHelper.getCompilerCommand;
import static org.ow2.mind.compilation.CompilerContextHelper.getLDFlags;
import static org.ow2.mind.compilation.CompilerContextHelper.getLinkerCommand;
import static org.ow2.mind.compilation.CompilerContextHelper.getLinkerScript;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.SourceFileWriter;
import org.ow2.mind.compilation.AbstractCompilerCommand;
import org.ow2.mind.compilation.AbstractLinkerCommand;
import org.ow2.mind.compilation.AbstractPreprocessorCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerErrors;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.DependencyHelper;
import org.ow2.mind.compilation.ExecutionHelper;
import org.ow2.mind.compilation.ExecutionHelper.ExecutionResult;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.io.OutputFileLocator;

public class GccCompilerWrapper implements CompilerWrapper, BindingController {

  private static final String TEMP_DIR  = "$TEMP_DIR";

  protected static Logger     depLogger = FractalADLLogManager.getLogger("dep");
  protected static Logger     ioLogger  = FractalADLLogManager.getLogger("io");

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager         errorManagerItf;

  public OutputFileLocator    outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the CompilerWrapper interface
  // ---------------------------------------------------------------------------

  public PreprocessorCommand newPreprocessorCommand(
      final Map<Object, Object> context) {
    return new GccPreprocessorCommand(context);
  }

  public CompilerCommand newCompilerCommand(final Map<Object, Object> context) {
    return new GccCompilerCommand(context);
  }

  public LinkerCommand newLinkerCommand(final Map<Object, Object> context) {
    return new GccLinkerCommand(context);
  }

  protected class GccPreprocessorCommand extends AbstractPreprocessorCommand {

    protected GccPreprocessorCommand(final Map<Object, Object> context) {
      super(getCompilerCommand(context), context);
    }

    public PreprocessorCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    public PreprocessorCommand addDefine(final String name, final String value) {
      if (value != null)
        flags.add("-D" + name + "=" + value);
      else
        flags.add("-D" + name);
      return this;
    }

    public PreprocessorCommand addIncludeDir(final File includeDir) {
      flags.add("-I" + includeDir.getPath());
      return this;
    }

    public PreprocessorCommand addIncludeFile(final File includeFile) {
      flags.add("-include");
      flags.add(includeFile.getPath());
      return this;
    }

    @Override
    protected Collection<File> readDependencies() {
      return readDeps(dependencyOutputFile, outputFile, context);
    }

    public void exec() throws ADLException, InterruptedException {
      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-E");

      cmd.addAll(CompilerContextHelper.getCFlags(context));
      cmd.addAll(flags);

      if (dependencyOutputFile != null) {
        cmd.add("-MMD");
        cmd.add("-MF");
        cmd.add(dependencyOutputFile.getPath());
        cmd.add("-MT");
        cmd.add(outputFile.getPath());
      }

      cmd.add("-o");
      cmd.add(outputFile.getPath());

      cmd.add(inputFile.getPath());

      // execute command
      final ExecutionResult result = ExecutionHelper
          .exec(getDescription(), cmd);
      if (dependencyOutputFile != null && dependencyOutputFile.exists()) {
        processDependencyOutputFile(dependencyOutputFile, context);
      }

      if (result.getExitValue() != 0) {
        throw new ADLException(CompilerErrors.COMPILER_ERROR,
            outputFile.getPath(), result.getOutput());
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        // TODO find a specific way to print warnings
        ioLogger.warning(result.getOutput());
      }
    }

    public String getDescription() {
      return "CPP: " + outputFile.getPath();
    }
  }

  protected class GccCompilerCommand extends AbstractCompilerCommand {

    protected GccCompilerCommand(final Map<Object, Object> context) {
      super(getCompilerCommand(context), context);
    }

    public CompilerCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    public CompilerCommand addDefine(final String name, final String value) {
      if (value != null)
        flags.add("-D" + name + "=" + value);
      else
        flags.add("-D" + name);
      return this;
    }

    public CompilerCommand addIncludeDir(final File includeDir) {
      flags.add("-I" + includeDir.getPath());
      return this;
    }

    public CompilerCommand addIncludeFile(final File includeFile) {
      flags.add("-include");
      flags.add(includeFile.getPath());
      return this;
    }

    @Override
    protected Collection<File> readDependencies() {
      return readDeps(dependencyOutputFile, outputFile, context);
    }

    public void exec() throws ADLException, InterruptedException {

      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-c");

      cmd.addAll(CompilerContextHelper.getCFlags(context));
      cmd.addAll(flags);

      if (dependencyOutputFile != null) {
        cmd.add("-MMD");
        cmd.add("-MF");
        cmd.add(dependencyOutputFile.getPath());
        cmd.add("-MT");
        cmd.add(outputFile.getPath());
      }

      cmd.add("-o");
      cmd.add(outputFile.getPath());

      cmd.add(inputFile.getPath());

      // execute command
      final ExecutionResult result = ExecutionHelper
          .exec(getDescription(), cmd);
      if (dependencyOutputFile != null && dependencyOutputFile.exists()) {
        processDependencyOutputFile(dependencyOutputFile, context);
      }

      if (result.getExitValue() != 0) {
        throw new ADLException(CompilerErrors.COMPILER_ERROR,
            outputFile.getPath(), result.getOutput());
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        // TODO find a specific way to print warnings
        ioLogger.warning(result.getOutput());
      }
    }

    public String getDescription() {
      return "GCC: " + outputFile.getPath();

    }
  }

  protected class GccLinkerCommand extends AbstractLinkerCommand {

    protected GccLinkerCommand(final Map<Object, Object> context) {
      super(getLinkerCommand(context), context);
    }

    public LinkerCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    public void exec() throws ADLException, InterruptedException {
      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);

      cmd.add("-o");
      cmd.add(outputFile.getPath());

      // archive files (i.e. '.a' files) are added at the end of the command
      // line.
      List<String> archiveFiles = null;
      for (final File inputFile : inputFiles) {
        final String path = inputFile.getPath();
        if (path.endsWith(".a")) {
          if (archiveFiles == null) archiveFiles = new ArrayList<String>();
          archiveFiles.add(path);
        } else {
          cmd.add(path);
        }
      }
      if (archiveFiles != null) {
        for (final String path : archiveFiles) {
          cmd.add(path);
        }
      }

      final String linkerScript = getLinkerScript(context);
      if (linkerScript != null) {
        cmd.add("-T");
        cmd.add(linkerScript);
      }

      cmd.addAll(getLDFlags(context));

      cmd.addAll(flags);

      // execute command
      final ExecutionResult result = ExecutionHelper
          .exec(getDescription(), cmd);
      if (result.getExitValue() != 0) {
        throw new ADLException(CompilerErrors.LINKER_ERROR,
            outputFile.getPath(), result.getOutput());
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        // TODO find a specific way to print warnings
        ioLogger.warning(result.getOutput());
      }
    }

    public String getDescription() {
      return "LD : " + outputFile.getPath();
    }
  }

  protected void processDependencyOutputFile(final File dependencyOutputFile,
      final Map<Object, Object> context) throws ADLException {
    String depFile = "";
    LineNumberReader reader = null;
    String tempDir = outputFileLocatorItf.getCSourceTemporaryOutputDir(context)
        .getPath();
    if (File.separatorChar != '/')
      tempDir = tempDir.replace(File.separatorChar, '/');
    try {
      reader = new LineNumberReader(new FileReader(dependencyOutputFile));
      String line = reader.readLine();
      while (line != null) {
        if (File.separatorChar != '/') line.replace(File.separatorChar, '/');
        line = line.replace(tempDir, TEMP_DIR);
        depFile += line + "\n";
        line = reader.readLine();
      }
      SourceFileWriter.writeToFile(dependencyOutputFile, depFile);
    } catch (final IOException e) {
      if (depLogger.isLoggable(Level.WARNING))
        depLogger.warning("Error while processing dependency file '"
            + dependencyOutputFile + "' : " + e.getMessage());
    } finally {
      if (reader != null) try {
        reader.close();
      } catch (final IOException e) {
        // ignore
      }
    }
  }

  private Collection<File> readDeps(final File dependencyOutputFile,
      final File outputFile, final Map<Object, Object> context) {
    if (!dependencyOutputFile.exists()) {
      if (depLogger.isLoggable(Level.FINE))
        depLogger.fine("Dependency file '" + dependencyOutputFile
            + "' does not exist, force compilation.");
      return null;
    }

    final Map<File, List<File>> depMap = DependencyHelper
        .parseDepFile(dependencyOutputFile);
    if (depMap == null) {
      if (depLogger.isLoggable(Level.FINE))
        depLogger.fine("Error in dependency file of '" + outputFile
            + "', recompile.");
      return null;
    }

    // process depMap to replace $TEMP_DIR occurrences
    final Map<File, List<File>> filteredDepMap;
    final String tempDir = outputFileLocatorItf.getCSourceTemporaryOutputDir(
        context).getPath();
    if (tempDir != null) {
      filteredDepMap = new HashMap<File, List<File>>(depMap.size());
      for (final Map.Entry<File, List<File>> entry : depMap.entrySet()) {
        File key = entry.getKey();
        if (key.getPath().contains(TEMP_DIR)) {
          key = new File(key.getPath().replace(TEMP_DIR, tempDir));
        }
        final List<File> value = new ArrayList<File>(entry.getValue().size());
        for (File dep : entry.getValue()) {
          if (dep.getPath().contains(TEMP_DIR)) {
            dep = new File(dep.getPath().replace(TEMP_DIR, tempDir));
          }
          value.add(dep);
        }
        filteredDepMap.put(key, value);
      }
    } else {
      filteredDepMap = depMap;
    }

    if (filteredDepMap.size() == 1) {
      // Only one rule, assume is it the right one
      return filteredDepMap.values().iterator().next();
    }

    Collection<File> depFiles = filteredDepMap.get(outputFile);
    if (depFiles == null) {
      // try with absolute path
      depFiles = filteredDepMap.get(outputFile.getAbsoluteFile());

      if (depFiles == null) {
        // try with single file name
        depFiles = filteredDepMap.get(new File(outputFile.getName()));

        if (depFiles == null) {
          // if depFiles is null (i.e. the dependencyFile is invalid),
          // recompile.
          if (depLogger.isLoggable(Level.WARNING))
            depLogger.warning("Invalid dependency file '"
                + dependencyOutputFile + "'. Can't find rule for target '"
                + outputFile + "', recompile.");
          return null;
        }
      }
    }

    return depFiles;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = (ErrorManager) value;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = (OutputFileLocator) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME, OutputFileLocator.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      return errorManagerItf;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      return outputFileLocatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = null;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

}
