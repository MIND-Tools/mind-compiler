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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;

public class NodeInputStream {

  protected final ObjectInputStream                   in;
  protected final ArrayList<Node>                     nodeIds      = new ArrayList<Node>();
  protected final Map<Integer, Class<? extends Node>> nodeClassIds = new IdentityHashMap<Integer, Class<? extends Node>>();

  protected final NodeFactory                         nodeFactory;

  /**
   * Creates a new {@link NodeInputStream} that reads from the given
   * {@link ObjectInputStream}.
   * 
   * @param in the input stream from which serialized nodes are read.
   * @param nodeFactory the node factory to be used to create node instances.
   * @throws IOException if an error occurs.
   */
  public NodeInputStream(final ObjectInputStream in,
      final NodeFactory nodeFactory) throws IOException {
    this.in = in;
    in.reset();
    this.nodeFactory = nodeFactory;
  }

  /**
   * Creates a new {@link NodeInputStream} that reads from the given
   * {@link InputStream}.
   * 
   * @param in the input stream from which serialized nodes are read.
   * @param nodeFactory the node factory to be used to create node instances.
   * @throws IOException if an error occurs.
   */
  public NodeInputStream(final InputStream in, final NodeFactory nodeFactory)
      throws IOException {
    if (in instanceof ObjectInputStream) {
      this.in = (ObjectInputStream) in;
      this.in.reset();
    } else {
      this.in = new ObjectInputStream(in);
    }
    this.nodeFactory = nodeFactory;
  }

  /**
   * Reset internal state. In particular, this method clears the map of
   * previously de-serialized nodes. <br>
   * This method must be called simultaneously with the
   * <code>NodeOutputStream.reset()</code> method.
   * 
   * @throws IOException if an error occurs.
   * @see ObjectInputStream#reset()
   */
  public synchronized void reset() throws IOException {
    nodeClassIds.clear();
  }

  /**
   * Close the stream.
   * 
   * @throws IOException if an error occurs.
   * @see ObjectInputStream#close()
   */
  public synchronized void close() throws IOException {
    reset();
    in.close();
  }

  /**
   * Read a Node graph.
   * 
   * @return the root of the Node graph.
   * @throws IOException
   * @throws ClassNotFoundException
   * @see ObjectInputStream#readObject()
   */
  public synchronized Node readNode() throws IOException,
      ClassNotFoundException {
    final byte b = in.readByte();
    if (b == NodeOutputStream.NULL_REF) {
      return null;
    }

    if (b == NodeOutputStream.NEW_NODE) {
      final Node node = readNodeClass();
      // store read node in list.
      nodeIds.add(node);

      readNodeData(node);
      return node;
    }

    if (b == NodeOutputStream.NODE_REF) {
      final int id = in.readInt();
      if (id < 0 || id >= nodeIds.size())
        throw new IOException("Stream error");
      return nodeIds.get(id);
    }

    // if falls here, b is not valid
    throw new IOException("Stream error");
  }

  protected Node readNodeClass() throws IOException, ClassNotFoundException {
    final byte b = in.readByte();
    if (b == NodeOutputStream.NEW_CLASS) {
      // read node type
      final String type = in.readUTF();
      // read node interfaces
      final int length = in.readByte();
      final String[] nodeInterfaces = new String[length];
      for (int i = 0; i < length; i++) {
        nodeInterfaces[i] = in.readUTF();
      }
      final Node node = nodeFactory.newNode(type, nodeInterfaces);
      nodeClassIds.put(nodeClassIds.size(), node.getClass());
      return node;
    } else if (b == NodeOutputStream.CLASS_REF) {
      final int nodeClassId = in.readInt();
      final Class<? extends Node> nodeClass = nodeClassIds.get(nodeClassId);
      if (nodeClass == null) throw new IOException("Stream Error");

      try {
        return nodeClass.newInstance();
      } catch (final Exception e) {
        final IOException ioe = new IOException("Stream Error");
        ioe.initCause(e);
        throw ioe;
      }
    } else {
      throw new IOException("Stream Error");
    }
  }

  protected void readNodeData(final Node node) throws IOException,
      ClassNotFoundException {
    // read node source
    node.astSetSource((String) in.readObject());

    // read attributes
    final int nbAttributes = in.readInt();
    final Map<String, String> attributes = new HashMap<String, String>(
        nbAttributes);
    for (int i = 0; i < nbAttributes; i++) {
      final String name = in.readUTF();
      final String value = (String) in.readObject();
      attributes.put(name, value);
    }
    node.astSetAttributes(attributes);

    // read decorations
    byte decoState;
    while ((decoState = in.readByte()) != NodeOutputStream.END_OF_DECO) {
      final String name = in.readUTF();
      Object value;
      if (decoState == NodeOutputStream.OBJ_DECO) {
        value = in.readObject();
      } else if (decoState == NodeOutputStream.NODE_DECO) {
        value = readNode();
      } else {
        throw new IOException("Stream Error");
      }
      node.astSetDecoration(name, value);
    }

    // read sub nodes
    final int nbSubNodes = in.readInt();
    for (int i = 0; i < nbSubNodes; i++) {
      node.astAddNode(readNode());
    }
  }
}
