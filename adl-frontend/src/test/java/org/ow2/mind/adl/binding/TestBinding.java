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

package org.ow2.mind.adl.binding;

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.Error;
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
import org.ow2.mind.adl.idl.BasicInterfaceSignatureResolver;
import org.ow2.mind.adl.idl.InterfaceSignatureLoader;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.imports.ImportInterfaceSignatureResolver;
import org.ow2.mind.adl.membrane.CompositeInternalInterfaceLoader;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.BasicIDLLocator;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBinding {

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
    final InterfaceSignatureLoader isl = new InterfaceSignatureLoader();
    final ExtendsLoader el = new ExtendsLoader();
    final CompositeInternalInterfaceLoader ciil = new CompositeInternalInterfaceLoader();
    final BindingNormalizerLoader bnl = new BindingNormalizerLoader();
    final BindingCheckerLoader bcl = new BindingCheckerLoader();
    final UnboundInterfaceCheckerLoader uicl = new UnboundInterfaceCheckerLoader();
    final CacheLoader cl = new CacheLoader();
    final ErrorLoader errl = new ErrorLoader();

    errl.clientLoader = cl;
    cl.clientLoader = uicl;
    uicl.clientLoader = bcl;
    bcl.clientLoader = bnl;
    bnl.clientLoader = ciil;
    ciil.clientLoader = el;
    el.clientLoader = isl;
    isl.clientLoader = scrl;
    scrl.clientLoader = adlLoader;

    adlLoader.errorManagerItf = errorManager;
    scrl.errorManagerItf = errorManager;
    isl.errorManagerItf = errorManager;
    errl.errorManagerItf = errorManager;
    uicl.errorManagerItf = errorManager;

    // definition reference resolver chain
    final BasicDefinitionReferenceResolver bdrr = new BasicDefinitionReferenceResolver();
    final ImportDefinitionReferenceResolver idrr = new ImportDefinitionReferenceResolver();
    final CachingDefinitionReferenceResolver cdrr = new CachingDefinitionReferenceResolver();

    cdrr.clientResolverItf = idrr;
    idrr.clientResolverItf = bdrr;
    bdrr.loaderItf = cl;
    cdrr.loaderItf = cl;

    scrl.definitionReferenceResolverItf = cdrr;
    el.definitionReferenceResolverItf = cdrr;
    el.nodeMergerItf = new STCFNodeMerger();

    bdrr.errorManagerItf = errorManager;

    // Binding checkers
    final BindingChecker bindingChecker;
    final BasicBindingChecker bbc = new BasicBindingChecker();
    final IDLBindingChecker ibc = new IDLBindingChecker();
    bindingChecker = ibc;
    ibc.clientBindingCheckerItf = bbc;

    bcl.bindingCheckerItf = bindingChecker;

    bbc.errorManagerItf = errorManager;
    ibc.errorManagerItf = errorManager;

    // additional components
    final BasicADLLocator adlLocator = new BasicADLLocator();
    final XMLNodeFactoryImpl xmlNodeFactory = new XMLNodeFactoryImpl();
    final NodeFactoryImpl nodeFactory = new NodeFactoryImpl();
    final NodeMergerImpl nodeMerger = new NodeMergerImpl();

    adlLoader.adlLocatorItf = adlLocator;
    adlLoader.nodeFactoryItf = xmlNodeFactory;
    ciil.nodeFactoryItf = nodeFactory;
    ciil.nodeMergerItf = nodeMerger;

    idrr.adlLocatorItf = adlLocator;
    bdrr.nodeFactoryItf = nodeFactory;

    final BasicInterfaceSignatureResolver bisr = new BasicInterfaceSignatureResolver();
    final ImportInterfaceSignatureResolver iisr = new ImportInterfaceSignatureResolver();
    final IDLLocator idlLocator = new BasicIDLLocator();
    iisr.clientResolverItf = bisr;
    bisr.idlLoaderItf = IDLLoaderChainFactory.newLoader(errorManager).loader;
    iisr.idlLocatorItf = idlLocator;
    isl.interfaceSignatureResolverItf = iisr;

    loader = errl;

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional", "checkin"})
  public void testBindingCheckerLoaderBC() {
    checkBCImplementation(new BindingCheckerLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testUnboundInterfaceCheckerLoaderBC() {
    checkBCImplementation(new UnboundInterfaceCheckerLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void testIDLBindingCheckerBC() throws Exception {
    checkBCImplementation(new IDLBindingChecker());
  }

  @Test(groups = {"functional"})
  public void testBindExtends() throws ADLException {
    loader.load("pkg1.binding.BindExtends", context);
  }

  @Test(groups = {"functional"})
  public void testBindSimple() throws ADLException {
    loader.load("pkg1.binding.BindSimple", context);
  }

  @Test(groups = {"functional"})
  public void testBindInvalid() throws ADLException {
    try {
      loader.load("pkg1.binding.BindInvalid", context);
      fail("An exception was expected here");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 1);
      final Error err = errors.iterator().next();
      assertSame(err.getTemplate(), BindingErrors.INVALID_SIGNATURE);
    }
  }

  @Test(groups = {"functional"})
  public void testExportCollection() throws ADLException {
    loader.load("pkg1.binding.ExportCollection", context);
  }

  @Test(groups = {"functional"})
  public void testExport() throws ADLException {
    loader.load("pkg1.binding.Export", context);
  }
}
