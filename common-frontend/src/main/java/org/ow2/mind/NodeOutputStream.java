/**
 * Copyright (C) 2008-2010 STMicroelectronics
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.io.NodeInputStream;

/**
 * An output stream that allows to serialize graph of AST node. This output
 * stream differs from traditional {@link ObjectOutputStream} from the fact that
 * information about node class is also serialized along side node data.
 * 
 * @see NodeInputStream
 */
public class NodeOutputStream {

  protected static final byte                         NULL_REF     = (byte) 0x10;
  protected static final byte                         NEW_NODE     = (byte) 0x11;
  protected static final byte                         NODE_REF     = (byte) 0x12;

  protected static final byte                         OBJ_DECO     = (byte) 0x20;
  protected static final byte                         NODE_DECO    = (byte) 0x21;
  protected static final byte                         END_OF_DECO  = (byte) 0x22;

  protected static final byte                         NEW_CLASS    = (byte) 0x70;
  protected static final byte                         CLASS_REF    = (byte) 0x71;

  protected final ObjectOutputStream                  out;
  protected final Map<Node, Integer>                  nodeIds      = new IdentityHashMap<Node, Integer>();
  protected final Map<Class<? extends Node>, Integer> nodeClassIds = new IdentityHashMap<Class<? extends Node>, Integer>();

  /**
   * Creates a new {@link NodeOutputStream} that writes in the given
   * {@link ObjectOutputStream}.
   * 
   * @param out the output stream into which serialized nodes are written.
   * @throws IOException if an error occurs.
   */
  public NodeOutputStream(final ObjectOutputStream out) throws IOException {
    this.out = out;
    out.reset();
  }

  /**
   * Creates a new {@link NodeOutputStream} that writes in the given
   * {@link OutputStream}.
   * 
   * @param out the output stream into which serialized nodes are written.
   * @throws IOException if an error occurs.
   */
  public NodeOutputStream(final OutputStream out) throws IOException {
    if (out instanceof ObjectOutputStream) {
      this.out = (ObjectOutputStream) out;
      this.out.reset();
    } else {
      this.out = new ObjectOutputStream(out);
      this.out.flush();
    }
  }

  /**
   * Reset internal state. In particular, this method clears the map of
   * previously serialized nodes. <br>
   * This method must be called simultaneously with the
   * <code>NodeInputStream.reset()</code> method.
   * 
   * @throws IOException if an error occurs.
   * @see ObjectOutputStream#reset()
   */
  public synchronized void reset() throws IOException {
    nodeClassIds.clear();
    out.reset();
  }

  /**
   * Flush pending data.
   * 
   * @throws IOException
   * @see ObjectOutputStream#flush()
   */
  public synchronized void flush() throws IOException {
    out.flush();
  }

  /**
   * Close the stream.
   * 
   * @throws IOException if an error occurs.
   * @see ObjectOutputStream#close()
   */
  public synchronized void close() throws IOException {
    flush();
    reset();
    out.close();
  }

  /**
   * Write the given node and its sub nodes.
   * 
   * @param node the node to serialize. May be <code>null</code>.
   * @throws IOException if an error occurs.
   */
  public synchronized void writeNode(final Node node) throws IOException {
    if (node == null) {
      // node is null simply write a NULL_REF.
      out.writeByte(NULL_REF);
      return;
    }

    Integer id = nodeIds.get(node);
    if (id == null) {
      // node has not been serialized yet.

      // Allocate an id for node.
      id = nodeIds.size();
      nodeIds.put(node, id);

      // write NEW_NODE marker;
      out.writeByte(NEW_NODE);
      writeNodeClass(node);
      writeNodeData(node);
    } else {
      // node has already been serialized.

      // write NODE_REF marker and nodeID
      out.writeByte(NODE_REF);
      out.writeInt(id);
    }
  }

  protected void writeNodeClass(final Node node) throws IOException {
    final Class<? extends Node> nodeClass = node.getClass();
    Integer id = nodeClassIds.get(nodeClass);
    if (id == null) {
      // node class has not been serialized yet.

      // Allocate an id for the class.
      id = nodeClassIds.size();
      nodeClassIds.put(nodeClass, id);

      // write NEW_CLASS marker and class bytecode
      out.writeByte(NEW_CLASS);
      out.writeUTF(node.astGetType());
      final Class<?>[] nodeInterfaces = nodeClass.getInterfaces();
      // assumes that there is less than 256 interfaces
      out.writeByte(nodeInterfaces.length);
      for (final Class<?> itf : nodeInterfaces) {
        out.writeUTF(itf.getName());
      }
    } else {
      // node class has already been serialized.

      // write CLASS_REF marker and classID
      out.writeByte(CLASS_REF);
      out.writeInt(id);
    }
  }

  protected void writeNodeData(final Node node) throws IOException {
    // write node source
    out.writeObject(node.astGetSource());

    // write node attributes
    final Map<String, String> attributes = node.astGetAttributes();
    out.writeInt(attributes.size());
    for (final Map.Entry<String, String> attribute : attributes.entrySet()) {
      // attribute name can't be null, can use writeUTF safely.
      out.writeUTF(attribute.getKey());
      out.writeObject(attribute.getValue());
    }

    // write node decorations
    for (final Map.Entry<String, Object> decoration : node.astGetDecorations()
        .entrySet()) {
      final Object deco = decoration.getValue();
      if (deco == null || deco instanceof Serializable) {
        out.writeByte(OBJ_DECO);
        // assert that decoration name is not null, use writeUTF .
        out.writeUTF(decoration.getKey());
        out.writeObject(deco);
      } else if (deco instanceof Node) {
        out.writeByte(NODE_DECO);
        // assert that decoration name is not null, use writeUTF .
        out.writeUTF(decoration.getKey());
        writeNode((Node) deco);
      }
    }
    out.writeByte(END_OF_DECO);

    // write sub nodes

    // count number of sub node
    int nbSubNode = 0;
    for (final String nodeType : node.astGetNodeTypes()) {
      final Node[] subNodes = node.astGetNodes(nodeType);
      if (subNodes != null) {
        for (final Node subNode : subNodes) {
          if (subNode != null) nbSubNode++;
        }
      }
    }

    // write number of sub node
    out.writeInt(nbSubNode);

    // write sub nodes
    for (final String nodeType : node.astGetNodeTypes()) {
      final Node[] subNodes = node.astGetNodes(nodeType);
      if (subNodes != null) {
        for (final Node subNode : subNodes) {
          if (subNode != null) writeNode(subNode);
        }
      }
    }
  }
}
