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

package org.ow2.mind;

import java.io.PrintStream;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.annotation.AnnotationChecker;
import org.ow2.mind.annotation.AnnotationFactory;
import org.ow2.mind.annotation.AnnotationLocator;
import org.ow2.mind.annotation.AnnotationValueEvaluator;
import org.ow2.mind.annotation.BasicAnnotationChecker;
import org.ow2.mind.annotation.BasicAnnotationFactory;
import org.ow2.mind.annotation.BasicAnnotationLocator;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.StreamErrorManager;
import org.ow2.mind.inject.AbstractMindModule;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.STNodeMergerImpl;
import org.ow2.mind.st.StringTemplateComponentLoader;
import org.ow2.mind.st.XMLSTNodeFactoryImpl;
import org.ow2.mind.st.templates.parser.StringTemplateLoader;
import org.ow2.mind.value.BasicValueEvaluator;
import org.ow2.mind.value.ValueEvaluator;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class CommonFrontendModule extends AbstractMindModule {

  // ---------------------------------------------------------------------------
  // Globals
  // ---------------------------------------------------------------------------

  /**
   * Creates a {@link Multibinder} that can be used to bind
   * {@link GenericResourceLocator}.
   * 
   * @param binder the binder to use to create {@link Multibinder}.
   * @return a {@link Multibinder}.
   * @see Multibinder#newSetBinder
   */
  public synchronized static Multibinder<GenericResourceLocator> getGenericResourceLocatorBinder(
      final Binder binder) {
    return Multibinder.newSetBinder(binder, GenericResourceLocator.class);
  }

  protected void configureInputResourceLocator() {
    bind(InputResourceLocator.class).to(BasicInputResourceLocator.class);
    // create an empty Multibinder for GenericResourceLocator.
    getGenericResourceLocatorBinder(binder());
  }

  protected void configureNodeFactory() {
    bind(NodeFactory.class).toProvider(new Provider<NodeFactory>() {
      public NodeFactory get() {
        final NodeFactory nodeFactory = new STNodeFactoryImpl();
        nodeFactory.setClassLoader(this.getClass().getClassLoader());
        return nodeFactory;
      }
    });
  }

  protected void configureNodeMerger() {
    bind(NodeMerger.class).toProvider(new Provider<NodeMerger>() {
      public NodeMerger get() {
        final NodeMerger nodeMerger = new STNodeMergerImpl();
        nodeMerger.setClassLoader(this.getClass().getClassLoader());
        return nodeMerger;
      }
    });
  }

  protected void configureXMLNodeFactory() {
    bind(XMLNodeFactory.class).toProvider(new Provider<XMLNodeFactory>() {
      public XMLNodeFactory get() {
        final XMLNodeFactory xmlNodeFactory = new XMLSTNodeFactoryImpl();
        // set my class loader as classloader used by XMLNodeFactory
        xmlNodeFactory.setClassLoader(this.getClass().getClassLoader());
        return xmlNodeFactory;
      }
    });
  }

  // ---------------------------------------------------------------------------
  // Annotations
  // ---------------------------------------------------------------------------

  protected void configureAnnotationFactory() {
    bind(AnnotationFactory.class).to(BasicAnnotationFactory.class);
  }

  protected void configureAnnotationLocator() {
    bind(AnnotationLocator.class).to(BasicAnnotationLocator.class);
  }

  protected void configureAnnotationChecker() {
    bind(AnnotationChecker.class).to(BasicAnnotationChecker.class);
  }

  // ---------------------------------------------------------------------------
  // ErrorManager
  // ---------------------------------------------------------------------------

  protected void configureErrorManager() {
    bind(ErrorManager.class).to(StreamErrorManager.class);
    bind(PrintStream.class).annotatedWith(
        Names.named(StreamErrorManager.ERROR_STREAM_NAME)).toInstance(
        System.err);
    bind(PrintStream.class).annotatedWith(
        Names.named(StreamErrorManager.WARNING_STREAM_NAME)).toInstance(
        System.err);
    bind(Boolean.class).annotatedWith(
        Names.named(StreamErrorManager.PRINT_STACK_TRACE_NAME)).toInstance(
        Boolean.FALSE);
  }

  // ---------------------------------------------------------------------------
  // StringTemplate
  // ---------------------------------------------------------------------------

  protected void configureStringTemplateGroupLoader() {
    bind(StringTemplateGroupLoader.class).to(
        StringTemplateComponentLoader.class);
    bind(Loader.class).annotatedWith(
        Names.named(StringTemplateComponentLoader.STRING_TEMPLATE_LOADER_NAME))
        .to(StringTemplateLoader.class);
  }

  // ---------------------------------------------------------------------------
  // Value
  // ---------------------------------------------------------------------------

  protected void configureValueEvaluator() {
    bind(ValueEvaluator.class).toChainStartingWith(
        AnnotationValueEvaluator.class).endingWith(BasicValueEvaluator.class);
  }

}
