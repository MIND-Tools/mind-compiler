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

package org.ow2.mind.adl.factory;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.DefinitionCompiler;
import org.ow2.mind.adl.GraphCompiler;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.CompilationCommand;

public class FactoryGraphCompiler implements GraphCompiler, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_LOADER_ITF_NAME       = "loader";
  public Loader              loaderItf;

  public static final String CLIENT_COMPILER_ITF_NAME     = "client-compiler";
  public GraphCompiler       clientCompilerItf;

  public static final String DEFINITION_COMPILER_ITF_NAME = "definition-compiler";
  public DefinitionCompiler  definitionCompilerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public Collection<CompilationCommand> visit(final ComponentGraph graph,
      final Map<Object, Object> context) throws ADLException {

    final Collection<CompilationCommand> compilationCommands = clientCompilerItf
        .visit(graph, context);

    visitFactoryDefinitions(graph, graph, null, compilationCommands, context);

    return compilationCommands;
  }

  protected void visitFactoryDefinitions(final ComponentGraph topLevelGraph,
      final ComponentGraph graph, Set<Definition> compiledDefs,
      final Collection<CompilationCommand> compilationCommands,
      final Map<Object, Object> context) throws ADLException {
    final Definition factoryDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(graph.getDefinition(), loaderItf,
            context);

    if (factoryDefinition != null) {
      // the "graph" component is a factory component.

      if (compiledDefs == null) {
        // compiledDefs is initialized lazily to avoid useless computation if
        // the graph does not contain factory.
        compiledDefs = new HashSet<Definition>();
        initCompiledDefs(topLevelGraph, compiledDefs);
      }

      compileFactoryDef(factoryDefinition, compiledDefs, compilationCommands,
          context);
    }

    // visit sub-components
    for (final ComponentGraph subComponent : graph.getSubComponents()) {
      visitFactoryDefinitions(topLevelGraph, subComponent, compiledDefs,
          compilationCommands, context);
    }
  }

  protected void compileFactoryDef(final Definition factoryDefinition,
      final Set<Definition> compiledDefs,
      final Collection<CompilationCommand> compilationCommands,
      final Map<Object, Object> context) throws ADLException {
    final boolean newDef = compiledDefs.add(factoryDefinition);
    if (newDef) {
      // the definition that is instantiated by the factory has not been
      // compiled yet, compile it.
      compileDef(factoryDefinition, compiledDefs, compilationCommands, context);
    }
  }

  protected void compileDef(final Definition definition,
      final Set<Definition> compiledDefs,
      final Collection<CompilationCommand> compilationCommands,
      final Map<Object, Object> context) throws ADLException {
    compilationCommands
        .addAll(definitionCompilerItf.visit(definition, context));

    if (definition instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) definition)
          .getComponents()) {
        final Definition subCompDef = ASTHelper.getResolvedComponentDefinition(
            subComp, loaderItf, context);
        compileFactoryDef(subCompDef, compiledDefs, compilationCommands,
            context);
        final Definition factoryDefinition = ASTHelper
            .getFactoryInstantiatedDefinition(subCompDef, loaderItf, context);
        if (factoryDefinition != null) {
          // the sub-component is a factory component.
          compileFactoryDef(factoryDefinition, compiledDefs,
              compilationCommands, context);
        }
      }
    }
  }

  protected void initCompiledDefs(final ComponentGraph graph,
      final Set<Definition> compiledDefs) {
    final boolean newDef = compiledDefs.add(graph.getDefinition());
    if (newDef) {
      for (final ComponentGraph subComponent : graph.getSubComponents()) {
        initCompiledDefs(subComponent, compiledDefs);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_LOADER_ITF_NAME)) {
      loaderItf = (Loader) value;
    } else if (itfName.equals(CLIENT_COMPILER_ITF_NAME)) {
      clientCompilerItf = (GraphCompiler) value;
    } else if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      definitionCompilerItf = (DefinitionCompiler) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(CLIENT_LOADER_ITF_NAME, CLIENT_COMPILER_ITF_NAME,
        DEFINITION_COMPILER_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_LOADER_ITF_NAME)) {
      return loaderItf;
    } else if (itfName.equals(CLIENT_COMPILER_ITF_NAME)) {
      return clientCompilerItf;
    } else if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      return definitionCompilerItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_LOADER_ITF_NAME)) {
      loaderItf = null;
    } else if (itfName.equals(CLIENT_COMPILER_ITF_NAME)) {
      clientCompilerItf = null;
    } else if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      definitionCompilerItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
