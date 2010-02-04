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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.plugin;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;

/**
 * A proxy class for connecting many factory components to a single factory
 * client interface.
 */
public class FactoryProxy implements Factory, BindingController {

  /**
   * Client factory collection interface.
   */
  public Map<String, Factory> factoryItfs = new HashMap<String, Factory>();

  /**
   * Iterates over the list of client factories until one of them successfully
   * instantiates the requested plugin component. If no client factories manage
   * to instantiate the requested component, then raises a {@link CompilerError}
   * 
   * @param name the name of the component to be created.
   * @param context optional additional information.
   * @return the component that has been created. The type of this result
   *         depends on the implementation of this interface: it can be a
   *         Fractal component reference (if this factory creates Fractal
   *         components), it can be an identifier (if this factory generates
   *         source code that will create components, instead of directly
   *         creating components), etc.
   * @throws ADLException if a problem occurs during the creation of the
   *           component.
   */
  public Object newComponent(final String name, final Map context)
      throws ADLException {
    for (final Factory factory : factoryItfs.values()) {
      try {
        return factory.newComponent(name, context);
      } catch (final ADLException e) {
      }
    }
    throw new CompilerError(GenericErrors.INTERNAL_ERROR,
        "Coud not instantiate the plugin '" + name + "'");
  }

  /*
   * (non-Javadoc)
   * @see org.objectweb.fractal.adl.Factory#newComponentType(java.lang.String,
   * java.util.Map)
   */
  public Object newComponentType(final String name, final Map context)
      throws ADLException {
    for (final Factory factory : factoryItfs.values()) {
      try {
        return factory.newComponentType(name, context);
      } catch (final ADLException e) {
      }
    }
    throw new CompilerError(GenericErrors.INTERNAL_ERROR,
        "Coud not instantiate the plugin '" + name + "'");
  }

  /*
   * (non-Javadoc)
   * @see
   * org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String
   * , java.lang.Object)
   */
  public void bindFc(final String clientItfName, final Object serverItf)
      throws NoSuchInterfaceException {
    if (clientItfName.startsWith("client-factory")) {
      factoryItfs.put(clientItfName, (Factory) serverItf);
    } else {
      throw new NoSuchInterfaceException("No such interface: '" + clientItfName
          + "'.");
    }
  }

  /*
   * (non-Javadoc)
   * @see org.objectweb.fractal.api.control.BindingController#listFc()
   */
  public String[] listFc() {
    return factoryItfs.keySet().toArray(new String[0]);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.
   * String)
   */
  public Object lookupFc(final String clientItfName)
      throws NoSuchInterfaceException {
    if (factoryItfs.containsKey(clientItfName)) {
      return factoryItfs.get(clientItfName);
    } else {
      throw new NoSuchInterfaceException("No such interface: '" + clientItfName
          + "'.");
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.
   * String)
   */
  public void unbindFc(final String clientItfName)
      throws NoSuchInterfaceException {
    if (clientItfName.startsWith("client-factory")) {
      factoryItfs.remove(clientItfName);
    } else {
      throw new NoSuchInterfaceException("No such interface: '" + clientItfName
          + "'.");
    }
  }

}
