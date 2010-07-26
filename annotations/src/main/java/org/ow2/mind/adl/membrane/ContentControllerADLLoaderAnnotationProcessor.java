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
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotations.controller.ContentController;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.ast.InterfaceDefinition;

/**
 * {@link ADLLoaderAnnotationProcessor annotation processor} for the
 * {@link ContentController} annotation.
 */
public class ContentControllerADLLoaderAnnotationProcessor
    extends
      AbstractControllerADLLoaderAnnotationProcessor
    implements
      DefaultControllerInterfaceConstants {

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    if (phase == ADLLoaderPhase.AFTER_EXTENDS) {
      return addContentController(definition, context);
    } else if (phase == ADLLoaderPhase.AFTER_CHECKING) {
      return addControllerInterfaceDecorations(definition, context);
    } else {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "ADL Annotation processor executed at unexpected phase + " + phase);
    }
  }

  protected Definition addContentController(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    if (!(definition instanceof ComponentContainer)) {
      errorManagerItf.logError(
          ADLErrors.INVALID_CONTENT_CONTROLLER_NOT_A_COMPOSITE, definition);
      return null;
    }
    for (final Component component : ((ComponentContainer) definition)
        .getComponents()) {
      final Definition subCompDef = ASTHelper.getResolvedComponentDefinition(
          component, loaderItf, context);
      if (ASTHelper.getInterface(subCompDef, COMPONENT) == null)
        errorManagerItf
            .logError(
                ADLErrors.INVALID_CONTENT_CONTROLLER_MISSING_COMPONENT_CONTROLLER_ON_SUB_COMPONENT,
                component, component.getName());
      boolean hasClientItf = false;
      for (final Interface itf : ((InterfaceContainer) subCompDef)
          .getInterfaces()) {
        if (TypeInterfaceUtil.isClient(itf)) {
          hasClientItf = true;
          break;
        }
      }
      if (hasClientItf
          && ASTHelper.getInterface(subCompDef, BINDING_CONTROLLER) == null)
        errorManagerItf
            .logError(
                ADLErrors.INVALID_CONTENT_CONTROLLER_MISSING_BINDING_CONTROLLER_ON_SUB_COMPONENT,
                component, component.getName());
    }

    addUsedIDL(definition, BINDING_CONTROLLER_SIGNATURE, context);
    addUsedIDL(definition, LIFECYCLE_CONTROLLER_SIGNATURE, context);
    addUsedIDL(definition, COMPONENT_SIGNATURE, context);

    return addControllerInterface(definition, CC, CONTENT_CONTROLLER_SIGNATURE,
        "ContentController", "/fractal/internal/CCdelegate.c");
  }

  protected Definition addControllerInterfaceDecorations(
      final Definition definition, final Map<Object, Object> context) {
    if (!(definition instanceof ControllerContainer)) {
      return null;
    }
    for (final Controller controller : ((ControllerContainer) definition)
        .getControllers()) {
      for (final ControllerInterface ctrlItf : controller
          .getControllerInterfaces()) {
        if (!MembraneASTHelper.isInternalInterface(ctrlItf)) {
          final Interface itf = ASTHelper.getInterface(definition,
              ctrlItf.getName());
          if (itf != null && TypeInterfaceUtil.isServer(itf)) {
            itf.astSetDecoration("controller-interface", Boolean.TRUE);
          }
        }
      }
    }
    return null;
  }

  protected void addUsedIDL(final Definition definition, final String idlName,
      final Map<Object, Object> context) throws ADLException {
    final TypeInterface tItf = ASTHelper.newInterfaceNode(nodeFactoryItf);
    tItf.setSignature(idlName);
    final InterfaceDefinition itfDef = itfSignatureResolverItf.resolve(tItf,
        definition, context);
    InterfaceDefinitionDecorationHelper.addUsedInterfaceDefinition(definition,
        itfDef);
  }
}
