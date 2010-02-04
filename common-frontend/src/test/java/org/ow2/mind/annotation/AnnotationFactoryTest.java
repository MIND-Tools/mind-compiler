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

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;

import org.ow2.mind.AbstractTestcase;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationChainFactory;
import org.ow2.mind.annotation.AnnotationFactory;
import org.ow2.mind.annotation.AnnotationInitializationException;
import org.ow2.mind.annotation.AnnotationValueEvaluator;
import org.ow2.mind.annotation.BasicAnnotationChecker;
import org.ow2.mind.annotation.BasicAnnotationFactory;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnnotationFactoryTest extends AbstractTestcase {

  AnnotationFactory annotationFactory;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    annotationFactory = AnnotationChainFactory.newAnnotationFactory();
  }

  @Test(groups = {"functional"})
  public void testAnnotationFactory1() throws Exception {
    final AnnotationNode annoAST = newAnnotationNode(FooAnnotation.class
        .getName(), newAnnotationArgument("foo", newStringLiteral("hello")));

    final Annotation annotation = annotationFactory.newAnnotation(annoAST,
        new HashMap<Object, Object>());

    assertNotNull(annotation);
    assertTrue(annotation instanceof FooAnnotation);
    final FooAnnotation fooAnnotation = (FooAnnotation) annotation;
    assertEquals(2, fooAnnotation.count);
    assertEquals("hello", fooAnnotation.foo);
  }

  @Test(groups = {"functional"})
  public void testAnnotationFactory2() throws Exception {
    final AnnotationNode annoAST = newAnnotationNode(FooAnnotation.class
        .getName(), newAnnotationArgument("foo", newStringLiteral("hello")),
        newAnnotationArgument("count", newNumberLiteral(3)));

    final Annotation annotation = annotationFactory.newAnnotation(annoAST,
        new HashMap<Object, Object>());

    assertNotNull(annotation);
    assertTrue(annotation instanceof FooAnnotation);
    final FooAnnotation fooAnnotation = (FooAnnotation) annotation;
    assertEquals(3, fooAnnotation.count);
    assertEquals("hello", fooAnnotation.foo);
  }

  @Test(groups = {"functional"})
  public void testAnnotationFactory3() throws Exception {
    final AnnotationNode annoAST = newAnnotationNode(BarAnnotation.class
        .getName(),

    newAnnotationArgument("bar",

    newArray(

    newAnnotationValue(FooAnnotation.class.getName(), newAnnotationArgument(
        "foo", newStringLiteral("hello"))),

    newAnnotationValue(FooAnnotation.class.getName(), newAnnotationArgument(
        "foo", newStringLiteral("world")), newAnnotationArgument("count",
        newNumberLiteral(3))))

    ));

    final Annotation annotation = annotationFactory.newAnnotation(annoAST,
        new HashMap<Object, Object>());

    assertNotNull(annotation);
    assertTrue(annotation instanceof BarAnnotation);
    final BarAnnotation barAnnotation = (BarAnnotation) annotation;
    final FooAnnotation[] bar = barAnnotation.bar;
    assertNotNull(bar);
    assertEquals(2, bar.length);
    final FooAnnotation bar0 = bar[0];
    assertNotNull(bar0);
    assertEquals("hello", bar0.foo);
    assertEquals(2, bar0.count);

    final FooAnnotation bar1 = bar[1];
    assertNotNull(bar1);
    assertEquals("world", bar1.foo);
    assertEquals(3, bar1.count);
  }

  @Test(groups = {"functional"})
  public void testAnnotationFactoryError1() throws Exception {
    final AnnotationNode annoAST = newAnnotationNode(FooAnnotation.class
        .getName(), newAnnotationArgument("count", newNumberLiteral(3)));

    try {
      annotationFactory.newAnnotation(annoAST, new HashMap<Object, Object>());
      fail();
    } catch (final AnnotationInitializationException e) {
      assertSame(annoAST, e.getLocation());
    }
  }

  @Test(groups = {"functional", "checkin"})
  public void testBasicAnnotationFactoryBC() throws Exception {
    checkBCImplementation(new BasicAnnotationFactory());
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnnotationValueEvaluatorBC() throws Exception {
    checkBCImplementation(new AnnotationValueEvaluator());
  }

  @Test(groups = {"functional", "checkin"})
  public void testBasicAnnotationCheckerBC() throws Exception {
    checkBCImplementation(new BasicAnnotationChecker());
  }
}
