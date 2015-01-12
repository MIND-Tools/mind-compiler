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

public abstract class AbstractPreprocessorCommand
    implements
      PreprocessorCommand {

  protected final Map<Object, Object> context;
  protected String                    cmd;
  protected String                    fullCmd;
  protected final List<String>        flags             = new ArrayList<String>();
  protected final List<String>        defines           = new ArrayList<String>();
  protected final List<File>          includeDir        = new ArrayList<File>();
  protected final List<File>          includeFile       = new ArrayList<File>();
  protected File                      inputFile;
  protected File                      outputFile;
  protected File                      dependencyOutputFile;
  protected Collection<File>          dependencies;
  protected boolean                   dependencyManaged = false;
  protected boolean                   forced            = false;

  private List<File>                  inputFiles;
  private List<File>                  outputFiles;

  protected AbstractPreprocessorCommand(final String cmd,
      final Map<Object, Object> context) {
    this.cmd = cmd;
    this.context = context;
  }

  public String getCommand() {
    return cmd;
  }

  public void setCommand(final String command) {
    this.cmd = command;
  }

  public PreprocessorCommand addFlag(final String flag) {
    if (flag != null) flags.add(flag);
    return this;
  }

  public PreprocessorCommand addFlags(final Collection<String> flags) {
    if (flags != null) this.flags.addAll(flags);
    return this;
  }

  public PreprocessorCommand addFlags(final String... flags) {
    if (flags != null) {
      for (final String flag : flags)
        this.flags.add(flag);
    }
    return this;
  }

  public PreprocessorCommand addDefine(final String name) {
    return addDefine(name, null);
  }

  public PreprocessorCommand addDefine(final String name, final String value) {
    if (value != null)
      defines.add(name + "=" + value);
    else
      defines.add(name);
    return this;
  }

  public PreprocessorCommand addIncludeDir(final File incDir) {
    includeDir.add(incDir);
    return this;
  }

  public PreprocessorCommand addIncludeFile(final File incFile) {
    includeFile.add(incFile);
    return this;
  }

  public PreprocessorCommand setInputFile(final File inFile) {
    inputFile = inFile;
    return this;
  }

  public File getInputFile() {
    return inputFile;
  }

  public PreprocessorCommand setOutputFile(final File outFile) {
    outputFile = outFile;
    return this;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public PreprocessorCommand addDependency(final File dependency) {
    if (dependencies == null) {
      dependencies = new ArrayList<File>();
    }
    dependencies.add(dependency);
    return this;
  }

  public PreprocessorCommand setDependencyOutputFile(
      final File dependencyOutputFile) {
    this.dependencyOutputFile = dependencyOutputFile;
    return this;
  }

  public PreprocessorCommand setAllDependenciesManaged(
      final boolean dependencyManaged) {
    this.dependencyManaged = dependencyManaged;
    return this;
  }

  public Collection<File> getInputFiles() {
    return inputFiles;
  }

  public Collection<File> getOutputFiles() {
    return outputFiles;
  }

  public boolean forceExec() {
    return forced;
  }

  public void prepare() {
    if (dependencyManaged || dependencyOutputFile != null) {
      forced = false;
    } else {
      forced = true;
    }

    inputFiles = new ArrayList<File>();
    inputFiles.add(inputFile);
    if (dependencies != null) {
      inputFiles.addAll(dependencies);
    }
    if (dependencyOutputFile != null) {
      final Collection<File> deps = readDependencies();
      if (deps != null) {
        inputFiles.addAll(deps);
      } else {
        // Can't read dependencies, force execution.
        forced = true;
      }
    }

    if (dependencyOutputFile != null)
      outputFiles = Arrays.asList(outputFile, dependencyOutputFile);
    else
      outputFiles = Arrays.asList(outputFile);
  }

  protected abstract Collection<File> readDependencies();
}
