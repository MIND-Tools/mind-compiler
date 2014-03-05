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

package org.ow2.mind.adl;

import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;
import static org.ow2.mind.PathHelper.replaceExtension;
import static org.ow2.mind.adl.CompilationDecorationHelper.getAdditionalCompilationUnit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
import org.ow2.mind.adl.compilation.CompilationCommandFactory;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.compilation.AssemblerCommand;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.preproc.MPPCommand;
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
  protected CompilationCommandFactory compilationCommandFactory;

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

    // we don't use the KeepSrcNameContextHelper here since it would need to
    // add a adl-backend -> mindc modules dependency, which is wrong.
    final Boolean keepSrcNameInCtx = (Boolean) context.get("keep-src-name");
    final Boolean keepSrcName = (keepSrcNameInCtx != null)
        ? keepSrcNameInCtx
        : false;

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
        compilationTasks.add(compilationCommandFactory
            .newFileProviderCompilerCommand(srcFile, context));

      } else if (ASTHelper.isAssembly(src)) {
        // src file is an assembly file

        // default naming convention
        String implSuffix = "_impl" + i;

        final File srcFile;
        assert src.getPath() != null;
        final URL srcURL = implementationLocatorItf.findSource(src.getPath(),
            context);
        try {
          srcFile = new File(srcURL.toURI());
        } catch (final URISyntaxException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e);
        }

        if (keepSrcName) {
          // keep-source-name convention: override suffix
          final String srcName = srcFile.getName();

          // replace all path separators by underscores and remove extension
          implSuffix = "_"
              + srcName.replace(File.separatorChar, '_').substring(0,
                  srcFile.getName().lastIndexOf('.'));
        }

        final File objectFile = outputFileLocatorItf.getCCompiledOutputFile(
            fullyQualifiedNameToPath(definition.getName(), implSuffix, ".o"),
            context);

        final AssemblerCommand gccCommand = compilationCommandFactory
            .newAssemblerCommand(definition, src, srcFile, objectFile, context);

        compilationTasks.add(gccCommand);

      } else {
        // src file is a normal C file to be processed with MPP.

        // default naming convention, useful to keep for inlined C code
        String implSuffix = "_impl" + i;

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

          if (keepSrcName) {
            // keep-source-name convention: override suffix
            final String srcName = srcFile.getName();

            // replace all path separators by underscores and remove extension
            implSuffix = "_"
                + srcName.replace(File.separatorChar, '_').substring(0,
                    srcFile.getName().lastIndexOf('.'));
          }
        }

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

        final PreprocessorCommand cppCommand = compilationCommandFactory
            .newPreprocessorCommand(definition, src, srcFile, null, depFile,
                cppFile, context);
        final MPPCommand mppCommand = compilationCommandFactory.newMPPCommand(
            definition, src, cppFile, mppFile, headerFile, context);
        final CompilerCommand gccCommand = compilationCommandFactory
            .newCompilerCommand(definition, src, mppFile, true, null, null,
                objectFile, context);

        cppCommand.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
            DefinitionIncSourceGenerator.getIncFileName(definition), context));

        gccCommand.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
            DefinitionMacroSourceGenerator.getMacroFileName(definition),
            context));
        gccCommand.setAllDependenciesManaged(true);

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
        final PreprocessorCommand cppCommand = compilationCommandFactory
            .newPreprocessorCommand(definition, additionalCompilationUnit,
                srcFile, additionalCompilationUnit.getDependencies(), depFile,
                cppFile, context);
        final CompilerCommand gccCommand = compilationCommandFactory
            .newCompilerCommand(definition, additionalCompilationUnit, cppFile,
                true, null, null, objectFile, context);

        gccCommand.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
            DefinitionMacroSourceGenerator.getMacroFileName(definition),
            context));
        gccCommand.setAllDependenciesManaged(true);

        compilationTasks.add(cppCommand);
        compilationTasks.add(gccCommand);
      } else {
        final PreprocessorCommand cppCommand = compilationCommandFactory
            .newPreprocessorCommand(definition, additionalCompilationUnit,
                srcFile, additionalCompilationUnit.getDependencies(), depFile,
                cppFile, context);
        final MPPCommand mppCommand = compilationCommandFactory.newMPPCommand(
            definition, additionalCompilationUnit, cppFile, mppFile, null,
            context);
        final CompilerCommand gccCommand = compilationCommandFactory
            .newCompilerCommand(definition, additionalCompilationUnit, mppFile,
                true, null, null, objectFile, context);

        gccCommand.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
            DefinitionMacroSourceGenerator.getMacroFileName(definition),
            context));
        gccCommand.setAllDependenciesManaged(true);

        compilationTasks.add(cppCommand);
        compilationTasks.add(mppCommand);
        compilationTasks.add(gccCommand);
      }
    }
  }
}
