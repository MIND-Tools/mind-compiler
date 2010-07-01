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
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;

/**
 * Interface Normalizer
 */
public class InterfaceNormalizerLoader
    extends
      AbstractNormalizerLoader<Interface> {

  // ---------------------------------------------------------------------------
  // Implementation of the abstract methods of AbstractNormalizerLoader
  // ---------------------------------------------------------------------------

  @Override
  protected Interface[] getSubNodes(final Node node) {
    if (node instanceof InterfaceContainer)
      return ((InterfaceContainer) node).getInterfaces();
    return null;
  }

  @Override
  protected Object getId(final Interface node) {
    return node.getName();
  }

  @Override
  protected void handleNameClash(final Interface previousDeclaration,
      final Interface subNode) throws ADLException {
    throw new ADLException(InterfaceErrors.DUPLICATED_INTERFACE_NAME,
        subNode.getName(), new NodeErrorLocator(previousDeclaration));
  }

  @Override
  protected void removeSubNode(final Node node, final Interface subNode) {
    ((InterfaceContainer) node).removeInterface(subNode);
  }
}
