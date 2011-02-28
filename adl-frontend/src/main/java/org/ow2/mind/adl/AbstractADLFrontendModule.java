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
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.GenericResourceLocator;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AnnotationProcessorLoader;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.parser.ADLJTBParser;
import org.ow2.mind.adl.parser.JTBProcessor;
import org.ow2.mind.inject.AbstractMindModule;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Base configuration for the ADL Front-end module. This class can be
 * sub-classed in tests to use custom front-end. Configurations made in this
 * module does not depends on IDL Front-end.
 */
public abstract class AbstractADLFrontendModule extends AbstractMindModule {

  protected static final String DEFAULT_ADL_DTD = "classpath://org/ow2/mind/adl/mind_v1.dtd";

  protected void configureJTBProcessor() {
    bind(ADLJTBParser.class).to(JTBProcessor.class);
    bind(String.class).annotatedWith(Names.named(JTBProcessor.ADL_DTD))
        .toInstance(DEFAULT_ADL_DTD);
  }

  public static final class AnnotationProcessorProvider
      implements
        Provider<Loader> {

    final Provider<AnnotationProcessorLoader> delegate;
    final ADLLoaderPhase                      phase;

    public AnnotationProcessorProvider(final Binder binder,
        final ADLLoaderPhase phase) {
      this.phase = phase;
      delegate = binder.getProvider(AnnotationProcessorLoader.class);
    }

    public Loader get() {
      final AnnotationProcessorLoader loader = delegate.get();
      loader.setPhase(phase);
      return loader;
    }
  }

  protected void configureADLLocator() {
    bind(ADLLocator.class).to(BasicADLLocator.class);
  }

  protected void configureADLGenericResourceLocator() {
    final Multibinder<GenericResourceLocator> multibinder = CommonFrontendModule
        .getGenericResourceLocatorBinder(binder());
    multibinder.addBinding().to(ADLLocator.class);
  }

  protected void configureImplementationLocator() {
    bind(ImplementationLocator.class).to(BasicImplementationLocator.class);
  }

  protected void configureImplementationGenericResourceLocator() {
    final Multibinder<GenericResourceLocator> multibinder = CommonFrontendModule
        .getGenericResourceLocatorBinder(binder());
    multibinder.addBinding().to(ImplementationLocator.class);
  }

  protected void configureADLIDAttributes() {
    final MapBinder<String, String> mapBinder = MapBinder.newMapBinder(
        binder(), String.class, String.class,
        Names.named(ExtendsLoader.ADL_ID_ATTRIBUTES));

    mapBinder.addBinding("component").toInstance("name");
    mapBinder.addBinding("interface").toInstance("name");
    mapBinder.addBinding("attribute").toInstance("name");
    mapBinder.addBinding("annotation").toInstance("type");
    mapBinder.addBinding("argument").toInstance("name");
    mapBinder.addBinding("template").toInstance("name");
  }

  /**
   * This method is not automatically called by {@link AbstractMindModule}, but
   * can be called by sub-classes (in tests) to set the default configuration of
   * the {@link ExtendsLoader}.
   */
  protected void setDefaultExtendsLoaderConfig() {
    bind(DefinitionReferenceResolver.class).annotatedWith(
        Names.named(ExtendsLoader.EXTENDS_DEFINITION_RESOLVER)).to(
        DefinitionReferenceResolver.class);

    bind(NodeMerger.class).annotatedWith(
        Names.named(ExtendsLoader.EXTENDS_NODE_MERGER))
        .to(STCFNodeMerger.class);
  }

  /**
   * This method is not automatically called by {@link AbstractMindModule}, but
   * can be called by sub-classes (in tests) to set the default configuration of
   * the {@link SubComponentResolverLoader}.
   */
  protected void setDefaultSubComponentLoaderConfig() {
    bind(DefinitionReferenceResolver.class)
        .annotatedWith(
            Names
                .named(SubComponentResolverLoader.SUB_COMPONENT_DEFINITION_RESOLVER))
        .to(DefinitionReferenceResolver.class);

  }
}