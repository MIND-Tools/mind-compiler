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

package org.ow2.mind.idl.annotation;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.IDLLoader.AbstractDelegatingIDLLoader;
import org.ow2.mind.idl.ast.IDL;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class AnnotationProcessorLoader extends AbstractDelegatingIDLLoader {

  IDLLoaderPhase     phase;

  @Inject
  protected Injector injector;

  /**
   * @param phase the phase to set
   */
  public void setPhase(final IDLLoaderPhase phase) {
    this.phase = phase;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);

    processAnnotations(idl, idl, context);

    return idl;
  }

  protected void processAnnotations(IDL idl, final Node node,
      final Map<Object, Object> context) throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final IDLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(IDLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final IDLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final IDLLoaderPhase processPhase : processPhases) {
          if (phase == processPhase) {
            idl = executeProcessor(processorAnnotation, annotation, node, idl,
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
            processAnnotations(idl, subNode, context);
          }
        }
      }
    }
  }

  protected IDL executeProcessor(final IDLLoaderProcessor processorAnnotation,
      final Annotation annotation, final Node node, final IDL idl,
      final Map<Object, Object> context) throws ADLException {
    final IDLLoaderAnnotationProcessor processor = injector
        .getInstance(processorAnnotation.processor());
    final IDL result = processor.processAnnotation(annotation, node, idl,
        phase, context);
    return (result != null) ? result : idl;
  }
}
