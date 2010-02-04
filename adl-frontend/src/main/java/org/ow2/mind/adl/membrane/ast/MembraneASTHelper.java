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

package org.ow2.mind.adl.membrane.ast;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;

/**
 * Helper methods for membrane AST nodes.
 */
public final class MembraneASTHelper {
  private MembraneASTHelper() {
  }

  /**
   * Returns <code>true</code> if the given {@link ControllerInterface} is
   * internal.
   * 
   * @param itf a {@link ControllerInterface}
   * @return <code>true</code> if the given {@link ControllerInterface} is
   *         internal.
   */
  public static boolean isInternalInterface(final ControllerInterface itf) {
    return itf.getIsInternal() != null
        && ControllerInterface.TRUE.equals(itf.getIsInternal());
  }

  /**
   * Create a new internal interface node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new internal interface node.
   */
  public static MindInterface newInternalInterfaceNode(
      final NodeFactory nodeFactory) {
    return ASTHelper.newNode(nodeFactory, "internalInterface",
        MindInterface.class);
  }

  /**
   * Create a new {@link Controller} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link Controller} node.
   */
  public static Controller newControllerNode(final NodeFactory nodeFactory) {
    return ASTHelper.newNode(nodeFactory, "controller", Controller.class);
  }

  /**
   * Create a new {@link ControllerInterface} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param itfName the name of the interface.
   * @param isInternal <code>true</code> if the interface is internal.
   * @return a new {@link ControllerInterface} node.
   */
  public static ControllerInterface newControllerInterfaceNode(
      final NodeFactory nodeFactory, final String itfName,
      final boolean isInternal) {
    final ControllerInterface controllerInterface = ASTHelper.newNode(
        nodeFactory, "controllerInterface", ControllerInterface.class);
    controllerInterface.setName(itfName);
    if (isInternal)
      controllerInterface.setIsInternal(ControllerInterface.TRUE);
    return controllerInterface;
  }

  /**
   * Create a new {@link Data} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param path the path.
   * @return a new {@link Data} node.
   */
  public static Data newDataNode(final NodeFactory nodeFactory,
      final String path) {
    final Data data = ASTHelper.newNode(nodeFactory, "data", Data.class);
    data.setPath(path);
    return data;
  }

  /**
   * Create a new {@link Source} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param path the path.
   * @return a new {@link Source} node.
   */
  public static Source newSourceNode(final NodeFactory nodeFactory,
      final String path) {
    final Source src = ASTHelper.newNode(nodeFactory, "source", Source.class);
    src.setPath(path);
    return src;
  }

  /**
   * Transforms the given node to an {@link ControllerContainer}. If the node
   * already implements the {@link ControllerContainer} interface, this method
   * simply cast it. Otherwise this method use the given node factory and node
   * merger to create a copy of the given node that implements the
   * {@link ControllerContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link ControllerContainer}, or a
   *         copy of the given node that implements {@link ControllerContainer}.
   */
  public static ControllerContainer turnToControllerContainer(final Node node,
      final NodeFactory nodeFactory, final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, ControllerContainer.class, nodeFactory,
        nodeMerger);
  }

  /**
   * Transforms the given node to an {@link InternalInterfaceContainer}. If the
   * node already implements the {@link InternalInterfaceContainer} interface,
   * this method simply cast it. Otherwise this method use the given node
   * factory and node merger to create a copy of the given node that implements
   * the {@link InternalInterfaceContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link InternalInterfaceContainer},
   *         or a copy of the given node that implements
   *         {@link InternalInterfaceContainer}.
   */
  public static InternalInterfaceContainer turnToInternalInterfaceContainer(
      final Node node, final NodeFactory nodeFactory,
      final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, InternalInterfaceContainer.class,
        nodeFactory, nodeMerger);
  }
}
