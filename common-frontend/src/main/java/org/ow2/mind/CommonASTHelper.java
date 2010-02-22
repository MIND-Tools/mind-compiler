/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;

/**
 * Common AST helper methods.
 */
public final class CommonASTHelper {
  private CommonASTHelper() {
  }

  /**
   * Returns a new AST node created with the given nodeFactory.
   * 
   * @param <T> the type of the returned node.
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param nodeType the {@link Node#astGetType() node type}.
   * @param nodeItf one interface implemented by the created node. The return
   *          type of this method is an instance of this class.
   * @param itfs the node interfaces that are implemented by the node to create.
   * @return a new AST node.
   */
  public static <T extends Node> T newNode(final NodeFactory nodeFactory,
      final String nodeType, final Class<T> nodeItf, final Class<?>... itfs) {
    final String[] itfNames = new String[itfs.length + 1];
    itfNames[0] = nodeItf.getName();
    for (int i = 0; i < itfs.length; i++) {
      itfNames[i + 1] = itfs[i].getName();
    }
    try {
      return nodeItf.cast(nodeFactory.newNode(nodeType, itfNames));
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unexpected error.");
    }
  }

  /**
   * Transforms the given node to a node that implements the
   * <code>nodeItf</code> interface. If the node already implements the
   * interface, this method simply cast it. Otherwise this method use the given
   * node factory and node merger to create a copy of the given node that
   * implements the <code>nodeItf</code> interface.
   * 
   * @param <T> the type of the returned node.
   * @param node the node to transform.
   * @param nodeItf the interface that the give node must implement
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted in the <code>nodeItf</code> interface,
   *         or a copy of the given node that implements the
   *         <code>nodeItf</code> interface.
   */
  public static <T extends Node> T turnsTo(final Node node,
      final Class<T> nodeItf, final NodeFactory nodeFactory,
      final NodeMerger nodeMerger) {
    if (nodeItf.isInstance(node)) return nodeItf.cast(node);

    // the given node does not implements the desired interface.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = newNode(nodeFactory, node.astGetType(), nodeItf);
      return nodeItf.cast(nodeMerger.merge(node, n, null));
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }
}
