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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.predefined.controller.Component;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.implementation.SharedImplementationDecorationHelper;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;

/**
 * {@link ADLLoaderAnnotationProcessor annotation processor} for the
 * {@link Component} annotation.
 */
public abstract class AbstractControllerADLLoaderAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected Definition addControllerInterfae(final Definition definition,
      final String controllerItfName, final String controllerSignature,
      final String controllerName, final String controllerImpl)
      throws ADLException {
    if (!(definition instanceof InterfaceContainer)) return null;

    if (controllerImpl != null)
      SharedImplementationDecorationHelper.addSharedImplementation(definition,
          controllerImpl);

    if (ASTHelper.getInterface(definition, controllerItfName) == null) {
      // Add the controller interface.
      final MindInterface ctrlInterface = ASTHelper
          .newInterfaceNode(nodeFactoryItf);
      ctrlInterface.setName(controllerItfName);
      ctrlInterface.setRole(TypeInterface.SERVER_ROLE);
      ctrlInterface.setSignature(controllerSignature);
      ((InterfaceContainer) definition).addInterface(ctrlInterface);
    }

    // Create the controller node.
    final ControllerContainer ctrlContainer = MembraneASTHelper
        .turnToControllerContainer(definition, nodeFactoryItf, nodeMergerItf);

    // check if the controller node is already present
    boolean ctrlFound = false;
    forEachCtrl : for (final Controller ctrl : ctrlContainer.getControllers()) {
      for (final ControllerInterface ctrlItf : ctrl.getControllerInterfaces()) {
        if (ctrlItf.getName().equals(controllerItfName)) {
          ctrlFound = true;
          break forEachCtrl;
        }
      }
    }

    if (!ctrlFound) {
      final Controller ctrl = MembraneASTHelper
          .newControllerNode(nodeFactoryItf);
      ctrl.addControllerInterface(MembraneASTHelper.newControllerInterfaceNode(
          nodeFactoryItf, controllerItfName, false));
      ctrl.addSource(MembraneASTHelper.newSourceNode(nodeFactoryItf,
          controllerName));

      ctrlContainer.addController(ctrl);
    }

    return (ctrlContainer != definition) ? (Definition) ctrlContainer : null;
  }
}
