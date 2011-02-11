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

package org.ow2.mind.adl;

import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;
import static org.ow2.mind.PathHelper.replaceExtension;
import static org.ow2.mind.adl.CompilationDecorationHelper.getAdditionalCompilationUnit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.SourceFileWriter;
import org.ow2.mind.adl.CompilationDecorationHelper.AdditionalCompilationUnitDecoration;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.preproc.MPPCommand;
import org.ow2.mind.preproc.MPPWrapper;
import org.ow2.mind.st.BackendFormatRenderer;

import com.google.inject.Inject;

public class BasicDefinitionCompiler implements DefinitionCompiler {

  @Inject
  protected DefinitionSourceGenerator definitionSourceGeneratorItf;

  @Inject
  protected OutputFileLocator         outputFileLocatorItf;

  @Inject
  protected ImplementationLocator     implementationLocatorItf;

  @Inject
  protected CompilerWrapper           compilerWrapperItf;

  @Inject
  protected MPPWrapper                mppWrapperItf;

  @Inject
  protected FlagExtractor             flagExtractor;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public Collection<CompilationCommand> visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {

    definitionSourceGeneratorItf.visit(definition, context);

    final Collection<CompilationCommand> result = new ArrayList<CompilationCommand>();
    if (definition instanceof ImplementationContainer)
      visitImplementation(definition, (ImplementationContainer) definition,
          result, context);

    visitAdditionalCompilationUnits(definition,
        getAdditionalCompilationUnit(definition), result, context);
    return result;
  }

  // ---------------------------------------------------------------------------
  // Helper methods
  // ---------------------------------------------------------------------------

