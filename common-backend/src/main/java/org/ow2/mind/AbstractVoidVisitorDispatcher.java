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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

public abstract class AbstractVoidVisitorDispatcher<I>
    implements
      VoidVisitor<I>,
      BindingController {

  /** The name of the {@link Compiler} client interface of this component. */
  public static final String         CLIENT_VISITOR = "client-visitor";

  /** The builders client interfaces. */
  // use a LinkedHashMap to ensure the iteration order which improves
  // stability when debugging
  public Map<String, VoidVisitor<I>> visitorsItf    = new LinkedHashMap<String, VoidVisitor<I>>();

  /**
   * This method is used by {@link #bindFc(String, Object) bindFc} to cast the
   * given <code>serverItf</code> to a correct type. Sub-classes must implements
   * this method to guaranty type safety.
   */
  protected abstract VoidVisitor<I> castVisitorInterface(Object serverItf);

  // --------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // --------------------------------------------------------------------------

  public void visit(final I input, final Map<Object, Object> context)
      throws ADLException {
    for (final VoidVisitor<I> visitorItf : visitorsItf.values()) {
      visitorItf.visit(input, context);
    }
  }

  // --------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // --------------------------------------------------------------------------

  public String[] listFc() {
    final List<String> interfaceList = new ArrayList<String>(visitorsItf
        .keySet());
    return interfaceList.toArray(new String[interfaceList.size()]);
  }

  public void bindFc(final String clientItfName, final Object serverItf)
      throws NoSuchInterfaceException, IllegalBindingException,
      IllegalLifeCycleException {

    if (clientItfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (clientItfName.startsWith(CLIENT_VISITOR))
      visitorsItf.put(clientItfName, castVisitorInterface(serverItf));
    else
      throw new NoSuchInterfaceException("There is no interface named '"
          + clientItfName + "'");
  }

  public Object lookupFc(final String clientItfName)
      throws NoSuchInterfaceException {

    if (clientItfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (clientItfName.startsWith(CLIENT_VISITOR))
      return visitorsItf.get(clientItfName);
    else
      throw new NoSuchInterfaceException("There is no interface named '"
          + clientItfName + "'");
  }

  public void unbindFc(final String clientItfName)
      throws NoSuchInterfaceException, IllegalBindingException,
      IllegalLifeCycleException {

    if (clientItfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (clientItfName.startsWith(CLIENT_VISITOR)) {
      if (visitorsItf.remove(clientItfName) == null) {
        throw new NoSuchInterfaceException("There is no interface named '"
            + clientItfName + "'");
      }
    } else
      throw new NoSuchInterfaceException("There is no interface named '"
          + clientItfName + "'");
  }

}
