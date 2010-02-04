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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;

import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.idl.ExtendedInterfaceDecorationHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class IDLBindingChecker implements BindingChecker, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_BINDING_CHECKER_ITF_NAME = "client-binding-checker";
  public BindingChecker      clientBindingCheckerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the BindingChecker interface
  // ---------------------------------------------------------------------------

  public void checkBinding(final Interface fromInterface,
      final Interface toInterface, final Binding binding, final Node locator)
      throws ADLException {
    clientBindingCheckerItf.checkBinding(fromInterface, toInterface, binding,
        locator);
    checkSignature(fromInterface, toInterface, locator);
  }

  public void checkFromCompositeToSubcomponentBinding(
      final Interface compositeInterface,
      final Interface subComponentInterface, final Binding binding,
      final Node locator) throws ADLException {
    clientBindingCheckerItf.checkFromCompositeToSubcomponentBinding(
        compositeInterface, subComponentInterface, binding, locator);
    checkSignature(compositeInterface, subComponentInterface, locator);
  }

  public void checkFromSubcomponentToCompositeBinding(
      final Interface subComponentInterface,
      final Interface compositeInterface, final Binding binding,
      final Node locator) throws ADLException {
    clientBindingCheckerItf.checkFromSubcomponentToCompositeBinding(
        subComponentInterface, compositeInterface, binding, locator);
    checkSignature(subComponentInterface, compositeInterface, locator);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected void checkSignature(final Interface from, final Interface to,
      final Node locator) throws ADLException {
    if ((from instanceof TypeInterface) && (to instanceof TypeInterface)) {
      final String fromSignature = ((TypeInterface) from).getSignature();
      final String toSignature = ((TypeInterface) to).getSignature();
      if (fromSignature.equals(toSignature)) {
        // same signature. binding is OK
        return;
      }
      // otherwise need to check if 'toSignature' extends 'fromSignature'
      final InterfaceDefinition toItf = InterfaceDefinitionDecorationHelper
          .getResolvedInterfaceDefinition((TypeInterface) to, null, null);
      final Set<String> extendedItf = ExtendedInterfaceDecorationHelper
          .getExtendedInterface(toItf);
      if (!extendedItf.contains(fromSignature)) {
        throw new ADLException(BindingErrors.INVALID_SIGNATURE, locator,
            new NodeErrorLocator(from), new NodeErrorLocator(to));
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_BINDING_CHECKER_ITF_NAME)) {
      clientBindingCheckerItf = (BindingChecker) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public String[] listFc() {
    return new String[]{CLIENT_BINDING_CHECKER_ITF_NAME};
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_BINDING_CHECKER_ITF_NAME)) {
      return clientBindingCheckerItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_BINDING_CHECKER_ITF_NAME)) {
      clientBindingCheckerItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
