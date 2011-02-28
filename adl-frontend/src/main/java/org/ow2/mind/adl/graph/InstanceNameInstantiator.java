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

import static org.ow2.mind.NameHelper.toValidName;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.graph.Instantiator.AbstractDelegatingInstantiator;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

public class InstanceNameInstantiator extends AbstractDelegatingInstantiator {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Instantiator interface
  // ---------------------------------------------------------------------------

  public ComponentGraph instantiate(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final ComponentGraph graph = clientInstantiatorItf.instantiate(definition,
        context);
    initInstanceName(graph, "");
    return graph;
  }

  protected void initInstanceName(final ComponentGraph graph, String path) {
    String instanceName = (String) graph.getDecoration("instance-name");
    if (instanceName == null) {
      final Definition def = graph.getDefinition();
      if (ASTHelper.isSingleton(def)) {
        // component is a singleton
        instanceName = "__component_" + toValidName(def.getName())
            + "_singleton_instance";
      } else {
        if (graph.getParents().length == 0) {
          // top level component
          instanceName = "__component_" + toValidName(def.getName());
        } else {
          instanceName = path + "_"
              + graph.getNameInParent(graph.getParents()[0]);
        }
      }
      graph.setDecoration("instance-name", instanceName);

      if (graph.getParents().length == 0) {
        // top level component
        path = "__component_" + toValidName(def.getName());
      } else {
        path = path + "_" + graph.getNameInParent(graph.getParents()[0]);
      }
      for (final ComponentGraph subComponent : graph.getSubComponents()) {
        initInstanceName(subComponent, path);
      }
    }
  }
}
