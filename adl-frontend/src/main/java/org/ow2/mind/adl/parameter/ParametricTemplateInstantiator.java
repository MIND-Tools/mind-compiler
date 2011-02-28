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

package org.ow2.mind.adl.parameter;

import static org.objectweb.fractal.adl.NodeUtil.cloneTree;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.turnsToArgumentContainer;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.turnsToParamContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.TemplateInstantiator.AbstractDelegatingTemplateInstantiator;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.value.ast.Reference;

import com.google.inject.Inject;

/**
 * This delegating {@link TemplateInstantiator} adds {@link FormalParameter
 * formal parameters} contained by the definitions designated by type arguments
 * to the instantiated template.
 */
public class ParametricTemplateInstantiator
    extends
      AbstractDelegatingTemplateInstantiator {

  @Inject
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

  @Inject
  protected NodeFactory                 nodeFactoryItf;

  @Inject
  protected NodeMerger                  nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TemplateInstantiator interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    // the list of FormalParameter to be added on the instantiated template.
    final List<FormalParameter> toBeAddedParameters = new ArrayList<FormalParameter>();

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

      // if the type argument value do not contains formal parameter, pass.
      if (!(typeArgumentDef instanceof FormalParameterContainer)) continue;

      final FormalParameter[] formalParameters = ((FormalParameterContainer) typeArgumentDef)
          .getFormalParameters();
      // if the type argument value do not contains formal parameter, pass.
      if (formalParameters.length == 0) continue;

      // The type argument value must be modified: an argument sub-node must be
      // added for each formal parameter. To avoid modification of given
      // DefinitionReference, clone it and ensure that it implements
      // ArgumentContainer.
      final TypeArgument clonedTypeArgument = cloneTree((TypeArgument) typeArgumentValue);
      final ArgumentContainer argumentContainer = turnsToArgumentContainer(
          clonedTypeArgument.getDefinitionReference(), nodeFactoryItf,
          nodeMergerItf);
      if (argumentContainer != clonedTypeArgument.getDefinitionReference())
        clonedTypeArgument
            .setDefinitionReference((DefinitionReference) argumentContainer);
      // replace the type argument value in given typeArgumentValues Map.
      typeArgumentValues.put(formalTypeParameterName, clonedTypeArgument);

      // remove present argument values (if any).
      for (final Argument argument : argumentContainer.getArguments())
        argumentContainer.removeArgument(argument);

      // add new arguments that reference the added formal parameters
      // for each formal parameter of the type argument.
      for (final FormalParameter formalParameter : formalParameters) {
        final String paramName = formalTypeParameterName + "$"
            + formalParameter.getName();

        // create a new formal parameter that will be added to the instantiated
        // template.
        final FormalParameter newFormalParameter = cloneTree(formalParameter);
        newFormalParameter.setName(paramName);
        toBeAddedParameters.add(newFormalParameter);

        // create a new argument that references the newly created formal
        // parameter.
        final Argument newArgument = newArgumentNode();
        newArgument.setName(formalParameter.getName());
        final Reference newReference = newReferenceNode();
        newReference.setRef(paramName);
        newArgument.setValue(newReference);
        argumentContainer.addArgument(newArgument);
      }
    }

    // Instantiate template
    Definition templateInstance = clientInstantiatorItf.instantiateTemplate(
        genericDefinition, typeArgumentValues, context);

    // add the formal parameters (if any).
    if (!toBeAddedParameters.isEmpty()) {
      final FormalParameterContainer formalParameterContainer = turnsToParamContainer(
          templateInstance, nodeFactoryItf, nodeMergerItf);
      if (templateInstance != formalParameterContainer)
        templateInstance = (Definition) formalParameterContainer;
      for (final FormalParameter formalParameter : toBeAddedParameters)
        formalParameterContainer.addFormalParameter(formalParameter);
    }

    return templateInstance;
  }

  protected Argument newArgumentNode() {
    try {
      return (Argument) nodeFactoryItf.newNode("argument",
          Argument.class.getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected Reference newReferenceNode() {
    try {
      return (Reference) nodeFactoryItf.newNode("value",
          Reference.class.getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }
}
