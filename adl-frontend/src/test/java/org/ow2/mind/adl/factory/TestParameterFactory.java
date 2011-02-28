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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ASTChecker.ComponentChecker;
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
import org.ow2.mind.adl.parameter.ParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricTemplateInstantiator;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class TestParameterFactory {

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
                .followedBy(ParametricGenericDefinitionReferenceResolver.class)
                .followedBy(GenericDefinitionReferenceResolver.class)
                .followedBy(ParametricDefinitionReferenceResolver.class)
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
                .followedBy(ParametricFactoryTemplateInstantiator.class)
                .followedBy(ParametricTemplateInstantiator.class)
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
    final Definition d = loader.load(
        "pkg1.parametricFactory.ParametricFactory1", context);
    final ComponentChecker component = checker.assertDefinition(d)
        .containsComponent("factory");
    component.isReferencing("Factory<pkg1.parameter.Parameter1>").that()
        .containsArguments("InstantiatedDefinition$a").whereFirst().valueIs(10);

    component.isAnInstanceOf("Factory<pkg1.parameter.Parameter1>").that()
        .containsAttributes("a").whereFirst()
        .valueReferences("InstantiatedDefinition$a");
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition d = loader.load(
        "pkg1.parametricFactory.ParametricFactory2", context);
    final ComponentChecker component = checker
        .assertDefinition(d)
        .containsComponent("subComp")
        .that()
        .isAnInstanceOf(
            "pkg1.factory.GenericFactory1<pkg1.parameter.Parameter1>")
        .containsComponent("factory");

    component.isReferencing("Factory<pkg1.parameter.Parameter1>").that()
        .containsArguments("InstantiatedDefinition$a").whereFirst()
        .valueReferences("T$a");

    component.isAnInstanceOf("Factory<pkg1.parameter.Parameter1>").that()
        .containsAttributes("a").whereFirst()
        .valueReferences("InstantiatedDefinition$a");
  }

}
