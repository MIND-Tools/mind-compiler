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

package org.ow2.mind.st;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.NodeContainerDecoration;

public class BasicASTTransformer
    implements
      StringTemplateASTTransformer,
      BindingController {

  protected Map<Class<?>, Node> nodeCache = new HashMap<Class<?>, Node>();

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /**
   * Client interface bound to the {@link NodeFactory node factory} component.
   */
  public NodeFactory            nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ASTTransformer interface
  // ---------------------------------------------------------------------------

  public <T extends Node> T toStringTemplateAST(final T tree) {
    return toStringTemplateGraph(tree, new IdentityHashMap<Node, Node>());
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  protected <T extends Node> T toStringTemplateGraph(final T node,
      final Map<Node, Node> clonedNodes) {
    T clone = (T) clonedNodes.get(node);
    if (clone == null) {
      clone = toStringTemplateNode(node, clonedNodes);

      // add sub nodes
      for (final String subNodeType : node.astGetNodeTypes()) {
        for (final Node subNode : node.astGetNodes(subNodeType)) {
          if (subNode != null)
            clone.astAddNode(toStringTemplateGraph(subNode, clonedNodes));
        }
      }

      clonedNodes.put(node, clone);
      // Add the clone itself in the clonedNodes map to avoid to re-clone it in
      // case of a NodeContainerDecoration that is shared by different nodes.
      clonedNodes.put(clone, clone);
    }

    return clone;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Node> T toStringTemplateNode(final T node,
      final Map<Node, Node> clonedNodes) {
    T newNode;
    if (node instanceof AbstractSTNode) {
      newNode = (T) node.astNewInstance();
    } else {
      final Class<?> nodeClass = node.getClass();

      // look in cache for an already generated node for the given node class
      Node n = nodeCache.get(nodeClass);
      if (n == null) {
        // no node found in cache
        final Class<?>[] nodeInterfaces = nodeClass.getInterfaces();
        final String[] nodeItfNames = new String[nodeInterfaces.length];
        for (int i = 0; i < nodeInterfaces.length; i++) {
          nodeItfNames[i] = nodeInterfaces[i].getName();
        }
        // use nodeFactory to generate the node.
        try {
          n = nodeFactoryItf.newNode(node.astGetType(), AbstractSTNode.class,
              nodeItfNames);
        } catch (final ClassNotFoundException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Unexpected error while cloning AST node");
        }

        // put node in cache
        nodeCache.put(nodeClass, n);
      }
      newNode = (T) n.astNewInstance();
    }

    // copy node attributes
    newNode.astSetAttributes(node.astGetAttributes());
    // copy source
    newNode.astSetSource(node.astGetSource());
    // copy node decoration
    final Map<String, Object> decorations = node.astGetDecorations();
    for (final Map.Entry<String, Object> entry : decorations.entrySet()) {
      final Object decoration = entry.getValue();
      if (decoration instanceof Node) {
        // decoration is itself a Node, transform it
        entry.setValue(toStringTemplateGraph((Node) decoration, clonedNodes));
      } else if (decoration instanceof NodeContainerDecoration) {
        final NodeContainerDecoration nodeContainerDecoration = (NodeContainerDecoration) decoration;
        final Collection<Node> containedNodes = nodeContainerDecoration
            .getNodes();
        if (!containedNodes.isEmpty()) {
          final IdentityHashMap<Node, Node> replacement = new IdentityHashMap<Node, Node>(
              containedNodes.size());
          for (final Node n : containedNodes) {
            replacement.put(n, toStringTemplateGraph(n, clonedNodes));
          }
          nodeContainerDecoration.replaceNodes(replacement);
        }
      }
    }
    newNode.astSetDecorations(decorations);

    return newNode;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      this.nodeFactoryItf = (NodeFactory) value;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(NodeFactory.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      return this.nodeFactoryItf;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      this.nodeFactoryItf = null;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }
}
