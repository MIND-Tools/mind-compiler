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

package org.ow2.mind.adl.attribute;

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
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestAttribute {

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
    final AttributesNormalizerLoader anl = new AttributesNormalizerLoader();
    final AttributeCheckerLoader acl = new AttributeCheckerLoader();
    final CacheLoader cl = new CacheLoader();
    final ErrorLoader errl = new ErrorLoader();

    errl.clientLoader = cl;
    cl.clientLoader = acl;
    acl.clientLoader = anl;
    anl.clientLoader = el;
    el.clientLoader = adlLoader;

    adlLoader.errorManagerItf = errorManager;
    el.errorManagerItf = errorManager;
    anl.errorManagerItf = errorManager;
    acl.errorManagerItf = errorManager;
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
    el.nodeMergerItf = new STCFNodeMerger();

    bdrr.errorManagerItf = errorManager;

    // additional components
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();

    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    idrr.adlLocatorItf = adlLocator;
    bdrr.nodeFactoryItf = nodeFactory;

    loader = errl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testAttributesNormalizerLoaderBC() throws Exception {
    checkBCImplementation(new AttributesNormalizerLoader());
  }

  @Test(groups = {"functional"})
  public void testAttr1() throws Exception {
    final Definition d = loader.load("pkg1.attr.Attr1", context);
    checker.assertDefinition(d).containsAttributes("attr1", "attr2", "attr3")
        .whereFirst().hasType("int").and().valueIs(12)

        .andNext().hasType("uint8_t").and().valueIs(11)

        .andNext().hasType("string").and().valueIs("toto");
  }

  @Test(groups = {"functional"})
  public void testExtendsAttr1() throws Exception {
    final Definition d = loader.load("pkg1.attr.ExtendsAttr1", context);
    checker.assertDefinition(d).containsAttributes("attr1", "attr2", "attr3")
        .whereFirst().hasType("int").and().valueReferences("a")

        .andNext().hasType("uint8_t").and().valueIs(11)

        .andNext().hasType("string").and().valueIs("titi");
  }
}
