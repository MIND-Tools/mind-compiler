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
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestADLAnnotationProcessor {

  private Loader loader;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new IDLFrontendModule(),
        new ADLFrontendModule() {
          protected void configureErrorLoader() {
            bind(Loader.class).annotatedWith(Names.named("ErrorLoader"))
                .toChainStartingWith(ErrorLoader.class)
                .endingWith(Loader.class);
          }
        });

    loader = injector.getInstance(Key.get(Loader.class,
        Names.named("ErrorLoader")));
    // ensure that phases are empty.
    FooProcessor.phases = new ArrayList<ProcessParams>();
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

        final String idlName = "pkg1.annotations.GeneratedIDL";
        final StringTemplate stIDL = getTemplate(
            "pkg1.annotations.ADLGenerator", "GenerateIDL");
        stIDL.setAttribute("idlName", idlName);

        loadIDLFromSource(idlName, stIDL.toString(), context);
        assertTrue(isIDLAlreadyGenerated(idlName, context));
        final IDL idl = idlLoaderItf.load(idlName, context);
        assertNotNull(idl);
        assertTrue(idl instanceof InterfaceDefinition);
        assertEquals(((InterfaceDefinition) idl).getMethods().length, 2);

      } else if (phase == ADLLoaderPhase.AFTER_CHECKING) {
        assertTrue(isAlreadyGenerated("pkg1.annotations.Generated", context));
        assertTrue(isIDLAlreadyGenerated("pkg1.annotations.GeneratedIDL",
            context));
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
