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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;

public class ComponentGraph {
  private static final ComponentGraph[] EMPTY_COMP_ARRAY = new ComponentGraph[0];

  private final Definition              definition;
  private final Node                    source;

  // most of the time a component has only one parent (i.e. it is not shared)
  // optimize this case.
  private ComponentGraph                parent;
  private String                        nameInParent;

  // map associating additional parents to the name of this component in the
  // parent.
  private Map<ComponentGraph, String>   parents;

  // sub components map is instantiated lazily
  private Map<String, ComponentGraph>   subComponents;

  private Map<String, Object>           decorations;

  public ComponentGraph(final Definition definition, final Node source) {
    if (definition == null)
      throw new IllegalArgumentException("definition can't be null");
    this.definition = definition;
    this.source = source;
  }

  public Definition getDefinition() {
    return definition;
  }

  public String getNameInParent(final ComponentGraph parent) {
    if (parent == null)
      throw new IllegalArgumentException("parent can't be null");

    if (this.parent == parent) return nameInParent;

    if (parents == null)
      throw new IllegalArgumentException(
          "Given ComponentGraph is not a parent of this ComponentGraph.");
    final String name = parents.get(parent);
    if (name == null)
      throw new IllegalArgumentException(
          "Given ComponentGraph is not a parent of this ComponentGraph.");

    return name;
  }

  public ComponentGraph[] getParents() {
    if (parent == null) {
      assert parents == null;
      return EMPTY_COMP_ARRAY;
    }

    if (parents == null) {
      return new ComponentGraph[]{parent};
    }

    final ComponentGraph[] result = new ComponentGraph[1 + parents.size()];
    result[0] = parent;
    int i = 1;
    for (final ComponentGraph p : parents.keySet()) {
      result[i++] = p;
    }

    return result;
  }

  public ComponentGraph getSubComponent(final String name) {
    if (subComponents == null)
      return null;
    else
      return subComponents.get(name);
  }

  public ComponentGraph[] getSubComponents() {
    if (subComponents == null || subComponents.isEmpty())
      return EMPTY_COMP_ARRAY;
    else
      return subComponents.values().toArray(
          new ComponentGraph[subComponents.size()]);
  }

  public void addSubComponent(final ComponentGraph subComponent,
      final String name) {
    if (subComponent == null)
      throw new IllegalArgumentException("subComponent can't be null");
    if (name == null) throw new IllegalArgumentException("name can't be null");

    if (subComponents == null)
      subComponents = new LinkedHashMap<String, ComponentGraph>();
    subComponents.put(name, subComponent);

    if (subComponent.parent == null) {
      subComponent.parent = this;
      subComponent.nameInParent = name;
    } else {
      if (subComponent.parents == null)
        subComponent.parents = new HashMap<ComponentGraph, String>();
      subComponent.parents.put(this, name);
    }
  }

  private Map<String, Object> decorations() {
    if (decorations == null) decorations = new HashMap<String, Object>();
    return decorations;
  }

  public Map<String, Object> getDecorations() {
    if (decorations == null)
      return new HashMap<String, Object>();
    else
      return new HashMap<String, Object>(this.decorations());
  }

  public Object getDecoration(final String name) {
    if (decorations == null) return null;
    return decorations.get(name);
  }

  public void setDecoration(final String name, final Object decoration) {
    this.decorations().put(name, decoration);
  }

  public void setDecorations(final Map<String, Object> decorations) {
    if (decorations.size() > 0) this.decorations().putAll(decorations);
  }

  public Node getSource() {
    return source;
  }
}
