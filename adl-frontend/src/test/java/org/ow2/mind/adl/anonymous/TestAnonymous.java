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

package org.ow2.mind.adl.anonymous;

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

public class TestAnonymous {

  Loader                  loader;

  HashMap<Object, Object> context;

  ASTChecker              checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();

    // Loader chain components
    final ADLParser adlLoader = new ADLParser();
    final AnonymousDefinitionLoader adl = new AnonymousDefinitionLoader();
    final SubComponentResolverLoader scrl = new SubComponentResolverLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final CacheLoader cl = new CacheLoader();
    final ErrorLoader errl = new ErrorLoader();

    errl.clientLoader = cl;
    cl.clientLoader = el;
    el.clientLoader = scrl;
    scrl.clientLoader = adl;
    adl.clientLoader = adlLoader;

    adlLoader.errorManagerItf = errorManager;
    adl.errorManagerItf = errorManager;
    scrl.errorManagerItf = errorManager;
    el.errorManagerItf = errorManager;
    cl.errorManagerItf = errorManager;
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

    // anonymous definition resolver chain
    final AnonymousDefinitionExtractorImpl adr = new AnonymousDefinitionExtractorImpl();
    final ImportAnonymousDefinitionExtractor iadr = new ImportAnonymousDefinitionExtractor();

    iadr.clientExtractorItf = adr;
    adl.anonymousDefinitionExtractorItf = iadr;

    // additional components
    final STCFNodeMerger stcfNodeMerger = new STCFNodeMerger();
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();
    final NodeMergerImpl nodeMerger = new NodeMergerImpl();

    el.nodeMergerItf = stcfNodeMerger;
    idrr.adlLocatorItf = adlLocator;
    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;

    adr.nodeFactoryItf = nodeFactory;
    iadr.nodeFactoryItf = nodeFactory;
    iadr.nodeMergerItf = nodeMerger;
    bdrr.nodeFactoryItf = nodeFactory;

    loader = errl;

    context = new HashMap<Object, Object>();
    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnonymousDefinitionLoaderBC() throws Exception {
    checkBCImplementation(new AnonymousDefinitionLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnonymousDefinitionResolverImplBC() throws Exception {
    checkBCImplementation(new AnonymousDefinitionExtractorImpl());
  }

  @Test(groups = {"functional", "checkin"})
  public void testImportAnonymousDefinitionResolverBC() throws Exception {
    checkBCImplementation(new ImportAnonymousDefinitionExtractor());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition content = loader
        .load("pkg1.anonymous.Composite1", context);
    checker.assertDefinition(content).containsComponent("subComp1")
        .isAnInstanceOf("pkg1.anonymous.Composite1$0")
        .containsComponents("subComp1", "subComp2");
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition content = loader
        .load("pkg1.anonymous.Composite2", context);
    checker.assertDefinition(content).containsComponent("subComp1")
        .isAnInstanceOf("pkg1.anonymous.Composite2$0")
        .containsComponents("subComp1", "subComp2");
  }
}
