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
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ASTChecker.DefinitionChecker;
import org.ow2.mind.adl.ASTChecker.ReferenceValue;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.BasicDefinitionReferenceResolver;
import org.ow2.mind.adl.CacheLoader;
import org.ow2.mind.adl.CachingDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.STCFNodeMerger;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.attribute.AttributeCheckerLoader;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.adl.parser.ADLParser;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class TestParameter {

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
                .followedBy(AttributeCheckerLoader.class)
                .followedBy(ExtendsLoader.class)
                .followedBy(SubComponentResolverLoader.class)
                .endingWith(ADLParser.class);

            bind(DefinitionReferenceResolver.class)
                .toChainStartingWith(CachingDefinitionReferenceResolver.class)
                .followedBy(ImportDefinitionReferenceResolver.class)
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
          }
        });

    loader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();

    checker = new ASTChecker();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition content = loader
        .load("pkg1.parameter.Parameter1", context);

    checker.assertDefinition(content).containsFormalParameters("a", "b", "c")

    .whereFirst().hasType(ParameterType.INT)

    .andNext().hasType(ParameterType.fromCType("./foo.h", "struct s"))

    .andNext().hasNoType();

    checker.assertDefinition(content)
        .containsAttributes("attr1", "attr2", "attr3")

        .whereFirst().valueIs(new ReferenceValue("a"))

        .andNext().valueIs(new ReferenceValue("b"))

        .andNext().valueIs(new Object[]{3, new ReferenceValue("c")});
  }

  @Test(groups = {"functional", "checkin"})
  public void test2() throws Exception {
    final Definition content = loader
        .load("pkg1.parameter.Parameter2", context);

    final DefinitionChecker definitionChecker = checker
        .assertDefinition(content);
    definitionChecker.containsComponent("subComp1")
        .isReferencing("pkg1.parameter.Parameter1")

        .containsArguments("a", "b", "c")

        .whereFirst().valueIs(10)

        .andNext().valueIs(new Object[]{3, 5})

        .andNext().valueIs("titi");

    definitionChecker.containsComponent("subComp2")
        .isReferencing("pkg1.parameter.Parameter1")

        .containsArguments("a", "b", "c")

        .whereFirst().valueIs(new ReferenceValue("a"))

        .andNext().valueIs(new Object[]{3, new ReferenceValue("b")})

        .andNext().valueIs(new Object[]{14, 15});
  }
}
