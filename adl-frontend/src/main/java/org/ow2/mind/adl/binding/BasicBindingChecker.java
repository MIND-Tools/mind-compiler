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
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

public class BasicBindingChecker implements BindingChecker {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the BindingChecker interface
  // ---------------------------------------------------------------------------

  public boolean checkBinding(final Interface fromInterface,
      final Interface toInterface, final Binding binding, final Node locator)
      throws ADLException {
    boolean isValid = true;
    if (!isClient(fromInterface)) {
      errorManagerItf
          .logError(BindingErrors.INVALID_FROM_NOT_A_CLIENT, locator,
              fromInterface.getName(), new NodeErrorLocator(fromInterface));
      isValid = false;
    }
    if (!isServer(toInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_TO_NOT_A_SERVER, locator,
          toInterface.getName(), new NodeErrorLocator(toInterface));
      isValid = false;
    }

    if (TypeInterfaceUtil.isMandatory(fromInterface)
        && TypeInterfaceUtil.isOptional(toInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, fromInterface.getName(), toInterface.getName());
      isValid = false;
    }

    final boolean singleFromInterface = isSingleton(fromInterface)
        || (isCollection(fromInterface) && binding.getFromInterfaceNumber() != null);
    final boolean singleToInterface = isSingleton(toInterface)
        || (isCollection(toInterface) && binding.getToInterfaceNumber() != null);

    // only single-to-single or multi-to-multi is allowed.
    if (singleFromInterface != singleToInterface) {
      errorManagerItf.logError(ADLErrors.INVALID_BINDING_CARDINALITY, locator);
      isValid = false;
    }

    if (!singleFromInterface) {
      // multi-to-multi binding
      assert !singleToInterface;
      final int fromSize = getNumberOfElement(fromInterface);
      final int toSize = getNumberOfElement(toInterface);
      if (fromSize > toSize) {
        // if there are more client interfaces than server interfaces
        if (isMandatory(fromInterface)) {
          errorManagerItf.logError(ADLErrors.INVALID_BINDING_COLLECTION_SIZE,
              locator, fromInterface.getName(), fromSize,
              toInterface.getName(), toSize);
          isValid = false;
        }
      }
    }
    return isValid;
  }

  public boolean checkFromCompositeToSubcomponentBinding(
      final Interface compositeInterface,
      final Interface subComponentInterface, final Binding binding,
      final Node locator) throws ADLException {
    boolean isValid = true;
    if (!isClient(compositeInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_FROM_INTERNAL, locator,
          compositeInterface.getName(),
          new NodeErrorLocator(compositeInterface));
      isValid = false;
    }

    if (!isServer(subComponentInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_TO_NOT_A_SERVER, locator,
          subComponentInterface.getName(), new NodeErrorLocator(
              subComponentInterface));
      isValid = false;
    }

    if (TypeInterfaceUtil.isMandatory(compositeInterface)
        && TypeInterfaceUtil.isOptional(subComponentInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, compositeInterface.getName(),
          subComponentInterface.getName());
      isValid = false;
    }

    final boolean singleFromInterface = isSingleton(compositeInterface)
        || (isCollection(compositeInterface) && binding
            .getFromInterfaceNumber() != null);
    final boolean singleToInterface = isSingleton(subComponentInterface)
        || (isCollection(subComponentInterface) && binding
            .getToInterfaceNumber() != null);

    // only single-to-single or multi-to-multi is allowed.
    if (singleFromInterface != singleToInterface) {
      errorManagerItf.logError(ADLErrors.INVALID_BINDING_CARDINALITY, locator);
      isValid = false;
    }

    if (!singleFromInterface) {
      assert !singleToInterface;
      final int compositeSize = getNumberOfElement(compositeInterface);
      final int subCompSize = getNumberOfElement(subComponentInterface);
      if (compositeSize > subCompSize) {
        // if there are more interfaces on composite side than in
        // sub-component side, so some of the interfaces of the composite side
        // can't be bound to the sub-component.
        if (isMandatory(compositeInterface)) {
          errorManagerItf.logError(ADLErrors.INVALID_BINDING_COLLECTION_SIZE,
              locator, compositeInterface.getName(), compositeSize,
              subComponentInterface.getName(), subCompSize);
          isValid = false;
        }

      }
    }
    return isValid;
  }

