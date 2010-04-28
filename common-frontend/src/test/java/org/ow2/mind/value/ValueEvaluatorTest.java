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

package org.ow2.mind.value;

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.ow2.mind.AbstractTestcase;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.StringLiteral;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueEvaluatorTest extends AbstractTestcase {

  ValueEvaluator      evaluator;
  Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    final BasicValueEvaluator bve = new BasicValueEvaluator();

    evaluator = bve;
    bve.recursiveEvaluatorItf = bve;

    context = new HashMap<Object, Object>();
  }

  @Test(groups = {"functional", "checkin"})
  public void testBasicValueEvaluatorBC() throws Exception {
    checkBCImplementation(new BasicValueEvaluator());
  }

  @Test(groups = {"functional"})
  public void testNumberLiteral1() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    final Integer i = evaluator.evaluate(v, Integer.class, context);
    assertNotNull(i);
    assertEquals((Integer) 12, i);
  }

  @Test(groups = {"functional"})
  public void testNumberLiteral2() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    final Number n = evaluator.evaluate(v, Number.class, context);
    assertNotNull(n);
    assertEquals(12, n);
  }

  @Test(groups = {"functional"})
  public void testNumberLiteral3() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    final int i = evaluator.evaluate(v, Integer.TYPE, context);
    assertEquals(12, i);
  }

  @Test(groups = {"functional"})
  public void testNumberLiteral4() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    final Object i = evaluator.evaluate(v, Object.class, context);
    assertNotNull(i);
    assertEquals(12, i);
  }

  @Test(groups = {"functional"})
  public void testNumberLiteralError1() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    try {
      evaluator.evaluate(v, String.class, context);
      fail();
    } catch (final ValueEvaluationException e) {
      // ok
      assertSame(v, e.getLocation());
    }
  }

  @Test(groups = {"functional"})
  public void testNumberError2() throws Exception {
    final NumberLiteral v = newNumberLiteral(12);

    try {
      evaluator.evaluate(v, String.class, context);
      fail();
    } catch (final ValueEvaluationException e) {
      // ok
      assertSame(v, e.getLocation());
    }
  }

  @Test(groups = {"functional"})
  public void testStringLiteral1() throws Exception {
    final StringLiteral v = newStringLiteral("foo");

    final String s = evaluator.evaluate(v, String.class, context);
    assertNotNull(s);
    assertEquals("foo", s);
  }

  @Test(groups = {"functional"})
  public void testStringLiteral2() throws Exception {
    final StringLiteral v = newStringLiteral("foo");

    final Object s = evaluator.evaluate(v, Object.class, context);
    assertNotNull(s);
    assertEquals("foo", s);
  }

  @Test(groups = {"functional"})
  public void testStringLiteral3() throws Exception {
    checkStringEvaluator("foo\\n", "foo\n");
    checkStringEvaluator("foo\\t", "foo\t");
    checkStringEvaluator("foo\\b", "foo\b");
    checkStringEvaluator("foo\\r", "foo\r");
    checkStringEvaluator("foo\\f", "foo\f");
    checkStringEvaluator("foo\\\\", "foo\\");
    checkStringEvaluator("foo\\\"", "foo\"");
    checkStringEvaluator("foo\\\'", "foo\'");

    checkStringEvaluator("foo\\\n", "foo\n");
    checkStringEvaluator("foo\\\r", "foo\r");
    checkStringEvaluator("foo\\\r\n", "foo\r\n");
    checkStringEvaluator("foo\\r\\n", "foo\r\n");

    checkStringEvaluator("foo\\6", "foo\006");
    checkStringEvaluator("foo\\06", "foo\006");
    checkStringEvaluator("foo\\006", "foo\006");

    checkStringEvaluator("foo\\66", "foo\066");
    checkStringEvaluator("foo\\066", "foo\066");

    checkStringEvaluator("foo\\166", "foo\166");
  }

  private void checkStringEvaluator(final String value, final String expected)
      throws ValueEvaluationException {
    final StringLiteral v = newStringLiteral(value);

    final Object s = evaluator.evaluate(v, Object.class, context);
    assertNotNull(s);
    assertEquals(s, expected);
  }

  @Test(groups = {"functional"})
  public void testStringLiteralError1() throws Exception {
    final StringLiteral v = newStringLiteral("foo");

    try {
      evaluator.evaluate(v, Integer.class, context);
      fail();
    } catch (final ValueEvaluationException e) {
      // ok
      assertSame(v, e.getLocation());
    }
  }

  @Test(groups = {"functional"})
  public void testArray1() throws Exception {
    final Array a = newArray(newStringLiteral("toto"), newStringLiteral("titi"));

    final String[] tab = evaluator.evaluate(a, String[].class, context);
    assertNotNull(tab);
    assertEquals(2, tab.length);
    assertEquals("toto", tab[0]);
    assertEquals("titi", tab[1]);
  }

  @Test(groups = {"functional"})
  public void testArray2() throws Exception {
    final Array a = newArray(newStringLiteral("toto"), newNumberLiteral(12));

    final Object[] tab = evaluator.evaluate(a, Object[].class, context);
    assertNotNull(tab);
    assertEquals(2, tab.length);
    assertEquals("toto", tab[0]);
    assertEquals(12, tab[1]);
  }

  @Test(groups = {"functional"})
  public void testArrayError1() throws Exception {
    final NumberLiteral n = newNumberLiteral(12);
    final Array a = newArray(newStringLiteral("toto"), n);

    try {
      evaluator.evaluate(a, String[].class, context);
      fail();
    } catch (final ValueEvaluationException e) {
      // ok
      assertSame(n, e.getLocation());
    }

  }
}
