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

package org.ow2.mind.annotation;

import static org.ow2.mind.annotation.AnnotationHelper.addAnnotation;
import static org.ow2.mind.annotation.AnnotationHelper.getAnnotation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.ow2.mind.annotation.ast.AnnotationContainer;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

public class BasicAnnotationChecker implements AnnotationChecker {

  @Inject
  public ErrorManager      errorManagerItf;

  @Inject
  public AnnotationFactory annotationFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the AnnotationChecker interface
  // ---------------------------------------------------------------------------

  public void checkAnnotations(final Node container,
      final Map<Object, Object> context) throws ADLException {
    checkAnnotations(container, new HashSet<Node>(), context);
  }

  protected void checkAnnotations(final Node container,
      final Set<Node> visitedNodes, final Map<Object, Object> context)
      throws ADLException {
    if (visitedNodes.add(container)) {
      if (container instanceof AnnotationContainer) {
        checkAnnotationContainer((AnnotationContainer) container, context);
      }

      for (final String nodeType : container.astGetNodeTypes()) {
        for (final Node subNode : container.astGetNodes(nodeType)) {
          if (subNode != null) {
            checkAnnotations(subNode, visitedNodes, context);
          }
        }
      }
    }
  }

  protected void checkAnnotationContainer(final AnnotationContainer container,
      final Map<Object, Object> context) throws ADLException {
    for (final AnnotationNode annotationNode : container.getAnnotations()) {
      // remove annotation node from AST.
      container.removeAnnotation(annotationNode);

      Annotation annotation;
      try {
        annotation = annotationFactoryItf
            .newAnnotation(annotationNode, context);
      } catch (final AnnotationInitializationException e) {
        errorManagerItf.logError(AnnotationErrors.INVALID_ANNOTATION,
            new NodeErrorLocator(e.getLocation()), e, e.getMessage());
        continue;
      }

      boolean isValidTarget = false;
      for (final AnnotationTarget target : annotation.getAnnotationTargets()) {
        if (target.isValidTarget(container)) {
          isValidTarget = true;
          break;
        }
      }

      if (!isValidTarget) {
        errorManagerItf.logError(AnnotationErrors.INVALID_ANNOTATION_TARGET,
            annotationNode);
        continue;
      }
      if (getAnnotation(container, annotation.getClass()) != null) {
        errorManagerItf.logError(AnnotationErrors.DUPLICATED_ANNOTATION,
            annotation);
      } else {
        addAnnotation(container, annotation);
      }
    }
  }
}
