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

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;

import com.google.inject.Inject;

public abstract class AbstractMembraneLoader extends AbstractDelegatingLoader {

  @Inject
  protected NodeFactory nodeFactoryItf;

  @Inject
  protected NodeMerger  nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Controller newControllerNode() {
    return MembraneASTHelper.newControllerNode(nodeFactoryItf);
  }

  protected ControllerInterface newControllerInterfaceNode(
      final String itfName, final boolean isInternal) {
    return MembraneASTHelper.newControllerInterfaceNode(nodeFactoryItf,
        itfName, isInternal);
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
}
