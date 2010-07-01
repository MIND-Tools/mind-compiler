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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
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
    throw new ADLException(ComponentErrors.DUPLICATED_COMPONENT_NAME,
        subNode.getName(), new NodeErrorLocator(previousDeclaration));
  }

  @Override
  protected void removeSubNode(final Node node, final Component subNode) {
    ((ComponentContainer) node).getComponents();
  }
}
