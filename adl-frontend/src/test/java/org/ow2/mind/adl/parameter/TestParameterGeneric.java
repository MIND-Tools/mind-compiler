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

import java.util.HashMap;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionCache;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.GraphChecker;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.binding.BindingChecker;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class TestParameterGeneric {

  Loader                  loader;

  HashMap<Object, Object> context;

  ASTChecker              checker;
  GraphChecker            graphChecker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new AbstractADLFrontendModule() {

          protected void configureTest() {
            bind(Loader.class).toChainStartingWith(ErrorLoader.class)
                .followedBy(CacheLoader.class).followedBy(ExtendsLoader.class)
                .followedBy(SubComponentResolverLoader.class)
                .followedBy(GenericDefinitionLoader.class)
                .endingWith(ADLParser.class);

            bind(DefinitionReferenceResolver.class)
                .toChainStartingWith(CachingDefinitionReferenceResolver.class)
                .followedBy(ImportDefinitionReferenceResolver.class)
                .followedBy(ParametricGenericDefinitionReferenceResolver.class)
                .followedBy(GenericDefinitionReferenceResolver.class)
                .followedBy(ParametricDefinitionReferenceResolver.class)
                .endingWith(BasicDefinitionReferenceResolver.class);

            bind(DefinitionReferenceResolver.class)
                .annotatedWith(
                    Names.named(ExtendsLoader.EXTENDS_DEFINITION_RESOLVER))
                .toChainStartingWith(
                    ExtendsParametricDefinitionReferenceResolver.class)
                .endingWith(DefinitionReferenceResolver.class);

            bind(NodeMerger.class).annotatedWith(
                Names.named(ExtendsLoader.EXTENDS_NODE_MERGER)).to(
                STCFNodeMerger.class);

            setDefaultSubComponentLoaderConfig();

            bind(TemplateInstantiator.class)
                .toChainStartingWith(CachingTemplateInstantiator.class)
                .followedBy(ParametricTemplateInstantiator.class)
                .endingWith(TemplateInstantiatorImpl.class);

            bind(BindingChecker.class).to(BasicBindingChecker.class);
            bind(DefinitionCache.class).to(CacheLoader.class);
          }
        });

    loader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
    graphChecker = new GraphChecker();
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

        .andNext().that()
        .isAnInstanceOf("pkg1.generic.Generic4<pkg1.parameter.Parameter1>");

    g4.containsFormalParameter("W$a");

    g4.containsComponents("c1")
        .whereFirst()
        .that()
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.generic.Generic1<pkg1.parameter.Parameter1>,pkg1.generic.Generic1<pkg1.parameter.Parameter1>>")
        .containsComponents("c1", "c2").whereFirst().that()
        .isAnInstanceOf("pkg1.generic.Generic1<pkg1.parameter.Parameter1>")
        .containsFormalParameter("T$a");

  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition content = loader.load("pkg1.parameterGeneric.Composite2",
        context);
    checker.assertDefinition(content).containsFormalParameters(/* no FP */);
    checker
        .assertDefinition(content)
        .containsComponents("c1", "c2", "c3", "c4", "c5")

        .whereFirst()
        /* c1 */.isAnInstanceOf("pkg1.parameter.Parameter1")

        .andNext()
        /* c2 */.isAnInstanceOf("pkg1.parameter.Parameter1")

        .andNext()
        /* c3 */.isAnInstanceOf(
            "pkg1.generic.Generic4<pkg1.parameter.Parameter1>")

        .andNext()
        /* c4 */.isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.parameter.Parameter1>")

        .andNext()/* c5 */.isAnInstanceOf("pkg1.parameterGeneric.Composite1");
  }
}
