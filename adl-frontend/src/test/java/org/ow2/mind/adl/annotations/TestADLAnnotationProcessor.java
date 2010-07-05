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

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.AnnotationProcessorTemplateInstantiator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.annotation.AnnotationProcessorLoader;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestADLAnnotationProcessor {

  private Loader loader;

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
    errorLoader.clientLoader = adlLoader;
    errorLoader.errorManagerItf = errorManager;
    loader = errorLoader;
    // ensure that phases are empty.
    FooProcessor.phases = new ArrayList<ProcessParams>();
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnnotationProcessorLoaderBC() throws Exception {
    checkBCImplementation(new AnnotationProcessorLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnnotationTemplateInstantiatorBC() throws Exception {
    checkBCImplementation(new AnnotationProcessorTemplateInstantiator());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    loader.load("pkg1.annotations.Foo", new HashMap<Object, Object>());
    assertEquals(FooProcessor.phases.size(), 2);
    assertPhase(ADLLoaderPhase.AFTER_PARSING);
    assertPhase(ADLLoaderPhase.AFTER_CHECKING);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    loader.load("pkg1.annotations.Generic1", new HashMap<Object, Object>());
    assertEquals(FooProcessor.phases.size(), 2);
    assertPhase(ADLLoaderPhase.AFTER_PARSING);
    assertPhase(ADLLoaderPhase.AFTER_CHECKING);
  }

  @Test(groups = {"functional"})
  public void test3() throws Exception {
    loader.load("pkg1.annotations.Composite1", new HashMap<Object, Object>());
    assertEquals(FooProcessor.phases.size(), 6);
    assertPhase(ADLLoaderPhase.ON_TEMPLATE_SUB_COMPONENT);
    assertPhase(ADLLoaderPhase.AFTER_TEMPLATE_INSTANTIATE);
  }

  protected static ProcessParams assertPhase(final ADLLoaderPhase phase) {
    for (final ProcessParams params : FooProcessor.phases) {
      if (params.phase == phase) return params;
    }
    fail("Phase " + phase + " not found.");
    return null;
  }

  public static class FooProcessor extends AbstractADLLoaderAnnotationProcessor {

    static List<ProcessParams> phases = new ArrayList<ProcessParams>();

    public Definition processAnnotation(final Annotation annotation,
        final Node node, final Definition definition,
        final ADLLoaderPhase phase, final Map<Object, Object> context)
        throws ADLException {
      assertNotNull(annotation);
      assertNotNull(node);
      assertNotNull(definition);
      assertNotNull(phase);
      phases.add(new ProcessParams(node, definition, phase));

      assertTrue(annotation instanceof FooAnnotation);

      assertNotNull(nodeFactoryItf);
      assertNotNull(nodeMergerItf);
      assertNotNull(definitionCacheItf);
      assertNotNull(loaderItf);
      assertNotNull(templateLoaderItf);

      if (phase == ADLLoaderPhase.AFTER_PARSING) {
        final String adlName = "pkg1.annotations.Generated";
        final StringTemplate st = getTemplate("pkg1.annotations.ADLGenerator",
            "Generate");
        st.setAttribute("adlName", adlName);

        loadFromSource(adlName, st.toString(), context);
        assertTrue(isAlreadyGenerated(adlName, context));
        final Definition d = loaderItf.load(adlName, context);
        final ASTChecker checker = new ASTChecker();
        checker.assertDefinition(d).containsInterfaces("itf").whereFirst()
            .isServer().hasSignature("pkg1.I2");
      } else if (phase == ADLLoaderPhase.AFTER_CHECKING) {
        assertTrue(isAlreadyGenerated("pkg1.annotations.Generated", context));
      }

      return null;
    }
  }

  static final class ProcessParams {
    final Node           node;
    final Definition     definition;
    final ADLLoaderPhase phase;

    ProcessParams(final Node node, final Definition definition,
        final ADLLoaderPhase phase) {
      this.node = node;
      this.definition = definition;
      this.phase = phase;
    }
  }

}
