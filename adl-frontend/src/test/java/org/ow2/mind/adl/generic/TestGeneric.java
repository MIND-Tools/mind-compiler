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

package org.ow2.mind.adl.generic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionCache;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.binding.BindingChecker;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class TestGeneric {

  Loader                  loader;

  Instantiator            instantiator;

  HashMap<Object, Object> context;

  ASTChecker              checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {

    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new AbstractADLFrontendModule() {

          protected void configureTest() {
            bind(Loader.class).toChainStartingWith(ErrorLoader.class)
                .followedBy(CacheLoader.class)
                .followedBy(NoAnySubComponentLoader.class)
                .followedBy(ExtendsLoader.class)
                .followedBy(SubComponentResolverLoader.class)
                .followedBy(GenericDefinitionLoader.class)
                .endingWith(ADLParser.class);

            bind(DefinitionReferenceResolver.class)
                .toChainStartingWith(CachingDefinitionReferenceResolver.class)
                .followedBy(ImportDefinitionReferenceResolver.class)
                .followedBy(GenericDefinitionReferenceResolver.class)
                .endingWith(BasicDefinitionReferenceResolver.class);

            bind(DefinitionReferenceResolver.class)
                .annotatedWith(
                    Names.named(ExtendsLoader.EXTENDS_DEFINITION_RESOLVER))
                .toChainStartingWith(
                    ExtendsGenericDefinitionReferenceResolver.class)
                .endingWith(DefinitionReferenceResolver.class);

            bind(NodeMerger.class).annotatedWith(
                Names.named(ExtendsLoader.EXTENDS_NODE_MERGER)).to(
                STCFNodeMerger.class);

            bind(DefinitionReferenceResolver.class)
                .annotatedWith(
                    Names
                        .named(SubComponentResolverLoader.SUB_COMPONENT_DEFINITION_RESOLVER))
                .toChainStartingWith(
                    NoAnyTypeArgumentDefinitionReferenceResolver.class)
                .endingWith(DefinitionReferenceResolver.class);

            bind(TemplateInstantiator.class).toChainStartingWith(
                CachingTemplateInstantiator.class).endingWith(
                TemplateInstantiatorImpl.class);

            bind(BindingChecker.class).to(BasicBindingChecker.class);
            bind(DefinitionCache.class).to(CacheLoader.class);
          }

        });

    loader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition content = loader.load("pkg1.generic.Generic1", context);
    checker.assertDefinition(content).containsFormalTypeParameters("T")
        .whereFirst().conformsTo("pkg1.pkg2.Type1");

    checker.assertDefinition(content).containsComponents("subComp1")
        .whereFirst().referencesFormalTypeParameter("T");
  }

  @Test(groups = {"functional"})
  public void test11() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite1", context);

    checker.assertDefinition(content).containsComponents("subComp1")
        .whereFirst().isAnInstanceOf("pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition content = loader.load("pkg1.generic.Generic2", context);

    checker.assertDefinition(content).containsFormalTypeParameters("U", "V")
        .whereFirst().conformsTo("pkg1.pkg2.Type1").andNext()
        .conformsTo("pkg1.pkg2.Type1");

    checker.assertDefinition(content).containsComponents("c1", "c2")
        .whereFirst().referencesFormalTypeParameter("U").andNext()
        .referencesFormalTypeParameter("V");
  }

  @Test(groups = {"functional"})
  public void test21() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite2", context);
    checker.assertDefinition(content)
        .containsFormalTypeParameters(/* no FTP */);

    checker.assertDefinition(content).containsComponents("c1", "c2")
        .whereFirst().isAnInstanceOf("pkg1.pkg2.Primitive1").andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test221() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite21", context);

    checker.assertDefinition(content)
        .containsFormalTypeParameters(/* no FTP */);

    checker.assertDefinition(content).containsComponents("c1", "c2")
        .whereFirst().isAnInstanceOf("pkg1.Composite1").andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test3() throws Exception {
    final Definition content = loader.load("pkg1.generic.Generic3", context);

    checker.assertDefinition(content).containsFormalTypeParameters("W")
        .whereFirst().conformsTo("pkg1.pkg2.Type1");

    checker
        .assertDefinition(content)
        .containsComponents("c1", "c2", "c3", "c4")
        .whereFirst()
        .referencesFormalTypeParameter("W")
        .andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1")
        .andNext()
        .referencesFormalTypeParameter("W")
        .andNext()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.pkg2.Type1,pkg1.pkg2.Primitive1>");

    final DefinitionChecker defC4 = checker
        .assertDefinition(content)
        .containsComponent("c4")
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.pkg2.Type1,pkg1.pkg2.Primitive1>");

    defC4.containsFormalTypeParameters("U", "V").whereFirst()
        .conformsTo("pkg1.pkg2.Type1").andNext().conformsTo("pkg1.pkg2.Type1");
    defC4.containsComponents("c1", "c2").whereFirst()
        .referencesFormalTypeParameter("U").andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test31() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite3", context);

    checker.assertDefinition(content)
        .containsFormalTypeParameters(/* no FTP */);

    checker
        .assertDefinition(content)
        .containsComponents("c1", "c2", "c3", "c4", "c5")
        .whereFirst()
        .isAnInstanceOf("pkg1.Composite1")
        .andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1")
        .andNext()
        .isAnInstanceOf("pkg1.Composite1")
        .andNext()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.Composite1,pkg1.pkg2.Primitive1>")
        .andNext()
        .isAnInstanceOf("pkg1.generic.Generic3<pkg1.pkg2.Primitive1>");

    final DefinitionChecker defC4 = checker
        .assertDefinition(content)
        .containsComponent("c4")
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.Composite1,pkg1.pkg2.Primitive1>");

    defC4.containsComponents("c1", "c2").whereFirst()
        .isAnInstanceOf("pkg1.Composite1").andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1");

    final DefinitionChecker defC5 = checker.assertDefinition(content)
        .containsComponent("c5")
        .isAnInstanceOf("pkg1.generic.Generic3<pkg1.pkg2.Primitive1>");

    defC5
        .containsComponents("c1", "c2", "c3", "c4")
        .whereFirst()
        .isAnInstanceOf("pkg1.pkg2.Primitive1")
        .andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1")
        .andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1")
        .andNext()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.pkg2.Primitive1,pkg1.pkg2.Primitive1>");
  }

  @Test(groups = {"functional"})
  public void test4() throws Exception {
    final Definition content = loader.load("pkg1.generic.Generic4", context);

    checker.assertDefinition(content).containsFormalTypeParameters("W")
        .whereFirst().conformsTo("pkg1.pkg2.Type1");

    checker
        .assertDefinition(content)
        .containsComponents("c1")
        .whereFirst()
        .that()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.generic.Generic1<pkg1.pkg2.Type1>,pkg1.generic.Generic1<pkg1.pkg2.Type1>>")
        .containsComponents("c1", "c2").whereFirst()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.pkg2.Type1>").andNext()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.pkg2.Type1>");
  }

  @Test(groups = {"functional"})
  public void test41() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite4", context);

    checker.assertDefinition(content)
        .containsFormalTypeParameters(/* no FTP */);

    checker
        .assertDefinition(content)
        .containsComponents("c1")
        .whereFirst()
        .that()
        .isAnInstanceOf("pkg1.generic.Generic4<pkg1.pkg2.Primitive1>")
        .containsComponents("c1")
        .whereFirst()
        .that()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.generic.Generic1<pkg1.pkg2.Primitive1>,pkg1.generic.Generic1<pkg1.pkg2.Primitive1>>")
        .containsComponents("c1", "c2").whereFirst()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.pkg2.Primitive1>")
        .andNext()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.pkg2.Primitive1>");
  }

  @Test(groups = {"functional"})
  public void test5() throws Exception {
    try {
      loader.load("pkg1.generic.Composite5", context);
      fail("An ADLException was expected here");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 1);
      final Error err = errors.iterator().next();
      assertSame(err.getTemplate(),
          ADLErrors.INVALID_TEMPLATE_VALUE_TYPE_DEFINITON);
    }

  }

  @Test(groups = {"functional"})
  public void testCycle() throws Exception {
    try {
      loader.load("pkg1.generic.Cycle1", context);
      fail("ADLException was expected here");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 2);
      final Error err = errors.iterator().next();
      assertEquals(err.getTemplate(), ComponentErrors.DEFINITION_CYCLE);
    }
  }

}
