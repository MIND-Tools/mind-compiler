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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.implementation.SharedImplementationDecorationHelper;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.inject.InjectDelegate;
import org.ow2.mind.io.OutputFileLocator;

import com.google.inject.Inject;

public class BasicGraphLinker implements GraphCompiler {

  @InjectDelegate
  protected GraphCompiler         clientCompilerItf;

  @Inject
  protected CompilerWrapper       compilerWrapperItf;

  @Inject
  protected OutputFileLocator     outputFileLocatorItf;

  @Inject
  protected ImplementationLocator implementationLocatorItf;

  @Inject
  protected FlagExtractor         flagExtractor;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public Collection<CompilationCommand> visit(final ComponentGraph graph,
      final Map<Object, Object> context) throws ADLException {
    final List<CompilationCommand> result = new ArrayList<CompilationCommand>();
    final Collection<CompilationCommand> compilationTasks = clientCompilerItf
        .visit(graph, context);

    compileSharedImplementation(graph, compilationTasks, context);

    String outputPath = CompilerContextHelper.getExecutableName(context);
    if (outputPath == null) {
      outputPath = fullyQualifiedNameToPath(graph.getDefinition().getName(),
          null);
    }
    final File outputFile = outputFileLocatorItf.getCExecutableOutputFile(
        outputPath, context);

    final LinkerCommand command = compilerWrapperItf.newLinkerCommand(context);
    for (final CompilationCommand compilationCommand : compilationTasks) {
      result.add(compilationCommand);
      if (compilationCommand instanceof CompilerCommand) {
        command.addInputFiles(((CompilerCommand) compilationCommand)
            .getOutputFile());
      }
    }
    command.setOutputFile(outputFile);

    addLDFlags(graph, new HashSet<Definition>(), command, context);

    result.add(command);

    return result;
  }

  protected void compileSharedImplementation(final ComponentGraph graph,
      final Collection<CompilationCommand> result,
      final Map<Object, Object> context) throws ADLException {
    final Set<String> sharedImplementations = new HashSet<String>();

    findSharedImplementations(graph, sharedImplementations);

    for (final String sharedImplementation : sharedImplementations) {
      File sharedImpl;
      final URL input = implementationLocatorItf.findSource(
          sharedImplementation, context);
      if (input == null) {
        // try to find shared implementation using output locator
        sharedImpl = outputFileLocatorItf.getCSourceOutputFile(
            sharedImplementation, context);
        if (sharedImpl == null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Can't find file " + sharedImplementation);
        }
      } else {
        try {
          sharedImpl = new File(input.toURI());
        } catch (final URISyntaxException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Can't find file " + sharedImplementation);
        }
      }

      final File outFile = outputFileLocatorItf.getCCompiledOutputFile(
          replaceExtension(sharedImplementation, ".o"), context);
      final File depFile = outputFileLocatorItf.getCCompiledOutputFile(
          replaceExtension(sharedImplementation, ".d"), context);
      final CompilerCommand command = compilerWrapperItf
          .newCompilerCommand(context);
      command.setInputFile(sharedImpl).setOutputFile(outFile)
          .setDependencyOutputFile(depFile);

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

      result.add(command);
    }
  }

  protected void findSharedImplementations(final ComponentGraph graph,
      final Set<String> sharedImplementations) {
    sharedImplementations.addAll(SharedImplementationDecorationHelper
        .getSharedImplementation(graph.getDefinition()));

    for (final ComponentGraph subComp : graph.getSubComponents()) {
      findSharedImplementations(subComp, sharedImplementations);
    }
  }

  protected void addLDFlags(final ComponentGraph graph,
      final Set<Definition> visitedDefinitions, final LinkerCommand command,
      final Map<Object, Object> context) throws ADLException {
    final Definition def = graph.getDefinition();
    if (visitedDefinitions.add(def)) {
      // get LDFlags annotation at definition level.
      command.addFlags(flagExtractor.getLDFlags(def, context));

      // get LDFlags annotation at source level.
      if (def instanceof ImplementationContainer) {
        for (final Source src : ((ImplementationContainer) def).getSources()) {
          command.addFlags(flagExtractor.getLDFlags(src, context));
        }
      }
    }

    for (final ComponentGraph subComp : graph.getSubComponents()) {
      addLDFlags(subComp, visitedDefinitions, command, context);
    }
  }
}
