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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.ow2.mind.CommonASTHelper;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.GenericASTHelper;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;
import org.ow2.mind.adl.parser.ADLParserContextHelper;
import org.ow2.mind.annotation.ast.AnnotationASTHelper;
import org.ow2.mind.annotation.ast.AnnotationContainer;
import org.ow2.mind.annotation.ast.AnnotationNode;

import com.google.inject.Inject;

/**
 * This loader component creates the AST of the special "Factory" definition.
 */
public class FactoryLoader extends AbstractDelegatingLoader {

  public static final String  FORMAL_TYPE_PARAMETER_NAME         = "InstantiatedDefinition";
  public static final String  FACTORY_DEFINITION_NAME            = "Factory";
  public static final String  FACTORY_CONTROLLED_DEFINITION_NAME = "FactoryWithCtrl";
  private static final String FACTORY_CONTROLLER                 = "FactoryController";
  private static final String FACTORY_SIGNATURE                  = "fractal.api.Factory";
  private static final String FACTORY_ITF_NAME                   = "factory";
  private static final String ALLOCATOR_SIGNATURE                = "memory.api.Allocator";
  private static final String ALLOCATOR_ITF_NAME                 = "allocator";

  @Inject
  protected NodeFactory       nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    if (FACTORY_DEFINITION_NAME.equals(name)) {
      ADLParserContextHelper.registerADL(createFactory(context), context);
    } else if (FACTORY_CONTROLLED_DEFINITION_NAME.equals(name)) {
      ADLParserContextHelper.registerADL(createFactoryWithCtrl(context),
          context);
    }
    return clientLoader.load(name, context);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Definition createFactory(final Map<Object, Object> context) {
    final Definition d = CommonASTHelper.newNode(nodeFactoryItf, "definition",
        Definition.class, FormalTypeParameterContainer.class,
        InterfaceContainer.class, ImplementationContainer.class,
        ControllerContainer.class, AnnotationContainer.class);
    d.setName(FACTORY_DEFINITION_NAME);

    final FormalTypeParameter typeParameter = GenericASTHelper
        .newFormalTypeParameter(nodeFactoryItf, FORMAL_TYPE_PARAMETER_NAME,
            null);
    typeParameter.setName(FORMAL_TYPE_PARAMETER_NAME);
    ((FormalTypeParameterContainer) d).addFormalTypeParameter(typeParameter);

    final MindInterface factoryItf = ASTHelper.newServerInterfaceNode(
        nodeFactoryItf, FACTORY_ITF_NAME, FACTORY_SIGNATURE);
    ((InterfaceContainer) d).addInterface(factoryItf);

    final MindInterface allocatorItf = ASTHelper.newClientInterfaceNode(
        nodeFactoryItf, ALLOCATOR_ITF_NAME, ALLOCATOR_SIGNATURE);
    ((InterfaceContainer) d).addInterface(allocatorItf);

    final Source factoryCtrlSrc = ASTHelper.newSource(nodeFactoryItf);
    factoryCtrlSrc.setPath(FACTORY_CONTROLLER);

    final ControllerInterface factoryCtrlItf = MembraneASTHelper
        .newControllerInterfaceNode(nodeFactoryItf, FACTORY_ITF_NAME, false);
    final ControllerInterface allocatorCtrlItf = MembraneASTHelper
        .newControllerInterfaceNode(nodeFactoryItf, ALLOCATOR_ITF_NAME, false);

    final Controller factoryCtrl = MembraneASTHelper
        .newControllerNode(nodeFactoryItf);
    factoryCtrl.addSource(factoryCtrlSrc);
    factoryCtrl.addControllerInterface(factoryCtrlItf);
    factoryCtrl.addControllerInterface(allocatorCtrlItf);
    ((ControllerContainer) d).addController(factoryCtrl);

    return d;
  }

  protected Definition createFactoryWithCtrl(final Map<Object, Object> context) {
    final Definition d = createFactory(context);
    d.setName(FACTORY_CONTROLLED_DEFINITION_NAME);
    final AnnotationNode componentCtrl = AnnotationASTHelper.newAnnotationNode(
        nodeFactoryItf, "controller.Component");
    ((AnnotationContainer) d).addAnnotation(componentCtrl);
    final AnnotationNode bcCtrl = AnnotationASTHelper.newAnnotationNode(
        nodeFactoryItf, "controller.BindingController");
    ((AnnotationContainer) d).addAnnotation(bcCtrl);

    return d;
  }
}
