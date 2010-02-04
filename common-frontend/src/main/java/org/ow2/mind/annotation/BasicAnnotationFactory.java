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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.annotation.ast.AnnotationArgument;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.value.ValueEvaluationException;
import org.ow2.mind.value.ValueEvaluator;

public class BasicAnnotationFactory
    implements
      AnnotationFactory,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public ValueEvaluator    evaluatorItf;

  public AnnotationLocator annotationLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the AnnotationFactory interface
  // ---------------------------------------------------------------------------

  public Annotation newAnnotation(final AnnotationNode annotationNode,
      final Map<Object, Object> context)
      throws AnnotationInitializationException {
    final String type = annotationNode.getType();
    final Class<? extends Annotation> annotationClass;
    try {
      annotationClass = annotationLocatorItf.findAnnotationClass(type, context)
          .asSubclass(Annotation.class);
    } catch (final ClassNotFoundException e) {
      throw new AnnotationInitializationException("Unknown annotation type",
          annotationNode, e);
    } catch (final ClassCastException e) {
      throw new AnnotationInitializationException(
          "Invalid annotation type, do not reference an annotation class",
          annotationNode, e);
    }

    final Annotation annotation;
    try {
      annotation = annotationClass.newInstance();
    } catch (final InstantiationException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Invalid annotation class \"" + type
              + "\", can't instantiate annotation");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Invalid annotation class \"" + type
              + "\", can't instantiate annotation");
    }

    // Build map of annotation fields by looking for public field with the
    // 'AnnotationElement' annotation.
    final Field[] fields = annotationClass.getFields();
    final Map<String, Field> annotationFields = new HashMap<String, Field>();

    for (final Field field : fields) {
      final AnnotationElement e = field.getAnnotation(AnnotationElement.class);
      if (e != null) {
        annotationFields.put(field.getName(), field);
      }
    }

    for (final AnnotationArgument argument : annotationNode
        .getAnnotationArguments()) {
      final Field field = annotationFields.remove(argument.getName());
      if (field == null) {
        throw new AnnotationInitializationException(
            "No such annotation argument \"" + argument.getName() + "\"",
            argument);
      }
      final Class<?> fieldType = field.getType();
      Object arguementValue;
      try {
        arguementValue = evaluatorItf.evaluate(argument.getValue(), fieldType,
            context);
      } catch (final ValueEvaluationException e) {
        throw new AnnotationInitializationException(
            "Invalid argument value for argument \"" + argument.getName()
                + "\"", argument, e);
      }
      try {
        field.set(annotation, arguementValue);
      } catch (final IllegalArgumentException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Fail to set value of annnotation element");
      } catch (final IllegalAccessException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Fail to set value of annnotation element");
      }
    }

    // checks that non-initialized fields have a default value.
    for (final Field field : annotationFields.values()) {
      final AnnotationElement annotationElement = field
          .getAnnotation(AnnotationElement.class);
      if (!annotationElement.hasDefaultValue()) {
        throw new AnnotationInitializationException(
            "Missing argument value for argument \"" + field.getName() + "\"",
            annotationNode);
      }
    }

    return annotation;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(ValueEvaluator.ITF_NAME, AnnotationLocator.ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (ValueEvaluator.ITF_NAME.equals(s)) {
      return evaluatorItf;
    } else if (AnnotationLocator.ITF_NAME.equals(s)) {
      return annotationLocatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (ValueEvaluator.ITF_NAME.equals(s)) {
      evaluatorItf = (ValueEvaluator) o;
    } else if (AnnotationLocator.ITF_NAME.equals(s)) {
      annotationLocatorItf = (AnnotationLocator) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (ValueEvaluator.ITF_NAME.equals(s)) {
      evaluatorItf = null;
    } else if (AnnotationLocator.ITF_NAME.equals(s)) {
      annotationLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

}
