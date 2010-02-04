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

package org.ow2.mind.adl.graph;

import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isClient;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;

public class BindingInstantiator extends AbstractInstantiator {

  // ---------------------------------------------------------------------------
  // Implementation of the Instantiator interface
  // ---------------------------------------------------------------------------

  public ComponentGraph instantiate(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final ComponentGraph graph = clientInstantiatorItf.instantiate(definition,
        context);
    initBindingDescs(graph);
    fillBindingDescs(graph);

    return graph;
  }

  protected void initBindingDescs(final ComponentGraph graph) {
    if (graph.getDefinition() instanceof InterfaceContainer) {
      final Map<String, BindingDescriptor> descs = getBindingDescriptors(graph);
      for (final Interface itf : ((InterfaceContainer) graph.getDefinition())
          .getInterfaces()) {
        if (isClient(itf)) {
          final int noe = ASTHelper.getNumberOfElement(itf);
          if (noe == -1) {
            descs.put(itf.getName(), new BindingDescriptor());
          } else {
            for (int i = 0; i < noe; i++) {
              descs.put(itf.getName() + "_" + i, new BindingDescriptor());
            }
          }
        }
      }

      if (graph.getDefinition() instanceof InternalInterfaceContainer) {
        for (final Interface itf : ((InternalInterfaceContainer) graph
            .getDefinition()).getInternalInterfaces()) {
          if (isClient(itf)) {
            final int noe = ASTHelper.getNumberOfElement(itf);
            if (noe == -1) {
              descs.put("INTERNAL_" + itf.getName(), new BindingDescriptor());
            } else {
              for (int i = 0; i < noe; i++) {
                descs.put("INTERNAL_" + itf.getName() + "_" + i,
                    new BindingDescriptor());
              }
            }
          }
        }
      }
    }

    // TODO handle shared components
    for (final ComponentGraph subComponent : graph.getSubComponents()) {
      initBindingDescs(subComponent);
    }
  }

  protected void fillBindingDescs(final ComponentGraph graph) {
    final Definition def = graph.getDefinition();
    if (def instanceof BindingContainer) {
      for (final Binding binding : ((BindingContainer) def).getBindings()) {
        final ComponentGraph clientComponent = getComponent(graph, binding
            .getFromComponent());
        final ComponentGraph serverComponent = getComponent(graph, binding
            .getToComponent());
        String fromItfName = binding.getFromInterface();
        if (Binding.THIS_COMPONENT.equals(binding.getFromComponent()))
          fromItfName = "INTERNAL_" + fromItfName;
        if (binding.getFromInterfaceNumber() != null)
          fromItfName += "_" + binding.getFromInterfaceNumber();

        final BindingDescriptor desc;
        desc = getBindingDescriptors(clientComponent).get(fromItfName);
        assert desc != null;
        desc.binding = binding;
        desc.serverComponent = serverComponent;
      }
    }

    // TODO handle shared components
    for (final ComponentGraph subComponent : graph.getSubComponents()) {
      fillBindingDescs(subComponent);
    }
  }

  ComponentGraph getComponent(final ComponentGraph graph, final String name) {
    if (Binding.THIS_COMPONENT.equals(name)) {
      return graph;
    } else {
      return graph.getSubComponent(name);
    }
  }

  @SuppressWarnings("unchecked")
  protected static Map<String, BindingDescriptor> getBindingDescriptors(
      final ComponentGraph component) {
    Map<String, BindingDescriptor> descs = (Map<String, BindingDescriptor>) component
        .getDecoration("binding-descriptors");
    if (descs == null) {
      descs = new HashMap<String, BindingDescriptor>();
      component.setDecoration("binding-descriptors", descs);
    }
    return descs;
  }

  public static class BindingDescriptor {
    public Binding        binding;
    public ComponentGraph serverComponent;
  }
}