  public boolean checkFromSubcomponentToCompositeBinding(
      final Interface subComponentInterface,
      final Interface compositeInterface, final Binding binding,
      final Node locator) throws ADLException {
    boolean isValid = true;

    if (!isClient(subComponentInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_FROM_NOT_A_CLIENT,
          locator, subComponentInterface.getName(), new NodeErrorLocator(
              subComponentInterface));
      isValid = false;
    }

    if (!isServer(compositeInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_TO_INTERNAL, locator,
          compositeInterface.getName(),
          new NodeErrorLocator(compositeInterface));
      isValid = false;
    }

    if (TypeInterfaceUtil.isMandatory(subComponentInterface)
        && TypeInterfaceUtil.isOptional(compositeInterface)) {
      errorManagerItf.logError(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, subComponentInterface.getName(),
          compositeInterface.getName());
      isValid = false;
    }
    final boolean singleFromInterface = isSingleton(subComponentInterface)
        || (isCollection(subComponentInterface) && binding
            .getFromInterfaceNumber() != null);
    final boolean singleToInterface = isSingleton(compositeInterface)
        || (isCollection(compositeInterface) && binding.getToInterfaceNumber() != null);

    // only single-to-single or multi-to-multi is allowed.
    if (singleFromInterface != singleToInterface) {
      errorManagerItf.logError(ADLErrors.INVALID_BINDING_CARDINALITY, locator);
      isValid = false;
    }

    if (!singleFromInterface) {
      assert !singleToInterface;
      final int compositeSize = getNumberOfElement(compositeInterface);
      final int subCompSize = getNumberOfElement(subComponentInterface);
      if (subCompSize > compositeSize) {
        // if there are more interfaces on sub-component side than in
        // composite side, so some of the interfaces of the sub-component side
        // can't be bound to the composite.
        if (isMandatory(subComponentInterface)) {
          errorManagerItf.logError(ADLErrors.INVALID_BINDING_COLLECTION_SIZE,
              locator, subComponentInterface.getName(), subCompSize,
              compositeInterface.getName(), compositeSize);
          isValid = false;
        }

      }
    }
    return isValid;
  }

  public boolean checkCompatibility(final Interface from, final Interface to,
      final Node locator) throws ADLException {
    boolean isValid = true;
    if (TypeInterfaceUtil.isMandatory(from) && TypeInterfaceUtil.isOptional(to)) {
      errorManagerItf.logError(BindingErrors.INVALID_MANDATORY_TO_OPTIONAL,
          locator, from.getName(), to.getName());
      isValid = false;
    }

    final boolean singleFromInterface = isSingleton(from);
    final boolean singleToInterface = isSingleton(to);

    // only single-to-single or multi-to-multi is allowed.
    if (singleFromInterface != singleToInterface) {
      errorManagerItf.logError(ADLErrors.INVALID_BINDING_CARDINALITY, locator);
      isValid = false;
    }

    if (!singleFromInterface) {
      // multi-to-multi binding
      assert !singleToInterface;
      final int fromSize = getNumberOfElement(from);
      final int toSize = getNumberOfElement(to);
      if (fromSize > toSize) {
        // if there are more client interfaces than server interfaces
        if (isMandatory(from)) {
          errorManagerItf.logError(ADLErrors.INVALID_BINDING_COLLECTION_SIZE,
              locator, from.getName(), fromSize, to.getName(), toSize);
          isValid = false;
        }
      }
    }
    return isValid;
  }
}
