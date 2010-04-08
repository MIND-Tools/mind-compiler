/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.adl.generic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;

public class InputResourceTemplateInstantiator
    implements
      TemplateInstantiator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #clientInstantiatorItf} client interface. */
  public static final String         CLIENT_INSTANTIATOR_ITF_NAME = "client-instantiator";

  /** The client {@link TemplateInstantiator} interface. */
  public TemplateInstantiator        clientInstantiatorItf;

  /** The interface used to resolve referenced definitions. */
  public DefinitionReferenceResolver definitionReferenceResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TemplateInstantiator interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    final Set<InputResource> inputResources = new HashSet<InputResource>();

    // for each formal type parameter of the generic definition
    for (final FormalTypeParameter formalTypeParameter : ((FormalTypeParameterContainer) genericDefinition)
        .getFormalTypeParameters()) {
      final String formalTypeParameterName = formalTypeParameter.getName();
      // get the given type argument value
      final Object typeArgumentValue = typeArgumentValues
          .get(formalTypeParameterName);
      assert typeArgumentValue != null;

      // if the type argument value is not a DefinitionReference, pass it.
      if (!(typeArgumentValue instanceof TypeArgument)) continue;
      final DefinitionReference typeArgumentDefRef = ((TypeArgument) typeArgumentValue)
          .getDefinitionReference();
      if (typeArgumentDefRef == null) continue;

      final Definition typeArgumentDef = definitionReferenceResolverItf
          .resolve(typeArgumentDefRef, null, context);
      assert typeArgumentDef != null;

      inputResources.addAll(InputResourcesHelper
          .getInputResources(typeArgumentDef));
    }

    // Instantiate template
    final Definition templateInstance = clientInstantiatorItf
        .instantiateTemplate(genericDefinition, typeArgumentValues, context);

    InputResourcesHelper.addInputResources(templateInstance, inputResources);

    return templateInstance;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = (TemplateInstantiator) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    return new String[]{DefinitionReferenceResolver.ITF_NAME,
        CLIENT_INSTANTIATOR_ITF_NAME};
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      return clientInstantiatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
