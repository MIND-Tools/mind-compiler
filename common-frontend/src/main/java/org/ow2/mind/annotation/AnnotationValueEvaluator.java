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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.value.ValueEvaluationException;
import org.ow2.mind.value.ValueEvaluator;
import org.ow2.mind.value.ast.Value;

public class AnnotationValueEvaluator
    implements
      ValueEvaluator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_EVALUATOR_ITF_NAME = "client-evaluator";
  public ValueEvaluator      clientEvaluatorItf;

  public AnnotationFactory   annotationFactoryItf;

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
      return clientEvaluatorItf.evaluate(value, expectedType, context);
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(CLIENT_EVALUATOR_ITF_NAME, AnnotationFactory.ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (CLIENT_EVALUATOR_ITF_NAME.equals(s)) {
      return clientEvaluatorItf;
    } else if (AnnotationFactory.ITF_NAME.equals(s)) {
      return annotationFactoryItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (CLIENT_EVALUATOR_ITF_NAME.equals(s)) {
      clientEvaluatorItf = (ValueEvaluator) o;
    } else if (AnnotationFactory.ITF_NAME.equals(s)) {
      annotationFactoryItf = (AnnotationFactory) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (CLIENT_EVALUATOR_ITF_NAME.equals(s)) {
      clientEvaluatorItf = null;
    } else if (AnnotationFactory.ITF_NAME.equals(s)) {
      annotationFactoryItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }
}
