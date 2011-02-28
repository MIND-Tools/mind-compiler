/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.plugin.util;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;

public final class Assert {
  private Assert() {
  }

  /**
   * Throws a {@link CompilerError}.
   * 
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void fail(final String message) {
    throw new CompilerError(GenericErrors.GENERIC_ERROR, (message == null)
        ? "Assertion fails"
        : message);
  }

  /**
   * Checks if the given boolean is true. If not throws a {@link CompilerError}.
   * 
   * @param condition the condition to check.
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void assertTrue(final boolean condition, final String message) {
    if (!condition) {
      fail(message);
    }
  }

  /**
   * Checks if the given boolean is true. If not throws a {@link CompilerError}.
   * 
   * @param condition the condition to check.
   */
  public static void assertTrue(final boolean condition) {
    assertTrue(condition, null);
  }

  /**
   * Checks if the given boolean is false. If not throws a {@link CompilerError}
   * .
   * 
   * @param condition the condition to check.
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void assertFalse(final boolean condition, final String message) {
    assertTrue(!condition, message);
  }

  /**
   * Checks if the given boolean is false. If not throws a {@link CompilerError}
   * .
   * 
   * @param condition the condition to check.
   */
  public static void assertFalse(final boolean condition) {
    assertFalse(condition, null);
  }

  /**
   * Checks if <code>actual</code> is equals to <code>expected</code> (using
   * <code>expected.equals(actual)</code>), or if both are <code>null</code>. If
   * it is not the case, throws a {@link CompilerError}.
   * 
   * @param actual an object. May be <code>null</code>
   * @param expected another object. May be <code>null</code>
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void assertEquals(final Object actual, final Object expected,
      final String message) {
    if (expected == null) {
      assertTrue(actual == null, message);
    } else {
      assertTrue(actual != null && expected.equals(actual), message);
    }
  }

  /**
   * Checks if <code>actual</code> is equals to <code>expected</code> (using
   * <code>expected.equals(actual)</code>), or if both are <code>null</code>. If
   * it is not the case, throws a {@link CompilerError}.
   * 
   * @param actual an object. May be <code>null</code>
   * @param expected another object. May be <code>null</code>
   */
  public static void assertEquals(final Object actual, final Object expected) {
    if (expected == null) {
      assertTrue(actual == null, null);
    } else {
      assertTrue(actual != null && expected.equals(actual), null);
    }
  }

  /**
   * Checks if given <code>object</code> is <code>null</code>. If not throws a
   * {@link CompilerError}.
   * 
   * @param object the object to check.
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void assertNull(final Object object, final String message) {
    assertTrue(object == null, message);
  }

  /**
   * Checks if given <code>object</code> is <code>null</code>. If not throws a
   * {@link CompilerError}.
   * 
   * @param object the object to check.
   */
  public static void assertNull(final Object object) {
    assertTrue(object == null);
  }

  /**
   * Checks if given <code>object</code> is not <code>null</code>. If not throws
   * a {@link CompilerError}.
   * 
   * @param object the object to check.
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   */
  public static void assertNotNull(final Object object, final String message) {
    assertTrue(object != null, message);
  }

  /**
   * Checks if given <code>object</code> is not <code>null</code>. If not throws
   * a {@link CompilerError}.
   * 
   * @param object the object to check.
   */
  public static void assertNotNull(final Object object) {
    assertTrue(object != null);
  }

  /**
   * Checks if given <code>object</code> is an instance of the given
   * <code>expectedClass</code>. If not throws a {@link CompilerError}.
   * 
   * @param <T> The type to which the object is casted.
   * @param object the object to cast.
   * @param exptectedClass the class that the object is expected to be an
   *          instance of.
   * @param message the message to be used in the {@link CompilerError}. May be
   *          <code>null</code>
   * @return the given object casted as an instance of the given class.
   */
  public static <T> T assertInstanceof(final Object object,
      final Class<T> exptectedClass, final String message) {
    assertTrue(exptectedClass.isInstance(object), message);
    return exptectedClass.cast(object);
  }

  /**
   * Checks if given <code>object</code> is an instance of the given
   * <code>expectedClass</code>. If not throws a {@link CompilerError}.
   * 
   * @param <T> The type to which the object is casted.
   * @param object the object to cast.
   * @param exptectedClass the class that the object is expected to be an
   *          instance of.
   * @return the given object casted as an instance of the given class.
   */
  public static <T> T assertInstanceof(final Object object,
      final Class<T> exptectedClass) {
    return assertInstanceof(object, exptectedClass, null);
  }

}
