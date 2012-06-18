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
 * Contributors: Julien Tous
 */

package org.ow2.mind.adl.compilation;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.FlagExtractor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.AssemblerCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.preproc.MPPCommand;
import org.ow2.mind.preproc.MPPWrapper;

import com.google.inject.Inject;

public class BasicCompilationCommandFactory
    implements
      CompilationCommandFactory {

  @Inject
  protected CompilerWrapper compilerWrapperItf;

  @Inject
  protected MPPWrapper      mppWrapperItf;

  @Inject
  protected FlagExtractor   flagExtractor;

  public PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final Object source, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = compilerWrapperItf
        .newPreprocessorCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile);

    if (depFile != null) {
      command.setDependencyOutputFile(depFile);
    }

    if (dependencies != null) {
      for (final File dep : dependencies) {
        command.addDependency(dep);
      }
    }

    return command;
  }

  public MPPCommand newMPPCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final File headerOutputFile, final Map<Object, Object> context)
      throws ADLException {
    final MPPCommand command = mppWrapperItf.newMPPCommand(definition, context);
    command.setOutputFile(outputFile).setInputFile(inputFile);
    if (headerOutputFile != null)
      command.setHeaderOutputFile(headerOutputFile);

    if (ASTHelper.isSingleton(definition)) {
      command.setSingletonMode();
    }

    return command;
  }

  public CompilerCommand newCompilerCommand(final Definition definition,
      final Object source, final File inputFile,
      final boolean preprocessedFile, final Collection<File> dependencies,
      final File depFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = compilerWrapperItf
        .newCompilerCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile);

    if (depFile != null) {
      command.setDependencyOutputFile(depFile);
    }

    if (dependencies != null) {
      for (final File dep : dependencies) {
        command.addDependency(dep);
      }
    }

    return command;
  }

  public AssemblerCommand newAssemblerCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final AssemblerCommand command = compilerWrapperItf
        .newAssemblerCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setAllDependenciesManaged(true);

    return command;
  }

  public LinkerCommand newLinkerCommand(final ComponentGraph graph,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final LinkerCommand command = compilerWrapperItf.newLinkerCommand(context);
    command.setOutputFile(outputFile);
    return command;
  }

  public CompilerCommand newFileProviderCompilerCommand(final File outputFile,
      final Map<Object, Object> context) {
    return new FileProviderCompilerCommand(outputFile, context);
  }

  protected static final class FileProviderCompilerCommand
      implements
        CompilerCommand {

    protected final File             outputFile;
    protected final Collection<File> outputFiles;

    protected FileProviderCompilerCommand(final File outputFile,
        final Map<Object, Object> context) {
      this.outputFile = outputFile;
      outputFiles = Arrays.asList(outputFile);
    }

    public String getCommand() {
      throw new UnsupportedOperationException();
    }

    public void setCommand(final String command) {
      throw new UnsupportedOperationException();
    }

    public String getDescription() {
      return "Provides " + outputFile;
    }

    public boolean exec() throws ADLException, InterruptedException {
      // nothing to do
      return true;
    }

    public void prepare() {
      // nothing to do
    }

    public Collection<File> getInputFiles() {
      return Collections.emptyList();
    }

    public Collection<File> getOutputFiles() {
      return outputFiles;
    }

    public boolean forceExec() {
      return false;
    }

    public CompilerCommand addDebugFlag() {
      return this;
    }

    public CompilerCommand addFlag(final String flag) {
      return this;
    }

    public CompilerCommand addFlags(final Collection<String> flags) {
      return this;
    }

    public CompilerCommand addFlags(final String... flags) {
      return this;
    }

    public CompilerCommand addDefine(final String name) {
      return this;
    }

    public CompilerCommand addDefine(final String name, final String value) {
      return this;
    }

    public CompilerCommand addIncludeDir(final File includeDir) {
      return this;
    }

    public CompilerCommand addIncludeFile(final File includeFile) {
      return this;
    }

    public CompilerCommand setOptimizationLevel(final String level) {
      return this;
    }

    public CompilerCommand setOutputFile(final File outputFile) {
      throw new UnsupportedOperationException();
    }

    public CompilerCommand setInputFile(final File inputFile) {
      throw new UnsupportedOperationException();
    }

    public File getOutputFile() {
      return outputFile;
    }

    public File getInputFile() {
      return null;
    }

    public CompilerCommand addDependency(final File dependency) {
      throw new UnsupportedOperationException();
    }

    public CompilerCommand setAllDependenciesManaged(
        final boolean dependencyManaged) {
      throw new UnsupportedOperationException();
    }

    public CompilerCommand setDependencyOutputFile(
        final File dependencyOutputFile) {
      throw new UnsupportedOperationException();
    }
  }
}
