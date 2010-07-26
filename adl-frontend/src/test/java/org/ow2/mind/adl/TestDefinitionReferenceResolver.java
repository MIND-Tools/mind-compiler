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

package org.ow2.mind.adl;

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestDefinitionReferenceResolver {

  Loader                  loader;

  Instantiator            instantiator;

  HashMap<Object, Object> context;

  ASTChecker              checker;

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

    loader = errl;
    errl.clientLoader = cl;
    cl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = adlLoader;

    adlLoader.errorManagerItf = errorManager;
    scrl.errorManagerItf = errorManager;
    el.errorManagerItf = errorManager;
    errl.errorManagerItf = errorManager;

    // definition reference resolver chain
    final BasicDefinitionReferenceResolver bdrr = new BasicDefinitionReferenceResolver();
    final ImportDefinitionReferenceResolver idrr = new ImportDefinitionReferenceResolver();
    final CachingDefinitionReferenceResolver cdrr = new CachingDefinitionReferenceResolver();

    cdrr.clientResolverItf = idrr;
    idrr.clientResolverItf = bdrr;
    bdrr.loaderItf = cl;
    cdrr.loaderItf = cl;

    el.definitionReferenceResolverItf = cdrr;
    scrl.definitionReferenceResolverItf = cdrr;

    bdrr.errorManagerItf = errorManager;

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

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testBasicDefinitionReferenceResolverBC() throws Exception {
    checkBCImplementation(new BasicDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testBinaryADLLoaderBC() throws Exception {
    checkBCImplementation(new BinaryADLLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testCacheLoaderBC() throws Exception {
    checkBCImplementation(new CacheLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testCachingDefinitionReferenceResolverBC() throws Exception {
    checkBCImplementation(new CachingDefinitionReferenceResolver());
  }

  @Test(groups = {"functional", "checkin"})
  public void testExtendsLoaderBC() throws Exception {
    checkBCImplementation(new ExtendsLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testSubComponentResolverLoaderBC() throws Exception {
    checkBCImplementation(new SubComponentResolverLoader());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition def = loader.load("pkg1.pkg2.Primitive1", context);
    assertNotNull(def);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition def = loader.load("pkg1.Composite1", context);
    checker.assertDefinition(def).containsComponent("subComp1")
        .isAnInstanceOf("pkg1.pkg2.Primitive1");
  }

  @Test(groups = {"functional"})
  public void test3() throws Exception {
    final Definition def = loader.load("pkg1.Composite2", context);
    checker.assertDefinition(def).containsComponent("subComp1")
        .isAnInstanceOf("pkg1.Composite1");
  }
}
