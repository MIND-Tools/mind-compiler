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
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.TemplateInstantiator.AbstractDelegatingTemplateInstantiator;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;

import com.google.inject.Inject;

public class InputResourceTemplateInstantiator
    extends
      AbstractDelegatingTemplateInstantiator {

  @Inject
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

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
}
