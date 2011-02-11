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

import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AnnotationLoader;
import org.ow2.mind.adl.annotation.AnnotationProcessorTemplateInstantiator;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractor;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractorImpl;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionLoader;
import org.ow2.mind.adl.anonymous.ImportAnonymousDefinitionExtractor;
import org.ow2.mind.adl.anonymous.InputResourceAnonymousDefinitionExtractor;
import org.ow2.mind.adl.attribute.AttributeCheckerLoader;
import org.ow2.mind.adl.attribute.AttributesNormalizerLoader;
import org.ow2.mind.adl.binding.BasicBindingChecker;
import org.ow2.mind.adl.binding.BindingChecker;
import org.ow2.mind.adl.binding.BindingCheckerLoader;
import org.ow2.mind.adl.binding.BindingNormalizerLoader;
import org.ow2.mind.adl.binding.IDLBindingChecker;
import org.ow2.mind.adl.binding.UnboundInterfaceCheckerLoader;
import org.ow2.mind.adl.factory.FactoryLoader;
import org.ow2.mind.adl.factory.FactoryTemplateInstantiator;
import org.ow2.mind.adl.factory.ParametricFactoryTemplateInstantiator;
import org.ow2.mind.adl.generic.CachingTemplateInstantiator;
import org.ow2.mind.adl.generic.ExtendsGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.GenericAnonymousDefinitionExtractor;
import org.ow2.mind.adl.generic.GenericDefinitionLoader;
import org.ow2.mind.adl.generic.GenericDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.InputResourceTemplateInstantiator;
import org.ow2.mind.adl.generic.NoAnySubComponentLoader;
import org.ow2.mind.adl.generic.NoAnyTypeArgumentDefinitionReferenceResolver;
import org.ow2.mind.adl.generic.TemplateInstanceLoader;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.TemplateInstantiatorImpl;
import org.ow2.mind.adl.graph.AttributeInstantiator;
import org.ow2.mind.adl.graph.BasicInstantiator;
import org.ow2.mind.adl.graph.BindingInstantiator;
import org.ow2.mind.adl.graph.InstanceNameInstantiator;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.idl.BasicInterfaceSignatureResolver;
import org.ow2.mind.adl.idl.InterfaceSignatureLoader;
import org.ow2.mind.adl.idl.InterfaceSignatureResolver;
import org.ow2.mind.adl.implementation.ImplementationLoader;
import org.ow2.mind.adl.imports.ADLImportChecker;
import org.ow2.mind.adl.imports.IDLImportChecker;
import org.ow2.mind.adl.imports.ImportChecker;
import org.ow2.mind.adl.imports.ImportCheckerLoader;
import org.ow2.mind.adl.imports.ImportDefinitionReferenceResolver;
import org.ow2.mind.adl.imports.ImportInterfaceSignatureResolver;
import org.ow2.mind.adl.membrane.CompositeInternalInterfaceLoader;
import org.ow2.mind.adl.membrane.MembraneCheckerLoader;
import org.ow2.mind.adl.parameter.ExtendsParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParameterNormalizerLoader;
import org.ow2.mind.adl.parameter.ParametricAnonymousDefinitionExtractor;
import org.ow2.mind.adl.parameter.ParametricDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricGenericDefinitionReferenceResolver;
import org.ow2.mind.adl.parameter.ParametricTemplateInstantiator;
import org.ow2.mind.adl.parser.ADLParser;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

/**
 * Configure the ADL-Frontend Guice module.
 */
public class ADLFrontendModule extends AbstractADLFrontendModule {

  /**
   * Returns the {@link Key} used to bind the default loader chain. This module
   * simply binds the {@link Loader} class to the default loader chain. But
   * another module can {@link Modules#override override} this binding to add
   * other loaders at the head of the chain. For instance :
   * 
   * <pre>
   * bind(Loader.class).toChainStartingWith(MyLoader.class)
   *     .followedBy(MyOtherLoader.class).endingWith(defaultLoaderKey());
   * </pre>
   * 
   * @return the {@link Key} used to bind the default loader chain.
   */
  public static Key<Loader> defaultLoaderKey() {
    return Key.get(Loader.class, Names.named("default-loader"));
  }

  protected void configureLoader() {
    bind(Loader.class).to(defaultLoaderKey());
  }

