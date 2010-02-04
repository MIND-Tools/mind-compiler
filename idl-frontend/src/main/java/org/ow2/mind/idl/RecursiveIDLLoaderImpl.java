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

package org.ow2.mind.idl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.idl.ast.IDL;

public class RecursiveIDLLoaderImpl
    implements
      RecursiveIDLLoader,
      BindingController {

  protected ThreadLocal<Map<String, IDL>> loadingIDLs                = new ThreadLocal<Map<String, IDL>>();

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /** The name of the {@link #clientIDLLoaderItf} client interface. */
  public static final String              CLIENT_IDL_LOADER_ITF_NAME = "client-idl-loader";

  /** The client {@link IDLLoader} used by this component. */
  public IDLLoader                        clientIDLLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final IDL encapsulatingIDL, final String name,
      final Map<Object, Object> context) throws ADLException {
    Map<String, IDL> loadingIDLs = this.loadingIDLs.get();
    if (loadingIDLs == null) {
      loadingIDLs = new HashMap<String, IDL>();
      this.loadingIDLs.set(loadingIDLs);
    }

    final IDL idl = loadingIDLs.get(name);
    if (idl != null) {
      // The IDL to resolve is already loading (i.e. cycle in included files)
      return idl;
    }

    assert !loadingIDLs.containsKey(encapsulatingIDL.getName());
    loadingIDLs.put(encapsulatingIDL.getName(), encapsulatingIDL);
    try {
      return clientIDLLoaderItf.load(name, context);
    } finally {
      loadingIDLs.remove(encapsulatingIDL.getName());
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(CLIENT_IDL_LOADER_ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (CLIENT_IDL_LOADER_ITF_NAME.equals(s)) {
      return clientIDLLoaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (CLIENT_IDL_LOADER_ITF_NAME.equals(s)) {
      clientIDLLoaderItf = (IDLLoader) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (CLIENT_IDL_LOADER_ITF_NAME.equals(s)) {
      clientIDLLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }
}
