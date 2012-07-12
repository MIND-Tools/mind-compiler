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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;

/**
 * Sub-component normalizer
 */
public class SubComponentNormalizerLoader
    extends
      AbstractNormalizerLoader<Component> {

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  @Override
  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    expandMultiComponents(d);
    normalize(d);
    return d;
  }

  protected void expandMultiComponents(final Definition d) {
    if (!(d instanceof ComponentContainer)) return;
    final ComponentContainer componentContainer = (ComponentContainer) d;
    final Component[] subComponents = componentContainer.getComponents();
    if (subComponents == null) return;

    for (final Component subComp : subComponents) {
      // first remove subComp. will be readded later
      componentContainer.removeComponent(subComp);

      // split subComp name around comma
      final String[] names = subComp.getName().split(",");

      // re-add first name (assuming that it as at least one name
      subComp.setName(names[0]);
      componentContainer.addComponent(subComp);
      // add clones to subsequent names
      for (int i = 1; i < names.length; i++) {
        final Component newSubComp = NodeUtil.cloneTree(subComp);
        newSubComp.setName(names[i]);
        componentContainer.addComponent(newSubComp);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the abstract methods of AbstractNormalizerLoader
  // ---------------------------------------------------------------------------

  @Override
  protected Component[] getSubNodes(final Node node) {
    if (node instanceof ComponentContainer)
      return ((ComponentContainer) node).getComponents();
    return null;
  }

  @Override
  protected Object getId(final Component node) {
    return node.getName();
  }

  @Override
  protected void handleNameClash(final Component previousDeclaration,
      final Component subNode) throws ADLException {
    errorManagerItf.logError(ComponentErrors.DUPLICATED_COMPONENT_NAME,
        subNode, subNode.getName(), new NodeErrorLocator(previousDeclaration));
  }

  @Override
  protected void removeSubNode(final Node node, final Component subNode) {
    ((ComponentContainer) node).getComponents();
  }
}
