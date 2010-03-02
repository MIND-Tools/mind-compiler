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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.CompilationCommand;

public class BasicGraphCompiler implements GraphCompiler, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String DEFINITION_COMPILER_ITF_NAME = "definition-compiler";
  public DefinitionCompiler  definitionCompilerItf;

  public static final String INSTANCE_COMPILER_ITF_NAME   = "instance-compiler";
  public InstanceCompiler    instanceCompilerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public Collection<CompilationCommand> visit(final ComponentGraph graph,
      final Map<Object, Object> context) throws ADLException {
    final List<CompilationCommand> result = new ArrayList<CompilationCommand>();

    final List<Definition> definitionList = new ArrayList<Definition>();
    final Map<String, Collection<ComponentGraph>> instanceMap = new HashMap<String, Collection<ComponentGraph>>();

    // visit graph to build instanceMap and compile definitions
    visitGraph(graph, instanceMap, definitionList, result, context);

    if (instanceCompilerItf != null) {
      final Definition topLevelDef = graph.getDefinition();
      for (final Definition def : definitionList) {
        result.addAll(instanceCompilerItf.visit(new InstancesDescriptor(
            topLevelDef, def, instanceMap.get(def.getName())), context));
      }
    }

    return result;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected void visitGraph(final ComponentGraph graph,
      final Map<String, Collection<ComponentGraph>> instanceMap,
      final List<Definition> definitionList,
      final List<CompilationCommand> result, final Map<Object, Object> context)
      throws ADLException {

    Collection<ComponentGraph> instances = instanceMap.get(graph
        .getDefinition().getName());
    if (instances == null) {
      // new definition, compile it.
      result
          .addAll(definitionCompilerItf.visit(graph.getDefinition(), context));
      instances = new ArrayList<ComponentGraph>();
      instances.add(graph);
      instanceMap.put(graph.getDefinition().getName(), instances);
      definitionList.add(graph.getDefinition());
    } else {
      // definition already compiled, simply add instance.
      instances.add(graph);
    }

    for (final ComponentGraph subComp : graph.getSubComponents()) {
      // TODO handle shared components
      visitGraph(subComp, instanceMap, definitionList, result, context);
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      definitionCompilerItf = (DefinitionCompiler) value;
    } else if (itfName.equals(INSTANCE_COMPILER_ITF_NAME)) {
      instanceCompilerItf = (InstanceCompiler) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(DEFINITION_COMPILER_ITF_NAME,
        INSTANCE_COMPILER_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      return definitionCompilerItf;
    } else if (itfName.equals(INSTANCE_COMPILER_ITF_NAME)) {
      return instanceCompilerItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DEFINITION_COMPILER_ITF_NAME)) {
      definitionCompilerItf = null;
    } else if (itfName.equals(INSTANCE_COMPILER_ITF_NAME)) {
      instanceCompilerItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
