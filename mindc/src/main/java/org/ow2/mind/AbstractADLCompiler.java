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

package org.ow2.mind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.error.ErrorManager;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public abstract class AbstractADLCompiler implements ADLCompiler {

  @Inject
  protected ErrorManager               errorManager;
  @Inject
  protected CompilationCommandExecutor executor;

  protected abstract void initContext(final String adlName,
      final String execName, final CompilationStage stage,
      final Map<Object, Object> context) throws ADLException;

  protected abstract Iterable<Definition> load(final String adlName,
      final Map<Object, Object> context) throws ADLException;

  protected abstract void generateSource(final Map<Object, Object> context,
      final Definition adlDef) throws ADLException;

  protected abstract Collection<CompilationCommand> compileDefinition(
      final Map<Object, Object> context, final Definition adlDef)
      throws ADLException;

  protected abstract ComponentGraph instantiateGraph(
      final Map<Object, Object> context, final Definition adlDef)
      throws ADLException;

  protected abstract Collection<CompilationCommand> compileGraph(
      final Map<Object, Object> context, final ComponentGraph graph,
      final String execName) throws ADLException;

  public List<Object> compile(final String adlName, final String execName,
      final CompilationStage stage, final Map<Object, Object> context)
      throws ADLException, InterruptedException {
    initContext(adlName, execName, stage, context);

    final int nbErrors = errorManager.getErrors().size();
    final Iterable<Definition> adlDefs = load(adlName, context);
    if (errorManager.getErrors().size() != nbErrors) {
      // ADL contains errors
      return null;
    }

    if (stage == CompilationStage.CHECK_ADL) {
      return Lists.<Object> newArrayList(adlDefs);
    }

    if (stage == CompilationStage.GENERATE_SRC) {
      for (final Definition adlDef : adlDefs) {
        generateSource(context, adlDef);
      }
      return Lists.<Object> newArrayList(adlDefs);
    }

    if (stage == CompilationStage.COMPILE_DEF) {
      final List<Object> result = new ArrayList<Object>();
      for (final Definition adlDef : adlDefs) {
        final Collection<CompilationCommand> commands = compileDefinition(
            context, adlDef);
        final boolean execOK = executor.exec(commands, context);
        if (execOK) {
          for (final CompilationCommand command : commands) {
            if (command instanceof CompilerCommand) {
              result.addAll(command.getOutputFiles());
            }
          }
        }
      }
      return result;
    }

    final List<Object> result = new ArrayList<Object>();
    for (final Definition adlDef : adlDefs) {
      final ComponentGraph graph = instantiateGraph(context, adlDef);
      if (errorManager.getErrors().size() != nbErrors) {
        // ADL contains errors
        return null;
      }

      final Collection<CompilationCommand> commands = compileGraph(context,
          graph, execName);
      if (commands != null) {
        final boolean execOK = executor.exec(commands, context);
        if (execOK) {
          for (final CompilationCommand command : commands) {
            if (command instanceof LinkerCommand) {
              result.addAll(command.getOutputFiles());
            }
          }
        }
      }
    }
    return result;
  }

}