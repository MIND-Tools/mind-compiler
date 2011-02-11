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
 * Authors: Ali Erdem Ozcan
 * Contributors: Matthieu Leclercq 
 */

package org.ow2.mind.adl.annotation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class AnnotationProcessorLoader extends AbstractDelegatingLoader {

  protected ADLLoaderPhase phase;

  @Inject
  protected Injector       injector;

  public void setPhase(final ADLLoaderPhase phase) {
    this.phase = phase;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    Definition def = clientLoader.load(name, context);

    if (phase == ADLLoaderPhase.ON_SUB_COMPONENT) {
      def = processOnSubComponentAnnotations(def, def, context);
    } else {
      def = processAnnotations(def, def, context);
    }

    return def;
  }

  protected Definition processOnSubComponentAnnotations(Definition def,
      final Node node, final Map<Object, Object> context) throws ADLException {
    if (def instanceof ComponentContainer) {
      final Set<Component> processedSubComps = new HashSet<Component>();
      do {
        for (final Component subComp : ((ComponentContainer) def)
            .getComponents()) {
          if (!processedSubComps.add(subComp)) {
            // sub comp already processed, ignore it
            continue;
          }
          // assume that sub-component definition has already been loaded.
          final Definition subCompDef = ASTHelper
              .getResolvedComponentDefinition(subComp, null, null);

          final Definition d = processSubComponent(def, subComp, subCompDef,
              context);
          if (def != d) {
            // definition has been modified, must restart processing on new
            // subComponents
            def = d;
            break;
          }
        }
      } while (!processedSubComps.containsAll(Arrays
          .asList(((ComponentContainer) def).getComponents())));
    }
    return def;
  }

  protected Definition processSubComponent(Definition compositeDef,
      final Component subComp, final Node node,
      final Map<Object, Object> context) throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final ADLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(ADLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final ADLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final ADLLoaderPhase processPhase : processPhases) {
          if (phase == processPhase) {
            compositeDef = executeProcessor(processorAnnotation, annotation,
                subComp, compositeDef, context);
            break;
          }
        }
      }
    }

    // process sub nodes
    final String[] nodeTypes = node.astGetNodeTypes();
    if (nodeTypes != null) {
      for (final String nodeType : nodeTypes) {
        for (final Node subNode : node.astGetNodes(nodeType)) {
          if (subNode != null) {
            compositeDef = processSubComponent(compositeDef, subComp, subNode,
                context);
          }
        }
      }
    }
    return compositeDef;
  }

  protected Definition processAnnotations(Definition def, final Node node,
      final Map<Object, Object> context) throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final ADLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(ADLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final ADLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final ADLLoaderPhase processPhase : processPhases) {
          if (phase == processPhase) {
            def = executeProcessor(processorAnnotation, annotation, node, def,
                context);
            break;
          }
        }
      }
    }

    // process sub nodes
    final String[] nodeTypes = node.astGetNodeTypes();
    if (nodeTypes != null) {
      for (final String nodeType : nodeTypes) {
        for (final Node subNode : node.astGetNodes(nodeType)) {
          if (subNode != null) {
            def = processAnnotations(def, subNode, context);
          }
        }
      }
    }
    return def;
  }

  protected Definition executeProcessor(
      final ADLLoaderProcessor processorAnnotation,
      final Annotation annotation, final Node node,
      final Definition definition, final Map<Object, Object> context)
      throws ADLException {
    final ADLLoaderAnnotationProcessor processor = injector
        .getInstance(processorAnnotation.processor());
    final Definition result = processor.processAnnotation(annotation, node,
        definition, phase, context);
    return (result != null) ? result : definition;
  }

}
