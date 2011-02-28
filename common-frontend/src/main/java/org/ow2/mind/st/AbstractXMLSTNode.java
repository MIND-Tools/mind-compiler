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

package org.ow2.mind.st;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.xml.XMLNode;

/**
 * Abstract node class that is suitable to access AST node attributes in
 * StringTemplate. This class provides getter methods equivalent to
 * <code>astGet...</code> methods of the {@link Node} interface.
 */
public abstract class AbstractXMLSTNode extends XMLNode {

  protected AbstractXMLSTNode(final String type) {
    super(type);
  }

  // Workaround to a bug in XMLNodeClassLoader.
  public abstract void xmlAddNode(String xmlName, AbstractXMLSTNode node);

  @Override
  public void xmlAddNode(final String xmlName, final XMLNode node) {
    xmlAddNode(xmlName, (AbstractXMLSTNode) node);
  }

  /**
   * Returns the type of this node. This method allows to access the node type
   * in a StringTemplate using the <code>astType</code> attribute.
   * 
   * @return the type of this node.
   * @see #astGetType()
   */
  public String getAstType() {
    return astGetType();
  }

  /**
   * Returns the source of this node. This method allows to access the node
   * source in a StringTemplate using the <code>astSource</code> attribute.
   * 
   * @return the source of this node (such as a file name).
   */
  public String getAstSource() {
    return astGetSource();
  }

  /**
   * Returns the attributes of this node. This method allows to access the node
   * attributes in a StringTemplate using the <code>astAttributes</code>
   * attribute.
   * 
   * @return the attributes of this node.
   */
  public Map<String, String> getAstAttributes() {
    return astGetAttributes();
  }

  /**
   * Returns the decorations of this node. This method allows to access the node
   * decorations in a StringTemplate using the <code>astDecorations</code>
   * attribute.
   * 
   * @return the decorations of this node.
   */
  public Map<String, Object> getAstDecorations() {
    return astGetDecorations();
  }

  /**
   * Returns the types of the sub nodes that this node can have. This method
   * allows to access the types of the sub nodes in a StringTemplate using the
   * <code>astNodeTypes</code>
   * 
   * @return the types of the sub nodes that this node can have.
   */
  public Map<String, Boolean> getAstNodeTypes() {
    final Map<String, Boolean> nodeTypes = new HashMap<String, Boolean>();
    for (final String nodeType : astGetNodeTypes()) {
      nodeTypes.put(nodeType, Boolean.TRUE);
    }
    return nodeTypes;
  }

  /**
   * Returns the sub nodes of this node in a map that associates node types and
   * an (eventually empty) array of the sub nodes of that given type. This
   * method allows to access the sub nodes in a StringTemplate using the
   * <code>astSubNodes</code>
   * 
   * @return the sub nodes of this node in a map that associates node types and
   *         an (eventually empty) array of the sub nodes of that given type.
   */
  public Map<String, Node[]> getAstSubNodes() {
    final Map<String, Node[]> astSubNodes = new HashMap<String, Node[]>();
    for (final String nodeType : astGetNodeTypes()) {
      astSubNodes.put(nodeType, astGetNodes(nodeType));
    }
    return astSubNodes;
  }
}
