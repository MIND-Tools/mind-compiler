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

package org.ow2.mind.adl.binding;

import static java.lang.Integer.parseInt;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isClient;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isCollection;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isMandatory;
import static org.ow2.mind.adl.ast.ASTHelper.getNumberOfElement;
import static org.ow2.mind.adl.ast.ASTHelper.getResolvedComponentDefinition;
import static org.ow2.mind.adl.ast.Binding.THIS_COMPONENT;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;

public class UnboundInterfaceCheckerLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #recursiveLoaderItf} client interface. */
  public static final String RECURSIVE_LOADER_ITF_NAME = "recursive-loader";

  /** The loader interface used to load referenced definition if needed. */
  public Loader              recursiveLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    if (d instanceof ComponentContainer) {
      checkUnboundInterfaces((ComponentContainer) d, context);
    }
    return d;
  }

  protected void checkUnboundInterfaces(final ComponentContainer container,
      final Map<Object, Object> context) throws ADLException {
    final Binding[] bindings = (container instanceof BindingContainer)
        ? ((BindingContainer) container).getBindings()
        : new Binding[0];

    // first check internal client interfaces
    if (container instanceof InternalInterfaceContainer) {
      for (final Interface itf : ((InternalInterfaceContainer) container)
          .getInternalInterfaces()) {
        if (isClient(itf) && isMandatory(itf)) {
          if (findBinding(bindings, THIS_COMPONENT, itf) == null)
            throw new ADLException(
                BindingErrors.UNBOUND_COMPOSITE_SERVER_INTERFACE, itf, itf
                    .getName(), ((Definition) container).getName());
        }
      }
    }

    // then check client interfaces of sub components
    for (final Component subComponent : container.getComponents()) {
      final Definition subCompDef = getResolvedComponentDefinition(
          subComponent, recursiveLoaderItf, context);
      assert subCompDef != null;

      if (subCompDef instanceof InterfaceContainer) {
        for (final Interface itf : ((InterfaceContainer) subCompDef)
            .getInterfaces()) {
          if (isClient(itf) && isMandatory(itf)) {
            if (findBinding(bindings, subComponent.getName(), itf) == null)
              throw new ADLException(BindingErrors.UNBOUND_CLIENT_INTERFACE,
                  subComponent, itf.getName(), subComponent.getName());
          }
        }
      }
    }
  }

  protected Binding findBinding(final Binding[] bindings,
      final String componentName, final Interface itf) {
    if (isCollection(itf)) {
      final int noe = getNumberOfElement(itf);
      if (noe == -1) {
        return findBinding(bindings, componentName, itf.getName(), -1);
      } else {
        // the interface is a collection interface
        // first checks if a global binding is defined on the interface

        Binding b = findBinding(bindings, componentName, itf.getName(), -1);
        if (b != null) {
          // there is a global binding for the defined interface
          return b;
        }

        // checks that every interfaces of the collection are bound
        assert noe > 0;
        for (int i = 0; i < noe; i++) {
          b = findBinding(bindings, componentName, itf.getName(), i);
          if (b == null) return null;
        }
        return b;
      }
    } else {
      return findBinding(bindings, componentName, itf.getName(), -1);
    }
  }

  protected Binding findBinding(final Binding[] bindings,
      final String componentName, final String itfName, final int index) {
    for (final Binding binding : bindings) {
      if (binding.getFromComponent().equals(componentName)
          && binding.getFromInterface().equals(itfName)) {
        if (index == -1) {
          return binding;
        }
        if (binding.getFromInterfaceNumber() != null) {
          final int fromItfNum = parseInt(binding.getFromInterfaceNumber());
          if (fromItfNum == index) return binding;
        }
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(RECURSIVE_LOADER_ITF_NAME)) {
      recursiveLoaderItf = (Loader) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 1];
    list[0] = RECURSIVE_LOADER_ITF_NAME;
    System.arraycopy(superList, 0, list, 1, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(RECURSIVE_LOADER_ITF_NAME)) {
      return recursiveLoaderItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(RECURSIVE_LOADER_ITF_NAME)) {
      recursiveLoaderItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
