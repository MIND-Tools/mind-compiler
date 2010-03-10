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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.mind.adl.ASTChecker.CheckerIterator;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.graph.ComponentGraph;

public class GraphChecker {

  protected final Map<Object, Object> checkers = new IdentityHashMap<Object, Object>();
  protected final ASTChecker          astChecker;

  public GraphChecker(final ASTChecker checker) {
    this.astChecker = checker;
  }

  public GraphChecker() {
    this(new ASTChecker());
  }

  protected void checkGraph(final ComponentGraph graph) {
    assertNotNull("Given graph is null", graph);
    assertNotNull("Graph definition is null", graph.getDefinition());
  }

  public ComponentGraphChecker assertGraph(final ComponentGraph graph) {
    ComponentGraphChecker checker = (ComponentGraphChecker) checkers.get(graph);
    if (checker == null) {
      checker = createComponentGraphChecker(graph);
      checkers.put(graph, checker);
    }
    return checker;
  }

  protected ComponentGraphChecker createComponentGraphChecker(
      final ComponentGraph graph) {
    return new ComponentGraphChecker(graph);
  }

  public class ComponentGraphChecker {
    protected final ComponentGraph graph;

    public ComponentGraphChecker(final ComponentGraph graph) {
      this.graph = graph;
      checkGraph(graph);
    }

    public DefinitionChecker definition() {
      return astChecker.assertDefinition(graph.getDefinition());
    }

    public ComponentGraphChecker and() {
      return this;
    }

    public ComponentGraphChecker isNotShared() {
      assertTrue("Compoent has more than one parent",
          graph.getParents().length <= 1);
      return this;
    }

    public ComponentGraphChecker isShared() {
      assertTrue("Compoent has less than two parent",
          graph.getParents().length > 1);
      return this;
    }

    public ComponentGraphChecker containsSubComponent(final String name) {
      definition().containsComponent(name);

      final ComponentGraph subComponent = graph.getSubComponent(name);
      assertNotNull("Component graph does not contains a " + name
          + " sub-component.", subComponent);
      return assertGraph(subComponent);
    }

    public ComponentGraphCheckerIterator containsSubComponents(
        final String... names) {
      definition().containsComponents(names);

      final Set<String> nameSet = new HashSet<String>();
      final List<ComponentGraphChecker> list = new ArrayList<ComponentGraphChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsSubComponent(name));
      }
      assertEquals("Definition contains more sub-components than expected.",
          names.length, graph.getSubComponents().length);
      return new ComponentGraphCheckerIterator(list);
    }

    public String containsAttributeValue(final String name) {
      definition().containsAttribute(name);

      final Map<?, ?> attributeValues = (Map<?, ?>) graph
          .getDecoration("attribute-values");
      assertNotNull("Component does not contains attribute value",
          attributeValues);
      final String v = (String) attributeValues.get(name);
      assertNotNull(
          "Component does not contains attribute value for attribute " + name,
          v);
      return v;
    }

    public ComponentGraphChecker containsAttributeValue(final String name,
        final int v) {
      assertEquals("Unexpected integer value", containsAttributeValue(name),
          Integer.toString(v));
      return this;
    }

    public ComponentGraphChecker containsAttributeValue(final String name,
        final String v) {
      assertEquals("Unexpected String value", containsAttributeValue(name), v);
      return this;
    }
  }

  public class ComponentGraphCheckerIterator
      extends
        CheckerIterator<ComponentGraphCheckerIterator, ComponentGraphChecker> {

    public ComponentGraphCheckerIterator(final List<ComponentGraphChecker> list) {
      super(list);
    }

    @Override
    protected ComponentGraphCheckerIterator getThis() {
      return this;
    }

    public ComponentGraphCheckerIterator isNotShared() {
      element.isNotShared();
      return this;
    }

    public ComponentGraphCheckerIterator isShared() {
      element.isShared();
      return this;
    }

    public ComponentGraphCheckerIterator containsSubComponent(final String name) {
      element.containsSubComponent(name);
      return this;
    }

    public ComponentGraphCheckerIterator containsSubComponents(
        final String... names) {
      return element.containsSubComponents(names);
    }

    public ComponentGraphCheckerIterator containsAttributeValue(
        final String name) {
      element.containsAttributeValue(name);
      return this;
    }

    public ComponentGraphCheckerIterator containsAttributeValue(
        final String name, final int v) {
      element.containsAttributeValue(name, v);
      return this;
    }

    public ComponentGraphCheckerIterator containsAttributeValue(
        final String name, final String v) {
      element.containsAttributeValue(name, v);
      return this;
    }

  }
}
