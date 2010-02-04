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

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import java.util.HashMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.BasicADLLocator;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.ExtendsGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.GenericAnonymousDefinitionExtractor;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.NoAnySubComponentLoader;
import org.ow2.mind.adl.generic.NoAnyTypeArgumentDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstanceLoader;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGeneric {

  Loader                  loader;

  Instantiator            instantiator;

  HashMap<Object, Object> context;

  ASTChecker              checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final GenericDefinitionLoader gdl = new GenericDefinitionLoader();
    final SubComponentResolverLoader scrl = new SubComponentResolverLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final NoAnySubComponentLoader nascl = new NoAnySubComponentLoader();
    final CacheLoader cl = new CacheLoader();

    cl.clientLoader = nascl;
    nascl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = gdl;
    gdl.clientLoader = adlLoader;

    // definition reference resolver chain
    final BasicDefinitionReferenceResolver bdrr = new BasicDefinitionReferenceResolver();
    final GenericDefinitionReferenceResolver gdrr = new GenericDefinitionReferenceResolver();
    final ImportDefinitionReferenceResolver idrr = new ImportDefinitionReferenceResolver();
    final CachingDefinitionReferenceResolver cdrr = new CachingDefinitionReferenceResolver();

    cdrr.clientResolverItf = idrr;
    idrr.clientResolverItf = gdrr;
    gdrr.clientResolverItf = bdrr;
    gdrr.recursiveResolverItf = cdrr;
    bdrr.loaderItf = cl;
    cdrr.loaderItf = cl;

    final NoAnyTypeArgumentDefinitionReferenceResolver natadrr = new NoAnyTypeArgumentDefinitionReferenceResolver();

    natadrr.clientResolverItf = cdrr;
    scrl.definitionReferenceResolverItf = natadrr;

    final ExtendsGenericDefinitionReferenceResolver egdrr = new ExtendsGenericDefinitionReferenceResolver();

    egdrr.clientResolverItf = cdrr;
    el.definitionReferenceResolverItf = egdrr;

    gdl.definitionReferenceResolverItf = cdrr;

    // template instantiator chain
    final TemplateInstantiatorImpl ti = new TemplateInstantiatorImpl();
    final CachingTemplateInstantiator cti = new CachingTemplateInstantiator();

    cti.clientInstantiatorItf = ti;

    ti.definitionReferenceResolverItf = cdrr;
    cti.definitionCacheItf = cl;
    cti.definitionReferenceResolverItf = cdrr;

    gdrr.templateInstantiatorItf = cti;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final BasicBindingChecker bindingChecker = new BasicBindingChecker();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    gdrr.bindingCheckerItf = bindingChecker;

    loader = cl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testCachingTemplateInstantiatorBC() throws Exception {
    checkBCImplementation(new CachingTemplateInstantiator());
  }

  @Test(groups = {"functional", "checkin"})
  public void testExtendsGenericDefinitionReferenceResolverBC()
      throws Exception {
    checkBCImplementation(new ExtendsGenericDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testGenericAnonymousDefinitionResolverBC() throws Exception {
    checkBCImplementation(new GenericAnonymousDefinitionExtractor());
  }

  @Test(groups = {"functional", "checkin"})
  public void testGenericDefinitionReferenceResolverBC() throws Exception {
    checkBCImplementation(new GenericDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testNoAnySubComponentLoaderBC() throws Exception {
    checkBCImplementation(new NoAnySubComponentLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testNoAnyTypeArgumentDefinitionReferenceResolverBC()
      throws Exception {
    checkBCImplementation(new NoAnyTypeArgumentDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testTemaplateInstanceLoaderBC() throws Exception {
    checkBCImplementation(new TemplateInstanceLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testTemplateInstantiatorImplBC() throws Exception {
    checkBCImplementation(new TemplateInstantiatorImpl());
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
        .whereFirst().conformsTo("pkg1.pkg2.Type1").andNext().conformsTo(
            "pkg1.pkg2.Type1");

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

    checker.assertDefinition(content)
        .containsComponents("c1", "c2", "c3", "c4").whereFirst()
        .referencesFormalTypeParameter("W").andNext().isAnInstanceOf(
            "pkg1.pkg2.Primitive1").andNext()
        .referencesFormalTypeParameter("W").andNext().isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.pkg2.Type1,pkg1.pkg2.Primitive1>");

    final DefinitionChecker defC4 = checker.assertDefinition(content)
        .containsComponent("c4").isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.pkg2.Type1,pkg1.pkg2.Primitive1>");

    defC4.containsFormalTypeParameters("U", "V").whereFirst().conformsTo(
        "pkg1.pkg2.Type1").andNext().conformsTo("pkg1.pkg2.Type1");
    defC4.containsComponents("c1", "c2").whereFirst()
        .referencesFormalTypeParameter("U").andNext().isAnInstanceOf(
            "pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test31() throws Exception {
    final Definition content = loader.load("pkg1.generic.Composite3", context);

    checker.assertDefinition(content)
        .containsFormalTypeParameters(/* no FTP */);

    checker.assertDefinition(content).containsComponents("c1", "c2", "c3",
        "c4", "c5").whereFirst().isAnInstanceOf("pkg1.Composite1").andNext()
        .isAnInstanceOf("pkg1.pkg2.Primitive1").andNext().isAnInstanceOf(
            "pkg1.Composite1").andNext().isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.Composite1,pkg1.pkg2.Primitive1>")
        .andNext()
        .isAnInstanceOf("pkg1.generic.Generic3<pkg1.pkg2.Primitive1>");

    final DefinitionChecker defC4 = checker.assertDefinition(content)
        .containsComponent("c4").isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.Composite1,pkg1.pkg2.Primitive1>");

    defC4.containsComponents("c1", "c2").whereFirst().isAnInstanceOf(
        "pkg1.Composite1").andNext().isAnInstanceOf("pkg1.pkg2.Primitive1");

    final DefinitionChecker defC5 = checker.assertDefinition(content)
        .containsComponent("c5").isAnInstanceOf(
            "pkg1.generic.Generic3<pkg1.pkg2.Primitive1>");

    defC5.containsComponents("c1", "c2", "c3", "c4").whereFirst()
        .isAnInstanceOf("pkg1.pkg2.Primitive1").andNext().isAnInstanceOf(
            "pkg1.pkg2.Primitive1").andNext().isAnInstanceOf(
            "pkg1.pkg2.Primitive1").andNext().isAnInstanceOf(
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
        .containsComponents("c1", "c2").whereFirst().isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.pkg2.Type1>").andNext().isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.pkg2.Type1>");
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
        .containsComponents("c1", "c2").whereFirst().isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.pkg2.Primitive1>").andNext()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.pkg2.Primitive1>");
  }

  @Test(groups = {"functional"})
  public void test5() throws Exception {
    try {
      loader.load("pkg1.generic.Composite5", context);
      fail("An ADLException was expected here");
    } catch (final ADLException e) {
      assertSame(e.getError().getTemplate(),
          ADLErrors.INVALID_TEMPLATE_VALUE_TYPE_DEFINITON);
    }

  }

  @Test(groups = {"functional"})
  public void testCycle() throws Exception {
    try {
      loader.load("pkg1.generic.Cycle1", context);
      fail("ADLException was expected here");
    } catch (final ADLException e) {
      assertEquals(e.getError().getTemplate(), ComponentErrors.DEFINITION_CYCLE);
    }
  }

}
