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
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.AbstractIDLLoader;
import org.ow2.mind.idl.ast.IDL;

public class AnnotationProcessorLoader extends AbstractIDLLoader
    implements
      AnnotationProcessorLoaderAttributes {

  IDLLoaderPhase phase;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);

    processAnnotations(idl, idl, context);

    return idl;
  }

  protected void processAnnotations(final IDL idl, final Node node,
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
            executeProcessor(processorAnnotation.processor(), annotation, node,
                idl, context);
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

  protected void executeProcessor(
      final Class<? extends IDLLoaderAnnotationProcessor> processorClass,
      final Annotation annotation, final Node node, final IDL idl,
      final Map<Object, Object> context) throws ADLException {
    IDLLoaderAnnotationProcessor processor;
    try {
      processor = processorClass.newInstance();
    } catch (final InstantiationException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Can't instantiate Annotation processor \""
              + processorClass.getName() + "\".");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Can't instantiate Annotation processor \""
              + processorClass.getName() + "\".");
    }
    processor.processAnnotation(annotation, node, idl, phase, context);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the AnnotationProcessorLoaderAttributes interface
  // ---------------------------------------------------------------------------

  public String getPhase() {
    return phase.name();
  }

  public void setPhase(final String phase) {
    this.phase = IDLLoaderPhase.valueOf(phase);
  }

}
