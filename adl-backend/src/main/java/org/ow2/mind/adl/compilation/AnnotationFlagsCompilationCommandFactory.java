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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.FlagExtractor;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.compilation.CompilationCommandFactory.AbstractDelegatingCompilationCommandFactory;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.AssemblerCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.preproc.MPPCommand;

import com.google.inject.Inject;

public class AnnotationFlagsCompilationCommandFactory
    extends
      AbstractDelegatingCompilationCommandFactory {

  @Inject
  protected FlagExtractor flagExtractor;

  public PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final Object source, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = factoryDelegate.newPreprocessorCommand(
        definition, source, inputFile, dependencies, depFile, outputFile,
        context);

    if (definition != null) {
      // Add definition level C-Flags
      command.addFlags(flagExtractor.getCPPFlags(definition, context));
      command.addFlags(flagExtractor.getCFlags(definition, context));
    }

    if (source instanceof Source) {
      // Add source level C-Flags
      command.addFlags(flagExtractor.getCPPFlags((Source) source, context));
      command.addFlags(flagExtractor.getCFlags((Source) source, context));
    }

    return command;
  }

  public MPPCommand newMPPCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final File headerOutputFile, final Map<Object, Object> context)
      throws ADLException {
    return factoryDelegate.newMPPCommand(definition, source, inputFile,
        outputFile, headerOutputFile, context);
  }

  public CompilerCommand newCompilerCommand(final Definition definition,
      final Object source, final File inputFile,
      final boolean preprocessedFile, final Collection<File> dependencies,
      final File depFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = factoryDelegate.newCompilerCommand(
        definition, source, inputFile, preprocessedFile, dependencies, depFile,
        outputFile, context);

    if (definition != null) {
      // Add definition level C-Flags
      if (!preprocessedFile) {
        command.addFlags(flagExtractor.getCPPFlags(definition, context));
      }
      command.addFlags(flagExtractor.getCFlags(definition, context));
    }

    if (source instanceof Source) {
      // Add source level C-Flags
      if (!preprocessedFile) {
        command.addFlags(flagExtractor.getCPPFlags((Source) source, context));
      }
      command.addFlags(flagExtractor.getCFlags((Source) source, context));
    }

    return command;
  }

  public AssemblerCommand newAssemblerCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final AssemblerCommand command = factoryDelegate.newAssemblerCommand(
        definition, source, inputFile, outputFile, context);

    if (definition != null) {
      // Add definition level As-Flags
      command.addFlags(flagExtractor.getASFlags(definition, context));
    }

    if (source instanceof Source) {
      // Add source level AS-Flags
      command.addFlags(flagExtractor.getASFlags((Source) source, context));
    }

    return command;
  }

  public LinkerCommand newLinkerCommand(final ComponentGraph graph,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final LinkerCommand command = factoryDelegate.newLinkerCommand(graph,
        outputFile, context);

    addLDFlags(graph, new HashSet<Definition>(), command, context);

    return command;
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

  public CompilerCommand newFileProviderCompilerCommand(final File outputFile,
      final Map<Object, Object> context) {
    return factoryDelegate.newFileProviderCompilerCommand(outputFile, context);
  }
}
