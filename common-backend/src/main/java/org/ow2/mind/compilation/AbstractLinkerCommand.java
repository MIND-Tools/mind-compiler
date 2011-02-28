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

public abstract class AbstractLinkerCommand implements LinkerCommand {

  protected final Map<Object, Object> context;
  protected String                    cmd;
  protected final List<String>        flags      = new ArrayList<String>();
  protected final List<File>          inputFiles = new ArrayList<File>();
  protected final List<String>        libs       = new ArrayList<String>();
  protected File                      outputFile;
  protected String                    optimizationLevel;
  protected String                    linkerScript;
  protected boolean                   forced;

  private List<File>                  outputFiles;

  protected AbstractLinkerCommand(final String cmd,
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

  public LinkerCommand addFlag(final String flag) {
    if (flag != null) flags.add(flag);
    return this;
  }

  public LinkerCommand addFlags(final Collection<String> flags) {
    if (flags != null) this.flags.addAll(flags);
    return this;
  }

  public LinkerCommand addFlags(final String... flags) {
    if (flags != null) {
      for (final String flag : flags)
        this.flags.add(flag);
    }
    return this;
  }

  public LinkerCommand addInputFile(final File inputFile) {
    inputFiles.add(inputFile);
    return this;
  }

  public LinkerCommand addInputFiles(final Collection<File> inputFiles) {
    if (inputFiles != null) this.inputFiles.addAll(inputFiles);
    return this;
  }

  public LinkerCommand addInputFiles(final File... inputFiles) {
    if (inputFiles != null) {
      for (final File inputFile : inputFiles) {
        this.inputFiles.add(inputFile);
      }
    }
    return this;
  }

  public LinkerCommand addLib(final String libName) {
    libs.add(libName);
    return this;
  }

  public LinkerCommand setLinkerScript(final String linkerScript) {
    this.linkerScript = linkerScript;
    return this;
  }

  public LinkerCommand setOptimizationLevel(final String level) {
    this.optimizationLevel = level;
    return this;
  }

  public LinkerCommand setOutputFile(final File outputFile) {
    this.outputFile = outputFile;
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
    outputFiles = Arrays.asList(outputFile);
  }

}
