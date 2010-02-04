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

package org.ow2.mind.adl.membrane;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;

public abstract class AbstractMembraneLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The node factory interface */
  public NodeFactory nodeFactoryItf;

  /** The node merger interface */
  public NodeMerger  nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Controller newControllerNode() {
    return MembraneASTHelper.newControllerNode(nodeFactoryItf);
  }

  protected ControllerInterface newControllerInterfaceNode(
      final String itfName, final boolean isInternal) {
    return MembraneASTHelper.newControllerInterfaceNode(nodeFactoryItf, itfName,
        isInternal);
  }

  protected Data newDataNode(final String path) {
    return MembraneASTHelper.newDataNode(nodeFactoryItf, path);
  }

  protected Source newSourceNode(final String path) {
    return MembraneASTHelper.newSourceNode(nodeFactoryItf, path);
  }

  protected ControllerContainer turnToControllerContainer(final Node node) {
    return MembraneASTHelper.turnToControllerContainer(node, nodeFactoryItf,
        nodeMergerItf);
  }

  protected InternalInterfaceContainer turnToInternalInterfaceContainer(
      final Node node) {
    return MembraneASTHelper.turnToInternalInterfaceContainer(node,
        nodeFactoryItf, nodeMergerItf);
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = (NodeMerger) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), NodeFactory.ITF_NAME,
        NodeMerger.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      return nodeMergerItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
