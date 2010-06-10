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
import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isCollection;
import static org.ow2.mind.adl.ast.ASTHelper.getNumberOfElement;
import static org.ow2.mind.adl.ast.ASTHelper.getResolvedComponentDefinition;
import static org.ow2.mind.adl.ast.Binding.THIS_COMPONENT;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;
import org.ow2.mind.adl.membrane.ast.MembraneASTHelper;

public class BindingCheckerLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public BindingChecker bindingCheckerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    if (d instanceof BindingContainer)
      checkBindings((BindingContainer) d, context);
    return d;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected void checkBindings(final BindingContainer container,
      final Map<Object, Object> context) throws ADLException {
    final Binding[] bindings = container.getBindings();
    if (bindings.length == 0) return;

    final Component[] subComponents = castNodeError(container,
        ComponentContainer.class).getComponents();
    final Map<String, Map<String, Interface>> subComponentInterfaces = new HashMap<String, Map<String, Interface>>(
        subComponents.length);
    for (final Component subComponent : subComponents) {
      final Definition subCompDef = getResolvedComponentDefinition(
          subComponent, null, context);
      assert subCompDef != null;

      final Interface[] interfaces = castNodeError(subCompDef,
          InterfaceContainer.class).getInterfaces();
      final Map<String, Interface> subComponentItfs = new HashMap<String, Interface>(
          interfaces.length);
      for (final Interface itf : interfaces) {
        subComponentItfs.put(itf.getName(), itf);
      }
      subComponentInterfaces.put(subComponent.getName(), subComponentItfs);
    }

    // add composite interfaces
    final Map<String, Interface> componentItfs = new HashMap<String, Interface>();
    subComponentInterfaces.put(null, componentItfs);
    // first add internal interfaces
    final Interface[] interfaces = castNodeError(container,
        InternalInterfaceContainer.class).getInternalInterfaces();
    for (final Interface itf : interfaces) {
      componentItfs.put(itf.getName(), itf);
    }
    // then add server interfaces of controllers
    final Controller[] controllers = castNodeError(container,
        ControllerContainer.class).getControllers();
    for (final Controller controller : controllers) {
      for (final ControllerInterface ctrlItf : controller
          .getControllerInterfaces()) {
        if (!componentItfs.containsKey(ctrlItf.getName())
            && !MembraneASTHelper.isInternalInterface(ctrlItf)) {
          final Interface itf = ASTHelper.getInterface(container,
              ctrlItf.getName());
          if (itf != null) {
            componentItfs.put(ctrlItf.getName(), itf);
          }
        }
      }
    }

    for (final Binding binding : bindings) {

      final Interface from = getInterface(binding, binding.getFromComponent(),
          binding.getFromInterface(), binding.getFromInterfaceNumber(),
          subComponentInterfaces);
      final Interface to = getInterface(binding, binding.getToComponent(),
          binding.getToInterface(), binding.getToInterfaceNumber(),
          subComponentInterfaces);

      if (THIS_COMPONENT.equals(binding.getFromComponent())) {
        bindingCheckerItf.checkFromCompositeToSubcomponentBinding(from, to,
            binding, binding);
      } else if (THIS_COMPONENT.equals(binding.getToComponent())) {
        bindingCheckerItf.checkFromSubcomponentToCompositeBinding(from, to,
            binding, binding);
      } else {
        bindingCheckerItf.checkBinding(from, to, binding, binding);
      }
    }
  }

  protected Interface getInterface(final Binding binding,
      final String componentName, final String interfaceName,
      final String interfaceNumber,
      final Map<String, Map<String, Interface>> subComponentInterfaces)
      throws ADLException {
    final Map<String, Interface> interfaces;

    if (Binding.THIS_COMPONENT.equals(componentName)) {
      interfaces = subComponentInterfaces.get(null);
    } else {
      interfaces = subComponentInterfaces.get(componentName);
      if (interfaces == null) {
        throw new ADLException(BindingErrors.INVALID_ITF_NO_SUCH_COMPONENT,
            binding, componentName);
      }
    }

    final Interface itf = interfaces.get(interfaceName);
    if (itf == null) {
      throw new ADLException(BindingErrors.INVALID_ITF_NO_SUCH_INTERFACE,
          binding, componentName, interfaceName);
    }

    if (interfaceNumber != null) {
      if (!isCollection(itf)) {
        throw new ADLException(BindingErrors.INVALID_ITF_NO_SUCH_INTERFACE,
            binding, componentName, interfaceName + "[" + interfaceNumber + "]");
      }

      final int nbElement = getNumberOfElement(itf);
      if (nbElement != -1) {
        final int itfNumber = parseInt(interfaceNumber);
        if (itfNumber < 0 || itfNumber >= nbElement) {
          throw new ADLException(BindingErrors.INVALID_ITF_NO_SUCH_INTERFACE,
              binding, componentName, interfaceName + "[" + interfaceNumber
                  + "]");
        }
      }
    }
    return itf;
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

    if (itfName.equals(BindingChecker.ITF_NAME)) {
      bindingCheckerItf = (BindingChecker) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 1];
    list[0] = BindingChecker.ITF_NAME;
    System.arraycopy(superList, 0, list, 1, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(BindingChecker.ITF_NAME)) {
      return bindingCheckerItf;
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

    if (itfName.equals(BindingChecker.ITF_NAME)) {
      bindingCheckerItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
