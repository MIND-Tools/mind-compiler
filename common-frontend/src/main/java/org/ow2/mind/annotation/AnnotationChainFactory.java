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

import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.value.BasicValueEvaluator;
import org.ow2.mind.value.ValueEvaluator;

public final class AnnotationChainFactory {
  private AnnotationChainFactory() {
  }

  public static ValueEvaluator newValueEvaluator(
      final AnnotationFactory annotationFactory) {
    final AnnotationValueEvaluator ave = new AnnotationValueEvaluator();
    final BasicValueEvaluator bve = new BasicValueEvaluator();

    final ValueEvaluator evaluator = ave;
    ave.clientEvaluatorItf = bve;
    bve.recursiveEvaluatorItf = evaluator;

    ave.annotationFactoryItf = annotationFactory;

    return evaluator;
  }

  public static AnnotationFactory newAnnotationFactory() {
    AnnotationFactory annotationFactory;
    final BasicAnnotationFactory baf = new BasicAnnotationFactory();
    final BasicAnnotationLocator bal = new BasicAnnotationLocator();

    annotationFactory = baf;
    baf.evaluatorItf = newValueEvaluator(annotationFactory);
    baf.annotationLocatorItf = bal;

    return annotationFactory;
  }

  public static AnnotationChecker newAnnotationChecker(
      final ErrorManager errorManager) {
    return newAnnotationChecker(newAnnotationFactory(), errorManager);
  }

  public static AnnotationChecker newAnnotationChecker() {
    return newAnnotationChecker(newAnnotationFactory(),
        ErrorManagerFactory.newStreamErrorManager());
  }

  public static AnnotationChecker newAnnotationChecker(
      final AnnotationFactory annotationFactory, final ErrorManager errorManager) {
    AnnotationChecker annotationChecker;
    final BasicAnnotationChecker bac = new BasicAnnotationChecker();
    annotationChecker = bac;
    bac.annotationFactoryItf = annotationFactory;
    bac.errorManagerItf = errorManager;

    return annotationChecker;
  }
}
