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

package org.ow2.mind.adl.factory;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.BindingControllerImplHelper;
import org.ow2.mind.adl.annotation.predefined.controller.BindingController;
import org.ow2.mind.adl.annotation.predefined.controller.Component;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.annotation.AnnotationHelper;

/**
 * This loader component creates the AST of the special "Factory" definition.
 */
public class FactoryLoader extends AbstractLoader {

  public static final String  FORMAL_TYPE_PARAMETER_NAME         = "InstantiatedDefinition";
  public static final String  FACTORY_DEFINITION_NAME            = "Factory";
  public static final String  FACTORY_CONTROLLED_DEFINITION_NAME = "FactoryWithCtrl";
  private static final String FACTORY_CONTROLLER                 = "FactoryController";
  private static final String FACTORY_SIGNATURE                  = "fractal.api.Factory";
  private static final String FACTORY_ITF_NAME                   = "factory";
  private static final String ALLOCATOR_SIGNATURE                = "memory.api.Allocator";
  private static final String ALLOCATOR_ITF_NAME                 = "allocator";

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link NodeFactory} interface used by this component. */
  public NodeFactory          nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    if (FACTORY_DEFINITION_NAME.equals(name)) {
      try {
        return createFactory(context);
      } catch (final ClassNotFoundException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Node factory error");
      }
    } else if (FACTORY_CONTROLLED_DEFINITION_NAME.equals(name)) {
      try {
        final Definition factoryDef = createFactory(context);
        factoryDef.setName(FACTORY_CONTROLLED_DEFINITION_NAME);
        AnnotationHelper.addAnnotation(factoryDef, new Component());
        AnnotationHelper.addAnnotation(factoryDef, new BindingController());
        return factoryDef;
      } catch (final ClassNotFoundException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Node factory error");
      }
    } else {
      return clientLoader.load(name, context);
    }
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Definition createFactory(final Map<Object, Object> context)
      throws ClassNotFoundException {
    final Definition d = (Definition) nodeFactoryItf.newNode("definition",
        Definition.class.getName(), FormalTypeParameterContainer.class
            .getName(), InterfaceContainer.class.getName(),
        ImplementationContainer.class.getName(), ControllerContainer.class
            .getName());
    d.setName(FACTORY_DEFINITION_NAME);

    final FormalTypeParameter typeParameter = (FormalTypeParameter) nodeFactoryItf
        .newNode("formalTypeParameter", FormalTypeParameter.class.getName());
    typeParameter.setName(FORMAL_TYPE_PARAMETER_NAME);
    ((FormalTypeParameterContainer) d).addFormalTypeParameter(typeParameter);

    final MindInterface factoryItf = (MindInterface) nodeFactoryItf.newNode(
        "interface", MindInterface.class.getName());
    factoryItf.setName(FACTORY_ITF_NAME);
    factoryItf.setRole(MindInterface.SERVER_ROLE);
    factoryItf.setSignature(FACTORY_SIGNATURE);
    ((InterfaceContainer) d).addInterface(factoryItf);

    final MindInterface allocatorItf = (MindInterface) nodeFactoryItf.newNode(
        "interface", MindInterface.class.getName());
    allocatorItf.setName(ALLOCATOR_ITF_NAME);
    allocatorItf.setRole(MindInterface.CLIENT_ROLE);
    allocatorItf.setSignature(ALLOCATOR_SIGNATURE);
    ((InterfaceContainer) d).addInterface(allocatorItf);

    final Source factoryCtrlSrc = (Source) nodeFactoryItf.newNode("source",
        Source.class.getName());
    factoryCtrlSrc.setPath(FACTORY_CONTROLLER);

    final ControllerInterface factoryCtrlItf = (ControllerInterface) nodeFactoryItf
        .newNode("controllerInterface", ControllerInterface.class.getName());
    factoryCtrlItf.setName(FACTORY_ITF_NAME);
    final ControllerInterface allocatorCtrlItf = (ControllerInterface) nodeFactoryItf
        .newNode("controllerInterface", ControllerInterface.class.getName());
    allocatorCtrlItf.setName(ALLOCATOR_ITF_NAME);

    final Controller factoryCtrl = (Controller) nodeFactoryItf.newNode(
        "controller", Controller.class.getName());
    factoryCtrl.addSource(factoryCtrlSrc);
    factoryCtrl.addControllerInterface(factoryCtrlItf);
    factoryCtrl.addControllerInterface(allocatorCtrlItf);
    ((ControllerContainer) d).addController(factoryCtrl);

    // add a "nodata" node.
    final Data data = (Data) nodeFactoryItf.newNode("data", Data.class
        .getName());
    ((ImplementationContainer) d).setData(data);

    return d;
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
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return BindingControllerImplHelper.listFcHelper(super.listFc(),
        NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
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
    } else {
      super.unbindFc(itfName);
    }
  }
}
