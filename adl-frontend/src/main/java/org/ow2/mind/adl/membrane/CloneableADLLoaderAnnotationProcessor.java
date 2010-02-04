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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.predefined.controller.Cloneable;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;
import org.ow2.mind.annotation.Annotation;

/**
 * {@link ADLLoaderAnnotationProcessor annotation processor} for the
 * {@link Cloneable} annotation.
 */
public class CloneableADLLoaderAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  private static final String CLONEABLE_CONTROLLER = "CloneableController";
  private static final String FACTORY_SIGNATURE    = "fractal.api.Factory";
  private static final String FACTORY_ITF_NAME     = "factory";
  private static final String ALLOCATOR_SIGNATURE  = "memory.api.Allocator";
  private static final String ALLOCATOR_ITF_NAME   = "allocator";

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof Cloneable;
    if (!(definition instanceof InterfaceContainer)) return null;

    // Create the factory interface.
    final MindInterface factoryInterface = ASTHelper
        .newInterfaceNode(nodeFactoryItf);
    factoryInterface.setName(FACTORY_ITF_NAME);
    factoryInterface.setRole(TypeInterface.SERVER_ROLE);
    factoryInterface.setSignature(FACTORY_SIGNATURE);
    ((InterfaceContainer) definition).addInterface(factoryInterface);

    final MindInterface allocatorItf = ASTHelper
        .newInterfaceNode(nodeFactoryItf);
    allocatorItf.setName(ALLOCATOR_ITF_NAME);
    allocatorItf.setRole(MindInterface.CLIENT_ROLE);
    allocatorItf.setSignature(ALLOCATOR_SIGNATURE);
    ((InterfaceContainer) definition).addInterface(allocatorItf);

    // Create the "factory" controller node.
    final ControllerContainer ctrlContainer = MembraneASTHelper
        .turnToControllerContainer(node, nodeFactoryItf, nodeMergerItf);

    final Controller factoryCtrl = MembraneASTHelper
        .newControllerNode(nodeFactoryItf);
    factoryCtrl.addControllerInterface(MembraneASTHelper
        .newControllerInterfaceNode(nodeFactoryItf, FACTORY_ITF_NAME, false));
    factoryCtrl.addControllerInterface(MembraneASTHelper
        .newControllerInterfaceNode(nodeFactoryItf, ALLOCATOR_ITF_NAME, false));
    factoryCtrl.addSource(MembraneASTHelper.newSourceNode(nodeFactoryItf,
        CLONEABLE_CONTROLLER));

    ctrlContainer.addController(factoryCtrl);

    return (ctrlContainer != definition) ? (Definition) ctrlContainer : null;
  }

}
