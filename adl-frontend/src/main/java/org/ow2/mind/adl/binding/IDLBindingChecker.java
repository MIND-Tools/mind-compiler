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

import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.binding.BindingChecker.AbstractDelegatingBindingChecker;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ExtendedInterfaceDecorationHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

import com.google.inject.Inject;

public class IDLBindingChecker extends AbstractDelegatingBindingChecker {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the BindingChecker interface
  // ---------------------------------------------------------------------------

  public boolean checkBinding(final Interface fromInterface,
      final Interface toInterface, final Binding binding, final Node locator)
      throws ADLException {
    final boolean isValid = clientBindingCheckerItf.checkBinding(fromInterface,
        toInterface, binding, locator);
    return checkSignature(fromInterface, toInterface, locator) && isValid;
  }

  public boolean checkFromCompositeToSubcomponentBinding(
      final Interface compositeInterface,
      final Interface subComponentInterface, final Binding binding,
      final Node locator) throws ADLException {
    final boolean isValid = clientBindingCheckerItf
        .checkFromCompositeToSubcomponentBinding(compositeInterface,
            subComponentInterface, binding, locator);
    return checkSignature(compositeInterface, subComponentInterface, locator)
        && isValid;
  }

  public boolean checkFromSubcomponentToCompositeBinding(
      final Interface subComponentInterface,
      final Interface compositeInterface, final Binding binding,
      final Node locator) throws ADLException {
    final boolean isValid = clientBindingCheckerItf
        .checkFromSubcomponentToCompositeBinding(subComponentInterface,
            compositeInterface, binding, locator);
    return checkSignature(subComponentInterface, compositeInterface, locator)
        && isValid;
  }

  public boolean checkCompatibility(final Interface from, final Interface to,
      final Node locator) throws ADLException {
    final boolean isValid = clientBindingCheckerItf.checkCompatibility(from,
        to, locator);
    return checkSignature(from, to, locator) && isValid;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected boolean checkSignature(final Interface from, final Interface to,
      final Node locator) throws ADLException {
    if ((from instanceof TypeInterface) && (to instanceof TypeInterface)) {
      final String fromSignature = ((TypeInterface) from).getSignature();
      final String toSignature = ((TypeInterface) to).getSignature();
      if (fromSignature.equals(toSignature)) {
        // same signature. binding is OK
        return true;
      }
      // otherwise need to check if 'toSignature' extends 'fromSignature'
      final InterfaceDefinition toItf = InterfaceDefinitionDecorationHelper
          .getResolvedInterfaceDefinition((TypeInterface) to, null, null);
      final Set<String> extendedItf = ExtendedInterfaceDecorationHelper
          .getExtendedInterface(toItf);
      if (!extendedItf.contains(fromSignature)) {
        errorManagerItf.logError(BindingErrors.INVALID_SIGNATURE, locator,
            new NodeErrorLocator(from), new NodeErrorLocator(to));
        return false;
      }
    }
    return true;
  }
}
