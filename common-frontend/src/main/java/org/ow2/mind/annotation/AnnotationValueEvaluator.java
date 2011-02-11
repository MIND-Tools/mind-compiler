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

import java.util.Map;

import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.value.ValueEvaluationException;
import org.ow2.mind.value.ValueEvaluator.AbstractDelegatingValueEvaluator;
import org.ow2.mind.value.ast.Value;

import com.google.inject.Inject;

public class AnnotationValueEvaluator extends AbstractDelegatingValueEvaluator {

  @Inject
  public AnnotationFactory annotationFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ValueEvaluator interface
  // ---------------------------------------------------------------------------

  public <T> T evaluate(final Value value, final Class<T> expectedType,
      final Map<Object, Object> context) throws ValueEvaluationException {
    if (value instanceof AnnotationNode) {
      if (!Annotation.class.isAssignableFrom(expectedType)) {
        throw new ValueEvaluationException(
            "Incompatible value type, found annotation where "
                + expectedType.getName() + " was expected", value);
      }

      final Annotation annotation;
      try {
        annotation = annotationFactoryItf.newAnnotation((AnnotationNode) value,
            context);
      } catch (final AnnotationInitializationException e) {
        throw new ValueEvaluationException("Invalid annotation value,", value,
            e);
      }

      T result;
      try {
        result = expectedType.cast(annotation);
      } catch (final ClassCastException e) {
        throw new ValueEvaluationException(
            "Incompatible annotation value type, found \""
                + annotation.getClass().getName() + "\" where "
                + expectedType.getName() + " was expected", value, e);
      }
      return result;
    } else {
      return clientValueEvaluatorItf.evaluate(value, expectedType, context);
    }
  }
}
