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

import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isClient;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isCollection;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isMandatory;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isServer;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isSingleton;
import static org.ow2.mind.adl.ast.ASTHelper.getNumberOfElement;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.Binding;

public class BasicBindingChecker implements BindingChecker {

  public void checkBinding(final Interface fromInterface,
      final Interface toInterface, final Binding binding, final Node locator)
      throws ADLException {
    if (!isClient(fromInterface)) {
      throw new ADLException(BindingErrors.INVALID_FROM_NOT_A_CLIENT, locator,
          fromInterface.getName(), new NodeErrorLocator(fromInterface));
    }
    if (!isServer(toInterface)) {
      throw new ADLException(BindingErrors.INVALID_TO_NOT_A_SERVER, locator,
          toInterface.getName(), new NodeErrorLocator(toInterface));
    }

    if (TypeInterfaceUtil.isMandatory(fromInterface)
        && TypeInterfaceUtil.isOptional(toInterface)) {
      throw new ADLException(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, fromInterface.getName(), toInterface.getName());
    }

    final boolean singleFromInterface = isSingleton(fromInterface)
        || (isCollection(fromInterface) && binding.getFromInterfaceNumber() != null);
    final boolean singleToInterface = isSingleton(toInterface)
        || (isCollection(toInterface) && binding.getToInterfaceNumber() != null);

    // only single-to-single or multi-to-multi is allowed.
    if (singleFromInterface != singleToInterface)
    // TODO use a specific error
      throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, locator,
          fromInterface.getName());

    if (!singleFromInterface) {
      // multi-to-multi binding
      assert !singleToInterface;
      final int fromSize = getNumberOfElement(fromInterface);
      final int toSize = getNumberOfElement(toInterface);
      if (fromSize > toSize) {
        // if there are more client interfaces than server interfaces
        if (isMandatory(fromInterface))
        // TODO use a specific error
          throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, locator,
              fromInterface.getName());
      }
    }

    // TODO check IDL compatibility
  }

  public void checkFromCompositeToSubcomponentBinding(
      final Interface compositeInterface,
      final Interface subComponentInterface, final Binding binding,
      final Node locator) throws ADLException {
    if (!isServer(compositeInterface)) {
      throw new ADLException(BindingErrors.INVALID_FROM_INTERNAL, locator,
          compositeInterface.getName(),
          new NodeErrorLocator(compositeInterface));
    }

    if (!isServer(subComponentInterface)) {
      throw new ADLException(BindingErrors.INVALID_TO_NOT_A_SERVER, locator,
          subComponentInterface.getName(), new NodeErrorLocator(
              subComponentInterface));
    }

    if (TypeInterfaceUtil.isMandatory(compositeInterface)
        && TypeInterfaceUtil.isOptional(subComponentInterface)) {
      throw new ADLException(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, compositeInterface.getName(), subComponentInterface
              .getName());
    }

    if (binding != null) {
      final boolean singleFromInterface = isSingleton(compositeInterface)
          || (isCollection(compositeInterface) && binding
              .getFromInterfaceNumber() != null);
      final boolean singleToInterface = isSingleton(subComponentInterface)
          || (isCollection(subComponentInterface) && binding
              .getToInterfaceNumber() != null);

      // only single-to-single or multi-to-multi is allowed.
      if (singleFromInterface != singleToInterface)
      // TODO use a specific error
        throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, locator,
            compositeInterface.getName());

      if (!singleFromInterface) {
        assert !singleToInterface;
        final int compositeSize = getNumberOfElement(compositeInterface);
        final int subCompSize = getNumberOfElement(subComponentInterface);
        if (compositeSize > subCompSize) {
          // if there are more interfaces on composite side than in
          // sub-component side, so some of the interfaces of the composite side
          // can't be bound to the sub-component.
          if (isMandatory(compositeInterface))
          // TODO use a specific error
            throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, locator,
                compositeInterface.getName());

        }
      }
    }

    // TODO check IDL compatibility
  }

  public void checkFromSubcomponentToCompositeBinding(
      final Interface subComponentInterface,
      final Interface compositeInterface, final Binding binding,
      final Node locator) throws ADLException {

    if (!isClient(subComponentInterface)) {
      throw new ADLException(BindingErrors.INVALID_FROM_NOT_A_CLIENT, locator,
          subComponentInterface.getName(), new NodeErrorLocator(
              subComponentInterface));
    }

    if (!isClient(compositeInterface)) {
      throw new ADLException(BindingErrors.INVALID_TO_INTERNAL, locator,
          compositeInterface.getName(),
          new NodeErrorLocator(compositeInterface));
    }

    if (TypeInterfaceUtil.isMandatory(subComponentInterface)
        && TypeInterfaceUtil.isOptional(compositeInterface)) {
      throw new ADLException(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, subComponentInterface.getName(), compositeInterface
              .getName());
    }

    if (binding != null) {
      final boolean singleFromInterface = isSingleton(subComponentInterface)
          || (isCollection(subComponentInterface) && binding
              .getFromInterfaceNumber() != null);
      final boolean singleToInterface = isSingleton(compositeInterface)
          || (isCollection(compositeInterface) && binding
              .getToInterfaceNumber() != null);

      // only single-to-single or multi-to-multi is allowed.
      if (singleFromInterface != singleToInterface)
        throw new ADLException(BindingErrors.INVALID_TO_SYNTAX, locator,
            compositeInterface.getName());

      if (!singleFromInterface) {
        assert !singleToInterface;
        final int compositeSize = getNumberOfElement(compositeInterface);
        final int subCompSize = getNumberOfElement(subComponentInterface);
        if (subCompSize > compositeSize) {
          // if there are more interfaces on sub-component side than in
          // composite side, so some of the interfaces of the sub-component side
          // can't be bound to the composite.
          if (isMandatory(subComponentInterface))
          // TODO use a specific error
            throw new ADLException(BindingErrors.INVALID_FROM_SYNTAX, locator,
                subComponentInterface.getName());

        }
      }
    }

    // TODO check IDL compatibility
  }

}
