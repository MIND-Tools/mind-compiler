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

import static org.ow2.mind.compilation.CompilerContextHelper.getCompilerCommand;
import static org.ow2.mind.compilation.CompilerContextHelper.getLDFlags;
import static org.ow2.mind.compilation.CompilerContextHelper.getLinkerCommand;
import static org.ow2.mind.compilation.CompilerContextHelper.getLinkerScript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.cecilia.adl.compiler.CompilerErrors;
import org.objectweb.fractal.cecilia.adl.compiler.ExecutionHelper;
import org.ow2.mind.compilation.AbstractCompilerCommand;
import org.ow2.mind.compilation.AbstractLinkerCommand;
import org.ow2.mind.compilation.AbstractPreprocessorCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;

public class GccCompilerWrapper implements CompilerWrapper {

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

  protected static class GccPreprocessorCommand
      extends
        AbstractPreprocessorCommand {

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

    public void exec() throws ADLException, InterruptedException {
      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-E");

      cmd.addAll(CompilerContextHelper.getCFlags(context));
      cmd.addAll(flags);

      cmd.add("-o");
      cmd.add(outputFile.getAbsolutePath());

      cmd.add(inputFile.getAbsolutePath());

      // execute command
      final int rValue = ExecutionHelper.exec(getDescription(), cmd);
      if (rValue != 0) {
        throw new ADLException(CompilerErrors.COMPILER_ERROR, inputFile
            .getPath());
      }
    }

    public String getDescription() {
      return "CPP: " + outputFile.getPath();
    }
  }

  protected static class GccCompilerCommand extends AbstractCompilerCommand {

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

    public void exec() throws ADLException, InterruptedException {

      final List<String> cmd = new ArrayList<String>();
      cmd.add(this.cmd);
      cmd.add("-c");

      cmd.addAll(CompilerContextHelper.getCFlags(context));
      cmd.addAll(flags);

      cmd.add("-o");
      cmd.add(outputFile.getAbsolutePath());

      cmd.add(inputFile.getAbsolutePath());

      // execute command
      final int rValue = ExecutionHelper.exec(getDescription(), cmd);
      if (rValue != 0) {
        throw new ADLException(CompilerErrors.COMPILER_ERROR, inputFile
            .getPath());
      }
    }

    public String getDescription() {
      return "GCC: " + outputFile.getPath();

    }
  }

  protected static class GccLinkerCommand extends AbstractLinkerCommand {

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
      cmd.add(outputFile.getAbsolutePath());

      for (final File inputFile : inputFiles) {
        cmd.add(inputFile.getPath());
      }

      final String linkerScript = getLinkerScript(context);
      if (linkerScript != null) {
        cmd.add("-T");
        cmd.add(linkerScript);
      }

      cmd.addAll(getLDFlags(context));

      cmd.addAll(flags);

      // execute command
      final int rValue = ExecutionHelper.exec(getDescription(), cmd);
      if (rValue != 0) {
        throw new ADLException(CompilerErrors.COMPILER_ERROR, outputFile
            .getPath());
      }
    }

    public String getDescription() {
      return "LD : " + outputFile.getPath();
    }
  }
}
