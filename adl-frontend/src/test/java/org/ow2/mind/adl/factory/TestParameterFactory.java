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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.objectweb.fractal.adl.merger.NodeMergerImpl;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.BasicADLLocator;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ASTChecker.ComponentChecker;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.factory.FactoryLoader;
import org.ow2.mind.adl.factory.FactoryTemplateInstantiator;
import org.ow2.mind.adl.factory.ParametricFactoryTemplateInstantiator;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.ExtendsGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricTemplateInstantiator;
import org.ow2.mind.adl.parser.ADLParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestParameterFactory {

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
    final ParametricDefinitionReferenceResolver pdrr = new ParametricDefinitionReferenceResolver();
    final GenericDefinitionReferenceResolver gdrr = new GenericDefinitionReferenceResolver();
    final ParametricGenericDefinitionReferenceResolver pgdrr = new ParametricGenericDefinitionReferenceResolver();
    final ImportDefinitionReferenceResolver idrr = new ImportDefinitionReferenceResolver();
    final CachingDefinitionReferenceResolver cdrr = new CachingDefinitionReferenceResolver();

    cdrr.clientResolverItf = idrr;
    idrr.clientResolverItf = pgdrr;
    pgdrr.clientResolverItf = gdrr;
    gdrr.clientResolverItf = pdrr;
    pdrr.clientResolverItf = bdrr;
    bdrr.loaderItf = cl;
    cdrr.loaderItf = cl;

    scrl.definitionReferenceResolverItf = cdrr;

    final ExtendsGenericDefinitionReferenceResolver egdrr = new ExtendsGenericDefinitionReferenceResolver();

    egdrr.clientResolverItf = cdrr;
    el.definitionReferenceResolverItf = egdrr;

    gdl.definitionReferenceResolverItf = cdrr;
    gdrr.recursiveResolverItf = cdrr;

    // template instantiator chain
    final TemplateInstantiatorImpl ti = new TemplateInstantiatorImpl();
    final FactoryTemplateInstantiator fti = new FactoryTemplateInstantiator();
    final ParametricTemplateInstantiator pti = new ParametricTemplateInstantiator();
    final ParametricFactoryTemplateInstantiator pfti = new ParametricFactoryTemplateInstantiator();
    final CachingTemplateInstantiator cti = new CachingTemplateInstantiator();

    cti.clientInstantiatorItf = pfti;
    pfti.clientInstantiatorItf = pti;
    pti.clientInstantiatorItf = fti;
    fti.clientInstantiatorItf = ti;

    ti.definitionReferenceResolverItf = cdrr;
    fti.definitionReferenceResolverItf = cdrr;
    cti.definitionCacheItf = cl;
    cti.definitionReferenceResolverItf = cdrr;
    pti.definitionReferenceResolverItf = cdrr;

    gdrr.templateInstantiatorItf = cti;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final BasicBindingChecker bindingChecker = new BasicBindingChecker();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();
    final NodeMergerImpl nodeMerger = new NodeMergerImpl();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    gdrr.bindingCheckerItf = bindingChecker;
    fl.nodeFactoryItf = nodeFactory;
    pti.nodeFactoryItf = nodeFactory;
    pti.nodeMergerItf = nodeMerger;
    pfti.nodeFactoryItf = nodeFactory;
    pfti.nodeMergerItf = nodeMerger;

    loader = cl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testParametricFactoryTemplateInstantiatorBC() throws Exception {
    checkBCImplementation(new ParametricFactoryTemplateInstantiator());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition d = loader.load(
        "pkg1.parametricFactory.ParametricFactory1", context);
    final ComponentChecker component = checker.assertDefinition(d)
        .containsComponent("factory");
    component.isReferencing("Factory<pkg1.parameter.Parameter1>").that()
        .containsArguments("InstantiatedDefinition$a").whereFirst().valueIs(10);

    component.isAnInstanceOf("Factory<pkg1.parameter.Parameter1>").that()
        .containsAttributes("a").whereFirst().valueReferences(
            "InstantiatedDefinition$a");
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition d = loader.load(
        "pkg1.parametricFactory.ParametricFactory2", context);
    final ComponentChecker component = checker.assertDefinition(d)
        .containsComponent("subComp").that().isAnInstanceOf(
            "pkg1.factory.GenericFactory1<pkg1.parameter.Parameter1>")
        .containsComponent("factory");

    component.isReferencing("Factory<pkg1.parameter.Parameter1>").that()
        .containsArguments("InstantiatedDefinition$a").whereFirst()
        .valueReferences("T$a");

    component.isAnInstanceOf("Factory<pkg1.parameter.Parameter1>").that()
        .containsAttributes("a").whereFirst().valueReferences(
            "InstantiatedDefinition$a");
  }

}
