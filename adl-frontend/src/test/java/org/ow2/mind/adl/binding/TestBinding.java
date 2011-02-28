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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.idl.BasicInterfaceSignatureResolver;
import org.ow2.mind.adl.idl.InterfaceSignatureLoader;
import org.ow2.mind.adl.idl.InterfaceSignatureResolver;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.imports.ImportInterfaceSignatureResolver;
import org.ow2.mind.adl.membrane.CompositeInternalInterfaceLoader;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestBinding {

  Loader              loader;

  Map<Object, Object> context;

  ASTChecker          checker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new IDLFrontendModule(),
        new AbstractADLFrontendModule() {
          protected void configureTest() {
            bind(Loader.class).toChainStartingWith(ErrorLoader.class)
                .followedBy(CacheLoader.class)
                .followedBy(UnboundInterfaceCheckerLoader.class)
                .followedBy(BindingCheckerLoader.class)
                .followedBy(BindingNormalizerLoader.class)
                .followedBy(CompositeInternalInterfaceLoader.class)
                .followedBy(ExtendsLoader.class)
                .followedBy(InterfaceSignatureLoader.class)
                .followedBy(SubComponentResolverLoader.class)
                .endingWith(ADLParser.class);

            bind(DefinitionReferenceResolver.class)
                .toChainStartingWith(CachingDefinitionReferenceResolver.class)
                .followedBy(ImportDefinitionReferenceResolver.class)
                .endingWith(BasicDefinitionReferenceResolver.class);

            bind(BindingChecker.class).toChainStartingWith(
                IDLBindingChecker.class).endingWith(BasicBindingChecker.class);

            setDefaultExtendsLoaderConfig();
            setDefaultSubComponentLoaderConfig();

            bind(InterfaceSignatureResolver.class).toChainStartingWith(
                ImportInterfaceSignatureResolver.class).endingWith(
                BasicInterfaceSignatureResolver.class);
          }
        });

    loader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
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
