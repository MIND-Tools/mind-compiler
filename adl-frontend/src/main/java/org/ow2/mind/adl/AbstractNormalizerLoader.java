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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

/**
 * Generic abstract class that ease the implementation of {@link Loader} that
 * checks that sub-nodes of a given type of sub-nodes have a unique Id.
 * 
 * @param <T> The type of the sub-nodes to check.
 */
public abstract class AbstractNormalizerLoader<T extends Node>
    extends
      AbstractDelegatingLoader {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    normalize(d);

    return d;
  }

  protected void normalize(final Definition d) throws ADLException {
    final T[] subNodes = getSubNodes(d);
    if (subNodes == null) return;

    final Map<Object, T> nodeByIds = new HashMap<Object, T>();
    for (final T subNode : subNodes) {
      final Object id = getId(subNode);
      final T previousDeclaration = nodeByIds.put(id, subNode);
      if (previousDeclaration != null) {
        handleNameClash(previousDeclaration, subNode);
        removeSubNode(d, subNode);
      }
    }
  }

  /**
   * Method calls when two sub-nodes with the same Id are found.
   * 
   * @param previousDeclaration The first node with the Id.
   * @param subNode The second node with the same Id.
   * @throws ADLException The exception thrown as a consequence of the error.
   */
  protected abstract void handleNameClash(T previousDeclaration, T subNode)
      throws ADLException;

  /**
   * Method to remove a sub-node. This method is called when two sub-nodes with
   * the same Id are found.
   * 
   * @param node the parent node.
   * @param subNode the node to remove.
   */
  protected abstract void removeSubNode(Node node, T subNode);

  /**
   * Returns the array of sub nodes to check. May return <code>null</code>.
   * 
   * @param node the parent node.
   * @return the array of sub nodes to check. May return <code>null</code>.
   */
  protected abstract T[] getSubNodes(Node node);

  /**
   * Returns the Id of the given sub-node.
   * 
   * @param node a sub-node
   * @return the Id of the given sub-node.
   * @throws ADLException If an error occurs.
   */
  protected abstract Object getId(T node) throws ADLException;

}