  protected void configureDefaultLoader() {
    bind(defaultLoaderKey())
        .toChainStartingWith(CacheLoader.class)
        .followedBy(BinaryADLLoader.class)
        .followedBy(TemplateInstanceLoader.class)
        .followedBy(
            new AnnotationProcessorProvider(binder(),
                ADLLoaderPhase.AFTER_CHECKING))
        .followedBy(AttributeCheckerLoader.class)
        .followedBy(AttributesNormalizerLoader.class)
        .followedBy(ParameterNormalizerLoader.class)
        .followedBy(ImplementationLoader.class)
        .followedBy(UnboundInterfaceCheckerLoader.class)
        .followedBy(BindingCheckerLoader.class)
        .followedBy(BindingNormalizerLoader.class)
        .followedBy(MembraneCheckerLoader.class)
        .followedBy(CompositeInternalInterfaceLoader.class)
        .followedBy(InterfaceSignatureLoader.class)
        .followedBy(InterfaceCheckerLoader.class)
        .followedBy(InterfaceNormalizerLoader.class)
        .followedBy(
            new AnnotationProcessorProvider(binder(),
                ADLLoaderPhase.ON_SUB_COMPONENT))
        .followedBy(
            new AnnotationProcessorProvider(binder(),
                ADLLoaderPhase.AFTER_EXTENDS))
        .followedBy(NoAnySubComponentLoader.class)
        .followedBy(ExtendsLoader.class)
        .followedBy(SubComponentResolverLoader.class)
        .followedBy(SubComponentNormalizerLoader.class)
        .followedBy(AnonymousDefinitionLoader.class)
        .followedBy(GenericDefinitionLoader.class)
        .followedBy(ImportCheckerLoader.class)
        .followedBy(
            new AnnotationProcessorProvider(binder(),
                ADLLoaderPhase.AFTER_PARSING)).followedBy(FactoryLoader.class)
        .followedBy(ADLLoader.class).followedBy(AnnotationLoader.class)
        .endingWith(parserKey());
  }

  /**
   * Returns the {@link Key} used to bind the ADL parser. This module simply
   * binds this key to {@link ADLParser} class. But another module can
   * {@link Modules#override override} this binding to chance the parser.
   * 
   * @return the {@link Key} used to bind the ADL parser.
   */
  public static Key<Loader> parserKey() {
    return Key.get(Loader.class, Names.named("parser"));
  }

  protected void configureParser() {
    bind(parserKey()).to(ADLParser.class);
  }

  protected void configureDefinitionCache() {
    // CacheLoader is singleton.
    bind(DefinitionCache.class).to(CacheLoader.class);
  }

  protected void configureExtendsLoader() {
    bind(DefinitionReferenceResolver.class)
        .annotatedWith(Names.named(ExtendsLoader.EXTENDS_DEFINITION_RESOLVER))
        .toChainStartingWith(ExtendsGenericDefinitionReferenceResolver.class)
        .followedBy(ExtendsParametricDefinitionReferenceResolver.class)
        .endingWith(/* normal chain */DefinitionReferenceResolver.class);

    bind(NodeMerger.class).annotatedWith(
        Names.named(ExtendsLoader.EXTENDS_NODE_MERGER))
        .to(STCFNodeMerger.class);
  }

  protected void configureSubComponentReferenceLoader() {
    bind(DefinitionReferenceResolver.class)
        .annotatedWith(
            Names
                .named(SubComponentResolverLoader.SUB_COMPONENT_DEFINITION_RESOLVER))
        .toChainStartingWith(NoAnyTypeArgumentDefinitionReferenceResolver.class)
        .endingWith(/* normal chain */DefinitionReferenceResolver.class);
  }

  protected void configureInterfaceSignatureResolver() {
    bind(InterfaceSignatureResolver.class).toChainStartingWith(
        ImportInterfaceSignatureResolver.class).endingWith(
        BasicInterfaceSignatureResolver.class);
  }

  protected void configureImportChecker() {
    bind(ImportChecker.class).toChainStartingWith(ADLImportChecker.class)
        .endingWith(IDLImportChecker.class);
  }

  protected void configureBindingChecker() {
    bind(BindingChecker.class).toChainStartingWith(IDLBindingChecker.class)
        .endingWith(BasicBindingChecker.class);
  }

  protected void configureDefinitionReferenceResolver() {
    bind(DefinitionReferenceResolver.class)
        .toChainStartingWith(CachingDefinitionReferenceResolver.class)
        .followedBy(ImportDefinitionReferenceResolver.class)
        .followedBy(ParametricGenericDefinitionReferenceResolver.class)
        .followedBy(GenericDefinitionReferenceResolver.class)
        .followedBy(ParametricDefinitionReferenceResolver.class)
        .followedBy(InputResourcesDefinitionReferenceResolver.class)
        .endingWith(BasicDefinitionReferenceResolver.class);

  }

  protected void configureTemplateInstantiator() {
    bind(TemplateInstantiator.class)
        .toChainStartingWith(CachingTemplateInstantiator.class)
        .followedBy(AnnotationProcessorTemplateInstantiator.class)
        .followedBy(ParametricFactoryTemplateInstantiator.class)
        .followedBy(ParametricTemplateInstantiator.class)
        .followedBy(FactoryTemplateInstantiator.class)
        .followedBy(InputResourceTemplateInstantiator.class)
        .endingWith(TemplateInstantiatorImpl.class);
  }

  protected void configureAnonymousDefinitionExtractor() {
    bind(AnonymousDefinitionExtractor.class)
        .toChainStartingWith(InputResourceAnonymousDefinitionExtractor.class)
        .followedBy(ParametricAnonymousDefinitionExtractor.class)
        .followedBy(GenericAnonymousDefinitionExtractor.class)
        .followedBy(ImportAnonymousDefinitionExtractor.class)
        .endingWith(AnonymousDefinitionExtractorImpl.class);
  }

  protected void configureInstantiator() {
    bind(Instantiator.class).toChainStartingWith(AttributeInstantiator.class)
        .followedBy(InstanceNameInstantiator.class)
        .followedBy(BindingInstantiator.class)
        .endingWith(BasicInstantiator.class);
  }
}
