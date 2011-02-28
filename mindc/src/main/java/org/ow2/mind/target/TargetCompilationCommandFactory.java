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
 * Contributors: 
 */

package org.ow2.mind.target;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.compilation.CompilationCommandFactory.AbstractDelegatingCompilationCommandFactory;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.DirectiveHelper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.preproc.MPPCommand;
import org.ow2.mind.target.ast.Flag;
import org.ow2.mind.target.ast.Target;

import com.google.inject.Inject;

public class TargetCompilationCommandFactory
    extends
      AbstractDelegatingCompilationCommandFactory {

  @Inject
  protected TargetDescriptorLoader targetDescriptorLoader;

  @Inject
  protected ErrorManager           errorManager;

  public PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final Object source, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = factoryDelegate.newPreprocessorCommand(
        definition, source, inputFile, dependencies, depFile, outputFile,
        context);

    processPreprocCommand(
        command,
        loadTarget(TargetDescriptorOptionHandler.getTargetDescriptor(context),
            context), context);

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

    processCompilerCommand(
        command,
        preprocessedFile,
        loadTarget(TargetDescriptorOptionHandler.getTargetDescriptor(context),
            context), context);

    return command;
  }

  public CompilerCommand newAssemblyCompilerCommand(
      final Definition definition, final Object source, final File inputFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final CompilerCommand command = factoryDelegate.newAssemblyCompilerCommand(
        definition, source, inputFile, outputFile, context);

    processCompilerCommand(
        command,
        false,
        loadTarget(TargetDescriptorOptionHandler.getTargetDescriptor(context),
            context), context);

    return command;
  }

  public LinkerCommand newLinkerCommand(final ComponentGraph graph,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final LinkerCommand command = factoryDelegate.newLinkerCommand(graph,
        outputFile, context);

    processLinkerCommand(
        command,
        loadTarget(TargetDescriptorOptionHandler.getTargetDescriptor(context),
            context), context);

    return command;
  }

  public CompilerCommand newFileProviderCompilerCommand(final File outputFile,
      final Map<Object, Object> context) {
    return factoryDelegate.newFileProviderCompilerCommand(outputFile, context);
  }

  protected void processPreprocCommand(final PreprocessorCommand command,
      final Target target, final Map<Object, Object> context)
      throws ADLException {
    if (target != null) {
      if (target.getCompiler() != null) {
        command.setCommand(target.getCompiler().getPath());
      }
      if (target.getCFlags() != null) {
        for (final Flag flag : target.getCFlags()) {
          command.addFlags(DirectiveHelper.splitOptionString(flag.getValue()));
        }
      }
      if (target.getCppFlags() != null) {
        for (final Flag flag : target.getCppFlags()) {
          command.addFlags(DirectiveHelper.splitOptionString(flag.getValue()));
        }
      }
    }
  }

  protected void processCompilerCommand(final CompilerCommand command,
      final boolean preprocessedFile, final Target target,
      final Map<Object, Object> context) throws ADLException {
    if (target != null) {
      if (target.getCompiler() != null) {
        command.setCommand(target.getCompiler().getPath());
      }
      if (target.getCFlags() != null) {
        for (final Flag flag : target.getCFlags()) {
          command.addFlags(DirectiveHelper.splitOptionString(flag.getValue()));
        }
      }
      if (!preprocessedFile && target.getCppFlags() != null) {
        for (final Flag flag : target.getCppFlags()) {
          command.addFlags(DirectiveHelper.splitOptionString(flag.getValue()));
        }
      }
    }
  }

  protected void processLinkerCommand(final LinkerCommand command,
      final Target target, final Map<Object, Object> context)
      throws ADLException {
    if (target != null) {
      if (target.getLinker() != null) {
        command.setCommand(target.getLinker().getPath());
      }
      if (target.getLdFlags() != null) {
        for (final Flag flag : target.getLdFlags()) {
          command.addFlags(DirectiveHelper.splitOptionString(flag.getValue()));
        }
      }
      if (target.getLinkerScript() != null
          && CompilerContextHelper.getLDFlags(context) == null) {
        command.setLinkerScript(target.getLinkerScript().getPath());
      }
    }
  }

  protected Target loadTarget(final String targetName,
      final Map<Object, Object> context) throws ADLException {
    if (targetName == null) return null;
    try {
      return targetDescriptorLoader.load(targetName, context);
    } catch (final ADLException e) {
      if (e.getError().getTemplate() == TargetDescErrors.TARGET_DESC_NOT_FOUND_FATAL
          || e.getError().getTemplate() == TargetDescErrors.PARSE_ERROR_FATAL
          || e.getError().getTemplate() == TargetDescErrors.CYCLE_FATAL) {
        return null;
      } else {
        throw e;
      }
    }
  }
}