  protected void visitImplementation(final Definition definition,
      final ImplementationContainer container,
      final Collection<CompilationCommand> compilationTasks,
      final Map<Object, Object> context) throws ADLException {

    final Source[] sources = container.getSources();
    for (int i = 0; i < sources.length; i++) {
      final Source src = sources[i];

      // check if src path refer to an already compiled file
      if (ASTHelper.isPreCompiled(src)) {
        // src file is already compiled
        final File srcFile;
        final URL srcURL = implementationLocatorItf.findSource(src.getPath(),
            context);
        try {
          srcFile = new File(srcURL.toURI());
        } catch (final URISyntaxException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e);
        }
        compilationTasks.add(newFileProviderCompilerCommand(srcFile, context));

      } else if (ASTHelper.isAssembly(src)) {
        // src file is an assembly file
        final String implSuffix = "_impl" + i;
        final File objectFile = outputFileLocatorItf.getCCompiledOutputFile(
            fullyQualifiedNameToPath(definition.getName(), implSuffix, ".o"),
            context);

        final File srcFile;
        assert src.getPath() != null;
        final URL srcURL = implementationLocatorItf.findSource(src.getPath(),
            context);
        try {
          srcFile = new File(srcURL.toURI());
        } catch (final URISyntaxException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e);
        }

        final CompilerCommand gccCommand = newAssemblyCompilerCommand(
            definition, srcFile, objectFile, context);

        // Add source-level C-Flags
        gccCommand.addFlags(flagExtractor.getCFlags(src, context));

        compilationTasks.add(gccCommand);

      } else {
        // src file is a normal C file to be processed with MPP.
        final String implSuffix = "_impl" + i;
        final File cppFile = outputFileLocatorItf
            .getCSourceTemporaryOutputFile(
                fullyQualifiedNameToPath(definition.getName(), implSuffix, ".i"),
                context);
        final File mppFile = outputFileLocatorItf
            .getCSourceTemporaryOutputFile(
                fullyQualifiedNameToPath(definition.getName(), implSuffix,
                    ".mpp.c"), context);
        final File objectFile = outputFileLocatorItf.getCCompiledOutputFile(
            fullyQualifiedNameToPath(definition.getName(), implSuffix, ".o"),
            context);
        final File depFile = outputFileLocatorItf.getCCompiledOutputFile(
            fullyQualifiedNameToPath(definition.getName(), implSuffix, ".d"),
            context);

        final File headerFile;
        if (sources.length == 1) {
          headerFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
              ImplementationHeaderSourceGenerator
                  .getImplHeaderFileName(definition), context);
        } else {
          headerFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
              ImplementationHeaderSourceGenerator.getImplHeaderFileName(
                  definition, i), context);
        }

        final File srcFile;
        String inlinedCCode = src.getCCode();
        if (inlinedCCode != null) {
          // Implementation code is inlined in the ADL. Dump it in a file.
          srcFile = outputFileLocatorItf.getCSourceOutputFile(
              fullyQualifiedNameToPath(definition.getName(), implSuffix, ".c"),
              context);
          inlinedCCode = BackendFormatRenderer.sourceToLine(src) + "\n"
              + inlinedCCode + "\n";
          try {
            SourceFileWriter.writeToFile(srcFile, inlinedCCode);
          } catch (final IOException e) {
            throw new CompilerError(IOErrors.WRITE_ERROR, e,
                srcFile.getAbsolutePath());
          }
        } else {
          assert src.getPath() != null;
          final URL srcURL = implementationLocatorItf.findSource(src.getPath(),
              context);
          try {
            srcFile = new File(srcURL.toURI());
          } catch (final URISyntaxException e) {
            throw new CompilerError(GenericErrors.INTERNAL_ERROR, e);
          }
        }

        final PreprocessorCommand cppCommand = newPreprocessorCommand(
            definition, srcFile, null, depFile, cppFile, context);
        final MPPCommand mppCommand = newMPPCommand(definition, cppFile,
            mppFile, headerFile, context);
        final CompilerCommand gccCommand = newCompilerCommand(definition,
            mppFile, objectFile, context);

        cppCommand.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
            DefinitionIncSourceGenerator.getIncFileName(definition), context));

        // Add source-level C-Flags
        cppCommand.addFlags(flagExtractor.getCFlags(src, context));
        gccCommand.addFlags(flagExtractor.getCFlags(src, context));

        compilationTasks.add(cppCommand);
        compilationTasks.add(mppCommand);
        compilationTasks.add(gccCommand);
      }
    }
  }

  protected void visitAdditionalCompilationUnits(
      final Definition definition,
      final Collection<AdditionalCompilationUnitDecoration> additionalCompilationUnits,
      final Collection<CompilationCommand> compilationTasks,
      final Map<Object, Object> context) throws ADLException {

    for (final AdditionalCompilationUnitDecoration additionalCompilationUnit : additionalCompilationUnits) {

      final String path = additionalCompilationUnit.getPath();
      final File cppFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
          replaceExtension(path, ".i"), context);
      final File mppFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
          replaceExtension(path, ".mpp.c"), context);
      final File objectFile = outputFileLocatorItf.getCCompiledOutputFile(
          replaceExtension(path, ".o"), context);
      final File depFile = outputFileLocatorItf.getCCompiledOutputFile(
          replaceExtension(path, ".d"), context);

      final File srcFile;

      if (additionalCompilationUnit.isGeneratedFile()) {
        srcFile = outputFileLocatorItf.getCSourceOutputFile(
            additionalCompilationUnit.getPath(), context);
        if (!srcFile.exists()) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Can't find source file \"" + additionalCompilationUnit + "\"");
        }
      } else {
        final URL srcURL = implementationLocatorItf.findSource(
            additionalCompilationUnit.getPath(), context);
        if (srcURL != null) {
          try {
            srcFile = new File(srcURL.toURI());
          } catch (final URISyntaxException e) {
            throw new CompilerError(GenericErrors.INTERNAL_ERROR, e);
          }
        } else {
          throw new ADLException(GenericErrors.INTERNAL_ERROR,
              "Can't find source file \"" + additionalCompilationUnit + "\"");
        }
      }

      if (additionalCompilationUnit.skipMPP()) {
        final PreprocessorCommand cppCommand = newPreprocessorCommand(
            definition, srcFile, additionalCompilationUnit.getDependencies(),
            depFile, cppFile, context);
        final CompilerCommand gccCommand = newCompilerCommand(definition,
            cppFile, objectFile, context);

        compilationTasks.add(cppCommand);
        compilationTasks.add(gccCommand);
      } else {
        final PreprocessorCommand cppCommand = newPreprocessorCommand(
            definition, srcFile, additionalCompilationUnit.getDependencies(),
            depFile, cppFile, context);
        final MPPCommand mppCommand = newMPPCommand(definition, cppFile,
            mppFile, null, context);
        final CompilerCommand gccCommand = newCompilerCommand(definition,
            mppFile, objectFile, context);

        compilationTasks.add(cppCommand);
        compilationTasks.add(mppCommand);
        compilationTasks.add(gccCommand);
      }
    }
  }

  protected PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = compilerWrapperItf
        .newPreprocessorCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setDependencyOutputFile(depFile);

    if (dependencies != null) {
      for (final File dep : dependencies) {
        command.addDependency(dep);
      }
    }

    command.addIncludeDir(outputFileLocatorItf.getCSourceOutputDir(context));
    command.addIncludeDir(outputFileLocatorItf
        .getCSourceTemporaryOutputDir(context));

    final URL[] inputResourceRoots = implementationLocatorItf
        .getInputResourcesRoot(context);
    if (inputResourceRoots != null) {
      for (final URL inputResourceRoot : inputResourceRoots) {
        try {
          final File inputDir = new File(inputResourceRoot.toURI());
          if (inputDir.isDirectory()) {
            command.addIncludeDir(inputDir);
          }
        } catch (final URISyntaxException e) {
          continue;
        }
      }
    }

    // Add definition level C-Flags
    command.addFlags(flagExtractor.getCFlags(definition, context));

    return command;
  }

  protected MPPCommand newMPPCommand(final Definition definition,
      final File inputFile, final File outputFile, final File headerOutputFile,
      final Map<Object, Object> context) throws ADLException {
    final MPPCommand command = mppWrapperItf.newMPPCommand(definition, context);
    command.setOutputFile(outputFile).setInputFile(inputFile);
    if (headerOutputFile != null)
      command.setHeaderOutputFile(headerOutputFile);

    if (ASTHelper.isSingleton(definition)) {
      command.setSingletonMode();
    }

    return command;
  }

  protected CompilerCommand newCompilerCommand(final Definition definition,
      final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = compilerWrapperItf
        .newCompilerCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setAllDependenciesManaged(true);

    command.addIncludeDir(outputFileLocatorItf.getCSourceOutputDir(context));

    command.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
        fullyQualifiedNameToPath(definition.getName(),
            DefinitionMacroSourceGenerator.FILE_EXT), context));

    // Add definition level C-Flags
    command.addFlags(flagExtractor.getCFlags(definition, context));

    return command;
  }

  protected CompilerCommand newAssemblyCompilerCommand(
      final Definition definition, final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = compilerWrapperItf
        .newCompilerCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setAllDependenciesManaged(true);

    command.addIncludeDir(outputFileLocatorItf.getCSourceOutputDir(context));

    final URL[] inputResourceRoots = implementationLocatorItf
        .getInputResourcesRoot(context);
    if (inputResourceRoots != null) {
      for (final URL inputResourceRoot : inputResourceRoots) {
        try {
          final File inputDir = new File(inputResourceRoot.toURI());
          if (inputDir.isDirectory()) {
            command.addIncludeDir(inputDir);
          }
        } catch (final URISyntaxException e) {
          continue;
        }
      }
    }

    // Add definition level C-Flags
    command.addFlags(flagExtractor.getCFlags(definition, context));

    return command;
  }

  protected CompilerCommand newFileProviderCompilerCommand(
      final File outputFile, final Map<Object, Object> context) {
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
