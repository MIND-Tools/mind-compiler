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

package org.ow2.mind.compilation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractCompilerCommand implements CompilerCommand {

  protected final Map<Object, Object> context;
  protected final String              cmd;
  protected final List<String>        flags = new ArrayList<String>();
  protected File                      inputFile;
  protected File                      outputFile;
  protected Collection<File>          dependencies;
  protected String                    optimizationLevel;

  protected AbstractCompilerCommand(final String cmd,
      final Map<Object, Object> context) {
    this.cmd = cmd;
    this.context = context;
  }

  public CompilerCommand addFlag(final String flag) {
    if (flag != null) flags.add(flag);
    return this;
  }

  public CompilerCommand addFlags(final Collection<String> flags) {
    if (flags != null) this.flags.addAll(flags);
    return this;
  }

  public CompilerCommand addFlags(final String... flags) {
    if (flags != null) {
      for (final String flag : flags)
        this.flags.add(flag);
    }
    return this;
  }

  public CompilerCommand addDefine(final String name) {
    return addDefine(name, null);
  }

  public CompilerCommand setInputFile(final File inputFile) {
    this.inputFile = inputFile;
    return this;
  }

  public CompilerCommand setOptimizationLevel(final String level) {
    this.optimizationLevel = level;
    return this;
  }

  public CompilerCommand setOutputFile(final File outputFile) {
    this.outputFile = outputFile;
    return this;
  }

  public CompilerCommand addDependency(final File dependency) {
    if (dependencies == null) {
      dependencies = new ArrayList<File>();
    }
    dependencies.add(dependency);
    return this;
  }

  public Collection<File> getInputFiles() {
    if (dependencies != null) {
      final Collection<File> inputFiles = new ArrayList<File>(dependencies);
      inputFiles.add(inputFile);
      return inputFiles;
    } else {
      return Arrays.asList(inputFile);
    }
  }

  public Collection<File> getOutputFiles() {
    return Arrays.asList(outputFile);
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof AbstractCompilerCommand)) return false;
    final AbstractCompilerCommand cmd = (AbstractCompilerCommand) obj;

    return super.equals(obj);
  }
}
