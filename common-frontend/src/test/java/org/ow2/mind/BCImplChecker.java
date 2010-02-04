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

package org.ow2.mind;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;

/**
 * This checker class allows to check that implementations of the
 * {@link BindingController} interface of front-end components are correct and
 * follows some coding rules.
 */
public class BCImplChecker {

  /**
   * Check the implementation of the {@link BindingController} interface of the
   * given component.
   * 
   * @param component a component that implements the {@link BindingController}
   *          interface.
   */
  public static void checkBCImplementation(final BindingController component) {

    try {
      component.bindFc(null, null);
      fail("bindFc(null, ...) does not throw an IllegalArgumentException.");
    } catch (final IllegalArgumentException e) {
      // OK
    } catch (final Exception e) {
      fail("bindFc(null, ...) does not throw an IllegalArgumentException: " + e);
    }

    try {
      component.lookupFc(null);
      fail("lookupFc(null) does not throw an IllegalArgumentException.");
    } catch (final IllegalArgumentException e) {
      // OK
    } catch (final Exception e) {
      fail("lookupFc(null) does not throw an IllegalArgumentException: " + e);
    }

    try {
      component.unbindFc(null);
      fail("unbindFc(null) does not throw an IllegalArgumentException.");
    } catch (final IllegalArgumentException e) {
      // OK
    } catch (final Exception e) {
      fail("unbindFc(null) does not throw an IllegalArgumentException: " + e);
    }

    final String[] itfs = component.listFc();
    assertNotNull("listFc return null", itfs);

    final Set<String> itfNames = new HashSet<String>();
    for (final String itf : itfs) {
      assertNotNull("Array returned by listFc contains null", itf);
      assertTrue("Interface name is empty", itf.length() > 0);
      itfNames.add(itf);
    }

    assertEquals("Duplicated interface names in array returned by listFc.",
        itfs.length, itfNames.size());

    for (final String itf : itfs) {
      try {
        component.unbindFc(itf);
      } catch (final Exception e) {
        fail("unbindFc throws an exception: " + e);
      }
    }

    for (final String itf : itfs) {
      try {
        assertNull("Client interface is not null after unbind", component
            .lookupFc(itf));
      } catch (final NoSuchInterfaceException e) {
        fail("lookupFc throws an exception: " + e);
      }
    }

    final String undefinedItfName = findNewName(itfNames);

    try {
      component.bindFc(undefinedItfName, null);
      fail("bindFc(\"undefinedItfName\", ...) does not throw an NoSuchInterfaceException.");
    } catch (final NoSuchInterfaceException e) {
      // OK
    } catch (final Exception e) {
      fail("bindFc(\"undefinedItfName\", ...) does not throw an NoSuchInterfaceException: "
          + e);
    }

    try {
      component.lookupFc(undefinedItfName);
      fail("lookup(\"undefinedItfName\") does not throw an NoSuchInterfaceException.");
    } catch (final NoSuchInterfaceException e) {
      // OK
    } catch (final Exception e) {
      fail("lookup(\"undefinedItfName\") does not throw an NoSuchInterfaceException: "
          + e);
    }

    try {
      component.unbindFc(undefinedItfName);
      fail("unbindFc(\"undefinedItfName\") does not throw an NoSuchInterfaceException.");
    } catch (final NoSuchInterfaceException e) {
      // OK
    } catch (final Exception e) {
      fail("unbindFc(\"undefinedItfName\") does not throw an NoSuchInterfaceException: "
          + e);
    }

    // try to find the type of each client interface by introspecting class
    // fields.
    forEachField : for (final Field field : component.getClass().getFields()) {
      if (Modifier.isStatic(field.getModifiers())
          || !Modifier.isPublic(field.getModifiers())) continue;
      final Class<?> c = field.getType();
      if (!c.isInterface())
        fail("public non-static field " + field.getName()
            + " does not correspond to a client interface.");

      try {
        if (field.get(component) != null)
          fail("public non-static field " + field.getName()
              + " does not correspond to a client interface.");
      } catch (final Exception e) {
        e.printStackTrace();
        fail(e.toString());
      }

      // proxy object implements the interface of the field.
      final Object proxy = Proxy.newProxyInstance(c.getClassLoader(),
          new Class[]{c}, new NullInvocationHandler());

      // find an interface name that allows to set the field.
      final Iterator<String> itfNameIterator = itfNames.iterator();
      while (itfNameIterator.hasNext()) {
        final String itf = itfNameIterator.next();
        try {
          component.bindFc(itf, proxy);
        } catch (final NoSuchInterfaceException e) {
          fail("bindFc throws a NoSuchInterfaceException: " + e);
        } catch (final Exception e) {
          continue;
        }

        // binding "itf" interface of component do not raise error. checks that
        // the field is correctly updated.
        try {
          if (field.get(component) == proxy) {
            // "itf" client interface correspond to 'field'
            itfNameIterator.remove();
            continue forEachField;
          } else {
            // "itf" client interface do not correspond to 'field'. unbind
            component.unbindFc(itf);
          }
        } catch (final Exception e) {
          e.printStackTrace();
          fail(e.toString());
        }
      }

      fail("public non-static field " + field.getName()
          + " does not correspond to a client interface.");
    }
    assertTrue("Can't find fields for client interfaces: " + itfNames, itfNames
        .isEmpty());
  }

  private static String findNewName(final Set<String> itfs) {

    final String itfName = "undefinedItfName";
    if (!itfs.contains(itfName)) return itfName;

    int i = 0;
    while (itfs.contains(itfName + i))
      i++;

    return itfName + i;
  }

  private static final class NullInvocationHandler implements InvocationHandler {

    public Object invoke(final Object proxy, final Method method,
        final Object[] args) throws Throwable {
      return null;
    }
  }
}
