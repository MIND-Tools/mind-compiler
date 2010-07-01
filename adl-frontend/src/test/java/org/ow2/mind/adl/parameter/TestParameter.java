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
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestParameter {

  Loader              loader;

  Map<Object, Object> context;

  ASTChecker          checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();

    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final SubComponentResolverLoader scrl = new SubComponentResolverLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final CacheLoader cl = new CacheLoader();
    final ErrorLoader errl = new ErrorLoader();

    errl.clientLoader = cl;
    cl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = adlLoader;

    errl.errorManagerItf = errorManager;
    cl.errorManagerItf = errorManager;
    el.errorManagerItf = errorManager;
    scrl.errorManagerItf = errorManager;
    adlLoader.errorManagerItf = errorManager;

    // definition reference resolver chain
    final BasicDefinitionReferenceResolver bdrr = new BasicDefinitionReferenceResolver();
    final ParametricDefinitionReferenceResolver pdrr = new ParametricDefinitionReferenceResolver();
    final ImportDefinitionReferenceResolver idrr = new ImportDefinitionReferenceResolver();
    final CachingDefinitionReferenceResolver cdrr = new CachingDefinitionReferenceResolver();

    cdrr.clientResolverItf = idrr;
    idrr.clientResolverItf = pdrr;
    pdrr.clientResolverItf = bdrr;
    bdrr.loaderItf = cl;
    cdrr.loaderItf = cl;

    scrl.definitionReferenceResolverItf = cdrr;

    bdrr.errorManagerItf = errorManager;
    pdrr.errorManagerItf = errorManager;

    final ExtendsParametricDefinitionReferenceResolver epdrr = new ExtendsParametricDefinitionReferenceResolver();

    epdrr.clientResolverItf = cdrr;
    el.definitionReferenceResolverItf = epdrr;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    bdrr.nodeFactoryItf = nodeFactory;

    loader = errl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testExtendsParametricDefinitionReferenceResolverBC()
      throws Exception {
    checkBCImplementation(new ExtendsParametricDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testParametricAnonymousDefinitionResolverBC() throws Exception {
    checkBCImplementation(new ParametricAnonymousDefinitionExtractor());
  }

  @Test(groups = {"functional", "checkin"})
  public void testParametricDefinitionReferenceResolverBC() throws Exception {
    checkBCImplementation(new ParametricDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testParametricTemplateInstantiatorBC() throws Exception {
    checkBCImplementation(new ParametricTemplateInstantiator());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition content = loader
        .load("pkg1.parameter.Parameter1", context);

    checker.assertDefinition(content).containsFormalParameters("a");

    checker.assertDefinition(content).containsAttributes("attr1").whereFirst()
        .valueReferences("a");
  }

  @Test(groups = {"functional", "checkin"})
  public void test2() throws Exception {
    final Definition content = loader
        .load("pkg1.parameter.Parameter2", context);

    checker.assertDefinition(content).containsComponent("subComp1")
        .isAnInstanceOf("pkg1.parameter.Parameter1")
        .containsAttributes("attr1").whereFirst().valueReferences("a");
  }
}
