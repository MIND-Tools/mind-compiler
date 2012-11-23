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
 * Contributors: Julien Tous
 */

package org.ow2.mind.compilation.gcc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.compilation.AbstractAssemblerCommand;
import org.ow2.mind.compilation.AbstractCompilerCommand;
import org.ow2.mind.compilation.AbstractLinkerCommand;
import org.ow2.mind.compilation.AbstractPreprocessorCommand;
import org.ow2.mind.compilation.AssemblerCommand;
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

import com.google.inject.Inject;

public class GccCompilerWrapper implements CompilerWrapper {

  private static final String TEMP_DIR  = "$TEMP_DIR";

  protected static Logger     depLogger = FractalADLLogManager.getLogger("dep");
  protected static Logger     ioLogger  = FractalADLLogManager.getLogger("io");

  @Inject
  protected ErrorManager      errorManagerItf;

  @Inject
  protected OutputFileLocator outputFileLocatorItf;

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

  public AssemblerCommand newAssemblerCommand(final Map<Object, Object> context) {
    return new GccAssemblerCommand(context);
  }

  public LinkerCommand newLinkerCommand(final Map<Object, Object> context) {
    return new GccLinkerCommand(context);
  }

  protected class GccPreprocessorCommand extends AbstractPreprocessorCommand {

    protected GccPreprocessorCommand(final Map<Object, Object> context) {
      super(CompilerContextHelper.getCompilerCommand(context), context);
    }

    public PreprocessorCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    @Override
    protected Collection<File> readDependencies() {
      return readDeps(dependencyOutputFile, outputFile, context);
    }

    public boolean exec() throws ADLException, InterruptedException {
      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-E");

      cmd.addAll(flags);

      for (final String def : defines) {
        cmd.add("-D" + def);
      }

      for (final File incDir : includeDir) {
        cmd.add("-I" + incDir.getPath().trim());
      }

      for (final File incFile : includeFile) {
        cmd.add("-include");
        cmd.add(incFile.getPath());
      }

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
      ExecutionResult result;
      try {
        result = ExecutionHelper.exec(getDescription(), cmd);
      } catch (final IOException e) {
        errorManagerItf.logError(CompilerErrors.EXECUTION_ERROR, this.cmd);
        return false;
      }
      if (dependencyOutputFile != null && dependencyOutputFile.exists()) {
        processDependencyOutputFile(dependencyOutputFile, context);
      }

      if (result.getExitValue() != 0) {
        errorManagerItf.logError(CompilerErrors.COMPILER_ERROR,
            outputFile.getPath(), result.getOutput());
        return false;
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        errorManagerItf.logWarning(CompilerErrors.COMPILER_WARNING,
            outputFile.getPath(), result.getOutput());
      }
      return true;
    }

    public String getDescription() {
      return "CPP: " + outputFile.getPath();
    }
  }

  protected class GccCompilerCommand extends AbstractCompilerCommand {

    protected GccCompilerCommand(final Map<Object, Object> context) {
      super(CompilerContextHelper.getCompilerCommand(context), context);
    }

    public CompilerCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    @Override
    protected Collection<File> readDependencies() {
      return readDeps(dependencyOutputFile, outputFile, context);
    }

    public boolean exec() throws ADLException, InterruptedException {

      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-c");

      cmd.addAll(flags);

      for (final String def : defines) {
        cmd.add("-D" + def);
      }
      for (final File incDir : includeDir) {
        cmd.add("-I" + incDir.getPath().trim());
      }

      for (final File incFile : includeFile) {
        cmd.add("-include");
        cmd.add(incFile.getPath());
      }

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
      ExecutionResult result;
      try {
        result = ExecutionHelper.exec(getDescription(), cmd);
      } catch (final IOException e) {
        errorManagerItf.logError(CompilerErrors.EXECUTION_ERROR, this.cmd);
        return false;
      }
      if (dependencyOutputFile != null && dependencyOutputFile.exists()) {
        processDependencyOutputFile(dependencyOutputFile, context);
      }

      if (result.getExitValue() != 0) {
        errorManagerItf.logError(CompilerErrors.COMPILER_ERROR,
            outputFile.getPath(), result.getOutput());
        return false;
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        errorManagerItf.logWarning(CompilerErrors.COMPILER_WARNING,
            outputFile.getPath(), result.getOutput());
      }
      return true;
    }

