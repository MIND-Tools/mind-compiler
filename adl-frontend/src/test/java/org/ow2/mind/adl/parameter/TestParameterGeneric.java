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

package org.ow2.mind.adl.parameter;

import static org.ow2.mind.BCImplChecker.checkBCImplementation;

import java.util.HashMap;

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
import org.ow2.mind.adl.GraphChecker;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ExtendsParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricTemplateInstantiator;
import org.ow2.mind.adl.parser.ADLParser;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestParameterGeneric {

  Loader                  loader;

  HashMap<Object, Object> context;

  ASTChecker              checker;
  GraphChecker            graphChecker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final GenericDefinitionLoader gdl = new GenericDefinitionLoader();
    final SubComponentResolverLoader scrl = new SubComponentResolverLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final CacheLoader cl = new CacheLoader();

    cl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = gdl;
    gdl.clientLoader = adlLoader;

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

    final ExtendsParametricDefinitionReferenceResolver epdrr = new ExtendsParametricDefinitionReferenceResolver();

    epdrr.clientResolverItf = cdrr;
    el.definitionReferenceResolverItf = epdrr;

    gdl.definitionReferenceResolverItf = cdrr;
    gdrr.recursiveResolverItf = cdrr;

    // template instantiator chain
    final TemplateInstantiatorImpl ti = new TemplateInstantiatorImpl();
    final ParametricTemplateInstantiator pti = new ParametricTemplateInstantiator();
    final CachingTemplateInstantiator cti = new CachingTemplateInstantiator();

    cti.clientInstantiatorItf = pti;
    pti.clientInstantiatorItf = ti;

    cti.definitionCacheItf = cl;
    cti.definitionReferenceResolverItf = cdrr;
    pti.definitionReferenceResolverItf = cdrr;
    ti.definitionReferenceResolverItf = cdrr;

    gdrr.templateInstantiatorItf = cti;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();
    final NodeMergerImpl nodeMerger = new NodeMergerImpl();
    final BasicBindingChecker bindingChecker = new BasicBindingChecker();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    gdrr.bindingCheckerItf = bindingChecker;
    pti.nodeFactoryItf = nodeFactory;
    pti.nodeMergerItf = nodeMerger;

    loader = cl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
    graphChecker = new GraphChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testParametricGenericDefinitionReferenceResolverBC()
      throws Exception {
    checkBCImplementation(new ParametricGenericDefinitionReferenceResolver());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition content = loader.load("pkg1.parameterGeneric.Composite1",
        context);

    checker.assertDefinition(content).containsFormalParameters(/* no FP */);
    final DefinitionChecker g4 = checker.assertDefinition(content)
        .containsComponents("c1", "c2", "c3")

        .whereFirst().isAnInstanceOf("pkg1.parameter.Parameter1")

        .andNext().isAnInstanceOf("pkg1.parameter.Parameter1")

        .andNext().that().isAnInstanceOf(
            "pkg1.generic.Generic4<pkg1.parameter.Parameter1>");

    g4.containsFormalParameter("W$a");

    g4
        .containsComponents("c1")
        .whereFirst()
        .that()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.generic.Generic1<pkg1.parameter.Parameter1>,pkg1.generic.Generic1<pkg1.parameter.Parameter1>>")
        .containsComponents("c1", "c2").whereFirst().that().isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.parameter.Parameter1>")
        .containsFormalParameter("T$a");

  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition content = loader.load("pkg1.parameterGeneric.Composite2",
        context);
    checker.assertDefinition(content).containsFormalParameters(/* no FP */);
    checker.assertDefinition(content).containsComponents("c1", "c2", "c3",
        "c4", "c5")

    .whereFirst()/* c1 */.isAnInstanceOf("pkg1.parameter.Parameter1")

    .andNext()/* c2 */.isAnInstanceOf("pkg1.parameter.Parameter1")

    .andNext()/* c3 */.isAnInstanceOf(
        "pkg1.generic.Generic4<pkg1.parameter.Parameter1>")

    .andNext()/* c4 */.isAnInstanceOf(
        "pkg1.generic.Generic1<pkg1.parameter.Parameter1>")

    .andNext()/* c5 */.isAnInstanceOf("pkg1.parameterGeneric.Composite1");
  }
}
