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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestADLAnnotationProcessor {

  private Loader loader;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
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
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader(idlLocator,
        inputResourceLocator);
    final Loader adlLoader = Factory
        .newLoader(inputResourceLocator, adlLocator, idlLocator,
            implementationLocator, idlLoader, pluginFactory);
    loader = adlLoader;
    // ensure that phases are empty.
    FooProcessor.phases = new HashSet<ADLLoaderPhase>();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    loader.load("pkg1.annotations.Foo", new HashMap<Object, Object>());
    assertTrue(FooProcessor.phases.contains(ADLLoaderPhase.AFTER_PARSING));
    assertTrue(FooProcessor.phases.contains(ADLLoaderPhase.AFTER_CHECKING));
  }

  public static class FooProcessor extends AbstractADLLoaderAnnotationProcessor {

    public static Set<ADLLoaderPhase> phases = new HashSet<ADLLoaderPhase>();

    public Definition processAnnotation(final Annotation annotation,
        final Node node, final Definition definition,
        final ADLLoaderPhase phase, final Map<Object, Object> context)
        throws ADLException {
      assertNotNull(annotation);
      assertNotNull(node);
      assertNotNull(definition);
      assertNotNull(phase);
      phases.add(phase);

      assertTrue(annotation instanceof FooAnnotation);
      assertTrue(node instanceof Source);

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
      } else {
        assertSame(phase, ADLLoaderPhase.AFTER_CHECKING);
        assertTrue(isAlreadyGenerated("pkg1.annotations.Generated", context));
      }

      return null;
    }
  }
}
