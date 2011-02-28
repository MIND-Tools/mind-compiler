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

package org.ow2.mind.idl;

import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.GenericResourceLocator;
import org.ow2.mind.idl.annotation.AnnotationProcessorLoader;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.parser.JTBProcessor;
import org.ow2.mind.inject.AbstractMindModule;

import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public abstract class AbstractIDLFrontendModule extends AbstractMindModule {

  protected static final String DEFAULT_IDL_DTD = "classpath://org/ow2/mind/idl/mind_v1.dtd";

  protected void configureJTBProcessor() {
    bind(JTBProcessor.class);
    bind(String.class).annotatedWith(Names.named(JTBProcessor.IDL_DTD))
        .toInstance(DEFAULT_IDL_DTD);
  }

  protected void configureLocators() {
    bind(IDLLocator.class).to(BasicIDLLocator.class);

    final Multibinder<GenericResourceLocator> multibinder = CommonFrontendModule
        .getGenericResourceLocatorBinder(binder());
    multibinder.addBinding().to(BasicIDLLocator.class);
  }

  protected final class AnnotationProcessorProvider
      implements
        Provider<IDLLoader> {

    final Provider<AnnotationProcessorLoader> delegate;
    final IDLLoaderPhase                      phase;

    AnnotationProcessorProvider(final IDLLoaderPhase phase) {
      this.phase = phase;
      delegate = getProvider(AnnotationProcessorLoader.class);
    }

    public IDLLoader get() {
      final AnnotationProcessorLoader loader = delegate.get();
      loader.setPhase(phase);
      return loader;
    }
  }

}