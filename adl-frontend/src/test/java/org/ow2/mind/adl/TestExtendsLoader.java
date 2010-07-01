/**
 * Copyright (C) 2010 STMicroelectronics
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestExtendsLoader {

  Loader              loader;

  Map<Object, Object> context;

  ASTChecker          checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();
    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final ExtendsLoader el = new ExtendsLoader();
    final CacheLoader cl = new CacheLoader();
    final ErrorLoader errl = new ErrorLoader();

    loader = errl;
    errl.clientLoader = cl;
    cl.clientLoader = el;
    el.clientLoader = adlLoader;

    adlLoader.errorManagerItf = errorManager;
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
  public void testExtendsLoaderBC() throws Exception {
    checkBCImplementation(new ExtendsLoader());
  }

  @Test(groups = {"functional"})
  public void testAbstract() throws Exception {
    final Definition d = loader.load("pkg1.abstracts.AbstractPrimitive",
        context);
    assertTrue(ASTHelper.isAbstract(d));
  }

  @Test(groups = {"functional"})
  public void testExtendsAbstract() throws Exception {
    final Definition d = loader.load("pkg1.abstracts.ConcretePrimitive",
        context);
    assertFalse(ASTHelper.isAbstract(d));
  }
}
