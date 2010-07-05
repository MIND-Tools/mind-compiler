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

package org.ow2.mind.adl.annotations;

import static org.ow2.mind.adl.membrane.DefaultControllerInterfaceConstants.BINDING_CONTROLLER;
import static org.ow2.mind.adl.membrane.DefaultControllerInterfaceConstants.BINDING_CONTROLLER_SIGNATURE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.annotations.controller.BindingController;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.membrane.ControllerInterfaceDecorationHelper;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.annotation.AnnotationLocatorHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBindingController {

  private Loader              loader;
  private ASTChecker          astChecker;
  private Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();

    // input locators
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final IDLLocator idlLocator = IDLLoaderChainFactory
        .newIDLLocator(inputResourceLocator);
    final ADLLocator adlLocator = Factory.newADLLocator(inputResourceLocator);
    final ImplementationLocator implementationLocator = Factory
        .newImplementationLocator(inputResourceLocator);

    // Plugin Manager Components
    final org.objectweb.fractal.adl.Factory pluginFactory = new SimpleClassPluginFactory();

    // loader chains
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader(errorManager,
        idlLocator, inputResourceLocator);
    final Loader adlLoader = Factory.newLoader(errorManager,
        inputResourceLocator, adlLocator, idlLocator, implementationLocator,
        idlLoader, pluginFactory);
    final ErrorLoader errorLoader = new ErrorLoader();
    errorLoader.errorManagerItf = errorManager;
    errorLoader.clientLoader = adlLoader;
    loader = errorLoader;

    context = new HashMap<Object, Object>();
    AnnotationLocatorHelper.addDefaultAnnotationPackage(
        "org.ow2.mind.adl.annotation.predefined", context);
    AnnotationLocatorHelper.addDefaultAnnotationPackage(
        "org.ow2.mind.adl.annotations", context);

    astChecker = new ASTChecker();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition d = loader.load("pkg1.controller.binding.Type1", context);
    checkBC(d);
  }

  @Test(groups = {"functional"})
  public void test11() throws Exception {
    final Definition d = loader.load("pkg1.controller.binding.Primitive1",
        context);
    checkBC(d);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition d = loader.load("pkg1.controller.binding.Type2", context);
    checkBC(d);
  }

  @Test(groups = {"functional"})
  public void test21() throws Exception {
    final Definition d = loader.load("pkg1.controller.binding.Primitive2",
        context);
    checkBC(d);
  }

  private void checkBC(final Definition d) {
    assertNotNull(AnnotationHelper.getAnnotation(d, BindingController.class));

    astChecker.assertDefinition(d).containsInterface(BINDING_CONTROLLER)
        .isServer().isMandatory().hasSignature(BINDING_CONTROLLER_SIGNATURE);

    assertTrue(d instanceof ControllerContainer);
    final Controller[] ctrls = ((ControllerContainer) d).getControllers();
    assertEquals(ctrls.length, 1);
    final Controller ctrl = ctrls[0];
    assertNotNull(ctrl);
    final ControllerInterface[] ctrlItfs = ctrl.getControllerInterfaces();
    assertEquals(ctrlItfs.length, 1);
    final ControllerInterface ctrlItf = ctrlItfs[0];
    assertNotNull(ctrlItf);
    assertEquals(ctrlItf.getName(), BINDING_CONTROLLER);
    assertNull(ctrlItf.getIsInternal());
    assertSame(
        ControllerInterfaceDecorationHelper.getReferencedInterface(ctrlItf),
        ASTHelper.getInterface(d, BINDING_CONTROLLER));
  }

}
