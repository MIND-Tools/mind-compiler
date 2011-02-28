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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionCache;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.binding.BindingChecker;
import org.ow2.mind.adl.binding.IDLBindingChecker;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.ExtendsGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class TestFactory {

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
                .followedBy(CacheLoader.class).followedBy(ExtendsLoader.class)
                .followedBy(SubComponentResolverLoader.class)
                .followedBy(GenericDefinitionLoader.class)
                .followedBy(FactoryLoader.class).endingWith(ADLParser.class);

            bind(DefinitionReferenceResolver.class)
                .toChainStartingWith(CachingDefinitionReferenceResolver.class)
                .followedBy(ImportDefinitionReferenceResolver.class)
                .followedBy(GenericDefinitionReferenceResolver.class)
                .endingWith(BasicDefinitionReferenceResolver.class);

            bind(BindingChecker.class).toChainStartingWith(
                IDLBindingChecker.class).endingWith(BasicBindingChecker.class);

            bind(DefinitionReferenceResolver.class)
                .annotatedWith(
                    Names.named(ExtendsLoader.EXTENDS_DEFINITION_RESOLVER))
                .toChainStartingWith(
                    ExtendsGenericDefinitionReferenceResolver.class)
                .endingWith(DefinitionReferenceResolver.class);

            bind(NodeMerger.class).annotatedWith(
                Names.named(ExtendsLoader.EXTENDS_NODE_MERGER)).to(
                STCFNodeMerger.class);

            setDefaultSubComponentLoaderConfig();

            bind(TemplateInstantiator.class)
                .toChainStartingWith(CachingTemplateInstantiator.class)
                .followedBy(FactoryTemplateInstantiator.class)
                .endingWith(TemplateInstantiatorImpl.class);

            bind(DefinitionCache.class).to(CacheLoader.class);
          }
        });

    loader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition d = loader.load("pkg1.factory.Factory1", context);
    final Definition def = checker.assertDefinition(d)
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.Composite1>").def;

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
    final Definition def = checker.assertDefinition(d)
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.Composite1>").def;
    final Definition instantiatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(def, null, null);
    assertSame(instantiatedDefinition, composite1Def);
  }

  @Test(groups = {"functional"})
  public void test3() throws Exception {
    final Definition d = loader.load("pkg1.factory.GenericFactory1", context);
    final Definition def = checker.assertDefinition(d)
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.pkg2.Type1>").def;

    checker.assertDefinition(def).containsInterfaces("factory", "allocator")
        .whereFirst().isServer().hasSignature("fractal.api.Factory").andNext()
        .isClient().hasSignature("memory.api.Allocator");
  }

  @Test(groups = {"functional"})
  public void test4() throws Exception {
    final Definition d = loader.load("pkg1.factory.Factory2", context);
    final Definition def = checker.assertDefinition(d)
        .containsComponent("subComp")
        .isAnInstanceOf("pkg1.factory.GenericFactory1<pkg1.Composite1>")
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.Composite1>").def;

    final Definition instantiatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(def, null, null);
    assertNotNull(instantiatedDefinition);
    assertEquals(instantiatedDefinition.getName(), "pkg1.Composite1");
  }

  @Test(groups = {"functional"})
  public void test5() throws Exception {
    final Definition d1 = loader.load("pkg1.factory.Factory1", context);
    final Definition def1 = checker.assertDefinition(d1)
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.Composite1>").def;
    assertNotNull(def1);

    final Definition d2 = loader.load("pkg1.factory.Factory2", context);
    final Definition def2 = checker.assertDefinition(d2)
        .containsComponent("subComp")
        .isAnInstanceOf("pkg1.factory.GenericFactory1<pkg1.Composite1>")
        .containsComponent("factory")
        .isAnInstanceOf("Factory<pkg1.Composite1>").def;
    assertNotNull(def2);

    assertSame(def1, def2);
  }

}
