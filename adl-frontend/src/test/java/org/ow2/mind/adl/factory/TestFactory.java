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

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.BasicADLLocator;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.factory.FactoryLoader;
import org.ow2.mind.adl.factory.FactoryTemplateInstantiator;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.ExtendsGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestFactory {

  Loader              loader;

  Map<Object, Object> context;

  ASTChecker          checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final FactoryLoader fl = new FactoryLoader();
    final GenericDefinitionLoader gdl = new GenericDefinitionLoader();
    final SubComponentResolverLoader scrl = new SubComponentResolverLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final CacheLoader cl = new CacheLoader();

    cl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = gdl;
    gdl.clientLoader = fl;
    fl.clientLoader = adlLoader;

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

    scrl.definitionReferenceResolverItf = cdrr;

    final ExtendsGenericDefinitionReferenceResolver egdrr = new ExtendsGenericDefinitionReferenceResolver();

    egdrr.clientResolverItf = cdrr;
    el.definitionReferenceResolverItf = egdrr;

    gdl.definitionReferenceResolverItf = cdrr;

    // template instantiator chain
    final TemplateInstantiatorImpl ti = new TemplateInstantiatorImpl();
    final FactoryTemplateInstantiator fti = new FactoryTemplateInstantiator();
    final CachingTemplateInstantiator cti = new CachingTemplateInstantiator();

    cti.clientInstantiatorItf = fti;
    fti.clientInstantiatorItf = ti;

    ti.definitionReferenceResolverItf = cdrr;
    fti.definitionReferenceResolverItf = cdrr;
    cti.definitionCacheItf = cl;
    cti.definitionReferenceResolverItf = cdrr;

    gdrr.templateInstantiatorItf = cti;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final BasicBindingChecker bindingChecker = new BasicBindingChecker();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    gdrr.bindingCheckerItf = bindingChecker;
    fl.nodeFactoryItf = nodeFactory;

    loader = cl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testFactoryLoaderBC() throws Exception {
    checkBCImplementation(new FactoryLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testFactoryTemplateInstantiatorBC() throws Exception {
    checkBCImplementation(new FactoryTemplateInstantiator());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition d = loader.load("pkg1.factory.Factory1", context);
    final Definition def = checker.assertDefinition(d).containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.Composite1>").def;

    checker.assertDefinition(def).containsInterfaces("factory", "allocator")
        .whereFirst().isServer().hasSignature("fractal.api.Factory").andNext()
        .isClient().hasSignature("memory.api.Allocator");

    final Definition instantiatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(def, null, null);
    assertNotNull(instantiatedDefinition);
    assertEquals(instantiatedDefinition.getName(), "pkg1.Composite1");
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition composite1Def = loader.load("pkg1.Composite1", context);
    final Definition d = loader.load("pkg1.factory.Factory1", context);
    final Definition def = checker.assertDefinition(d).containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.Composite1>").def;
    final Definition instantiatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(def, null, null);
    assertSame(instantiatedDefinition, composite1Def);
  }

  @Test(groups = {"functional"})
  public void test3() throws Exception {
    final Definition d = loader.load("pkg1.factory.GenericFactory1", context);
    final Definition def = checker.assertDefinition(d).containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.pkg2.Type1>").def;

    checker.assertDefinition(def).containsInterfaces("factory", "allocator")
        .whereFirst().isServer().hasSignature("fractal.api.Factory").andNext()
        .isClient().hasSignature("memory.api.Allocator");
  }

  @Test(groups = {"functional"})
  public void test4() throws Exception {
    final Definition d = loader.load("pkg1.factory.Factory2", context);
    final Definition def = checker.assertDefinition(d).containsComponent(
        "subComp").isAnInstanceOf(
        "pkg1.factory.GenericFactory1<pkg1.Composite1>").containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.Composite1>").def;

    final Definition instantiatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(def, null, null);
    assertNotNull(instantiatedDefinition);
    assertEquals(instantiatedDefinition.getName(), "pkg1.Composite1");
  }

  @Test(groups = {"functional"})
  public void test5() throws Exception {
    final Definition d1 = loader.load("pkg1.factory.Factory1", context);
    final Definition def1 = checker.assertDefinition(d1).containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.Composite1>").def;
    assertNotNull(def1);

    final Definition d2 = loader.load("pkg1.factory.Factory2", context);
    final Definition def2 = checker.assertDefinition(d2).containsComponent(
        "subComp").isAnInstanceOf(
        "pkg1.factory.GenericFactory1<pkg1.Composite1>").containsComponent(
        "factory").isAnInstanceOf("Factory<pkg1.Composite1>").def;
    assertNotNull(def2);

    assertSame(def1, def2);
  }

}