    public String getDescription() {
      return "GCC: " + outputFile.getPath();

    }
  }

  protected class GccAssemblerCommand extends AbstractAssemblerCommand {

    protected GccAssemblerCommand(final Map<Object, Object> context) {
      super(CompilerContextHelper.getAssemblerCommand(context), context);
    }

    public AssemblerCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    @Override
    protected Collection<File> readDependencies() {
      return readDeps(dependencyOutputFile, outputFile, context);
    }

    public boolean exec() throws ADLException, InterruptedException {

      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-c");

      cmd.addAll(flags);

      for (final String def : defines) {
        cmd.add("-D" + def);
      }
      for (final File incDir : includeDir) {
        cmd.add("-I" + incDir.getPath().trim());
      }

      for (final File incFile : includeFile) {
        cmd.add("-include");
        cmd.add(incFile.getPath());
      }
      cmd.add("-o");
      cmd.add(outputFile.getPath());

      cmd.add(inputFile.getPath());

      // execute command
      ExecutionResult result;
      try {
        result = ExecutionHelper.exec(getDescription(), cmd);
      } catch (final IOException e) {
        errorManagerItf.logError(CompilerErrors.EXECUTION_ERROR, this.cmd);
        return false;
      }

      if (result.getExitValue() != 0) {
        errorManagerItf.logError(CompilerErrors.COMPILER_ERROR,
            outputFile.getPath(), result.getOutput());
        return false;
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        errorManagerItf.logWarning(CompilerErrors.COMPILER_WARNING,
            outputFile.getPath(), result.getOutput());
      }
      return true;
    }

    public String getDescription() {
      return "AS: " + outputFile.getPath();

    }
  }

  protected class GccLinkerCommand extends AbstractLinkerCommand {

    protected GccLinkerCommand(final Map<Object, Object> context) {
      super(CompilerContextHelper.getLinkerCommand(context), context);
    }

    public LinkerCommand addDebugFlag() {
      flags.add("-g");
      return this;
    }

    public boolean exec() throws ADLException, InterruptedException {
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

      if (linkerScript != null) {
        cmd.add("-T");
        cmd.add(linkerScript);
      }

      cmd.addAll(flags);

      // execute command
      ExecutionResult result;
      try {
        result = ExecutionHelper.exec(getDescription(), cmd);
      } catch (final IOException e) {
        errorManagerItf.logError(CompilerErrors.EXECUTION_ERROR, this.cmd);
        return false;
      }

      if (result.getExitValue() != 0) {
        errorManagerItf.logError(CompilerErrors.LINKER_ERROR,
            outputFile.getPath(), result.getOutput());
        return false;
      }
      if (result.getOutput() != null) {
        // command returns 0 and generates an output (warning)
        errorManagerItf.logWarning(CompilerErrors.LINKER_WARNING,
            outputFile.getPath(), result.getOutput());
      }
      return true;
    }

    public String getDescription() {
      return "LD : " + outputFile.getPath();
    }
  }

  protected void processDependencyOutputFile(final File dependencyOutputFile,
      final Map<Object, Object> context) throws ADLException {
    try {
      final String tempDir = outputFileLocatorItf.getCSourceTemporaryOutputDir(
          context).getCanonicalPath();

      final Map<File, List<File>> deps = DependencyHelper
          .parseDepFile(dependencyOutputFile);
      final Map<File, List<File>> newDeps = new HashMap<File, List<File>>(
          deps.size());
      for (final Map.Entry<File, List<File>> dep : deps.entrySet()) {
        final File target = new File(dep.getKey().getCanonicalPath()
            .replace(tempDir, TEMP_DIR));
        final List<File> depFiles = new ArrayList<File>(dep.getValue().size());
        for (final File depFile : dep.getValue()) {
          depFiles.add(new File(depFile.getCanonicalPath().replace(tempDir,
              TEMP_DIR)));
        }
        newDeps.put(target, depFiles);
      }
      DependencyHelper.writeDepFile(dependencyOutputFile, newDeps);
    } catch (final IOException ioe) {
      if (depLogger.isLoggable(Level.WARNING))
        depLogger
            .warning("Error while processing dependency file '"
                + dependencyOutputFile
                + "' remove it to force future compilation.");
      if (depLogger.isLoggable(Level.FINE))
        depLogger.log(Level.FINE, "Error while processing dependency file '"
            + dependencyOutputFile + ":", ioe);
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
}
