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

package org.ow2.mind.adl.implementation;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.PathHelper.fullyQualifiedNameToAbsolute;
import static org.ow2.mind.PathHelper.isRelative;
import static org.ow2.mind.PathHelper.isValid;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;

public class ImplementationLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public ImplementationLocator implementationLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition def = clientLoader.load(name, context);
    if (def instanceof ImplementationContainer) {
      processImplementation(def, (ImplementationContainer) def, context);
    }
    return def;
  }

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected void processImplementation(final Definition def,
      final ImplementationContainer container, final Map<Object, Object> context)
      throws ADLException {
    final Data data = container.getData();
    if (data != null) {
      processData(def, data, context);
    }

    for (final Source src : container.getSources()) {
      processSrc(def, src, context);
    }
  }

  protected void processData(final Definition def, final Data data,
      final Map<Object, Object> context) throws ADLException {
    String path = data.getPath();
    if (path != null) {
      if (!isValid(path))
        throw new ADLException(ADLErrors.INVALID_PATH, data, path);

      if (isRelative(path)) {
        path = fullyQualifiedNameToAbsolute(def.getName(), path);
        data.setPath(path);
      }

      if (implementationLocatorItf.findSource(path, context) == null) {
        throw new ADLException(ADLErrors.SOURCE_NOT_FOUND, data, path);
      }
    }
  }

  protected void processSrc(final Definition def, final Source src,
      final Map<Object, Object> context) throws ADLException {
    String path = src.getPath();
    if (path != null) {
      if (!isValid(path))
        throw new ADLException(ADLErrors.INVALID_PATH, src, path);

      if (isRelative(path)) {
        path = fullyQualifiedNameToAbsolute(def.getName(), path);
        src.setPath(path);
      }

      if (implementationLocatorItf.findSource(path, context) == null) {
        throw new ADLException(ADLErrors.SOURCE_NOT_FOUND, src, path);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ImplementationLocator.ITF_NAME)) {
      this.implementationLocatorItf = (ImplementationLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), ImplementationLocator.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ImplementationLocator.ITF_NAME)) {
      return this.implementationLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ImplementationLocator.ITF_NAME)) {
      this.implementationLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
