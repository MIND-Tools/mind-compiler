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

import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.getInferredParameterType;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.setInferredParameterType;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.setUsedFormalParameter;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.ContextLocal;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

/**
 * This delegating {@link DefinitionReferenceResolver} checks that
 * {@link Argument} nodes contained by the {@link DefinitionReference} to
 * resolve, match {@link FormalParameter} contained by the resolved
 * {@link Definition}.
 */
public class ParametricDefinitionReferenceResolver
    extends
      AbstractDefinitionReferenceResolver {

  protected final ContextLocal<Map<Definition, Map<String, FormalParameter>>> contextualParameters = new ContextLocal<Map<Definition, Map<String, FormalParameter>>>();

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {

    return resolve(reference, encapsulatingDefinition, getParameters(
        encapsulatingDefinition, context), context);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<String, FormalParameter> formalParameters,
      final Map<Object, Object> context) throws ADLException {

    final Definition d = clientResolverItf.resolve(reference,
        encapsulatingDefinition, context);

    final Argument[] argumentValues = (reference instanceof ArgumentContainer)
        ? ((ArgumentContainer) reference).getArguments()
        : null;
    final FormalParameter[] refFormalParameters = (d instanceof FormalParameterContainer)
        ? ((FormalParameterContainer) d).getFormalParameters()
        : null;

    // Map argument values to formal parameters.
    // argumentMap associates formal parameter names to actual arguments;
    final Map<String, Argument> argumentMap = mapArguments(refFormalParameters,
        argumentValues, reference);

    if (argumentMap != null) {
      // referenced definition has formal parameters.

      // Check argument values
      for (final FormalParameter parameter : refFormalParameters) {
        final ParameterType type = getInferredParameterType(parameter);
        final Argument argumentValue = argumentMap.get(parameter.getName());
        final Value value = argumentValue.getValue();

        if (value instanceof Reference) {
          // the argument references a formal parameter
          final String ref = ((Reference) value).getRef();
          final FormalParameter referencedParameter = formalParameters.get(ref);
          if (referencedParameter == null) {
            throw new ADLException(ADLErrors.UNDEFINED_PARAMETER, value, ref);
          }
          setUsedFormalParameter(referencedParameter);

          final ParameterType referencedType = getInferredParameterType(referencedParameter);
          if (referencedType == null) {
            setInferredParameterType(referencedParameter, type);
          } else if (type != null && type != referencedType) {
            throw new ADLException(ADLErrors.INCOMPATIBLE_ARGUMENT_TYPE, value,
                ref);
          }
        } else if (value instanceof StringLiteral) {
          if (type != null && type != ParameterType.STRING) {
            throw new ADLException(ADLErrors.INCOMPATIBLE_ARGUMENT_VALUE,
                value, parameter.getName());
          }
        } else if (value instanceof NumberLiteral) {
          if (type != null && type != ParameterType.INTEGER) {
            throw new ADLException(ADLErrors.INCOMPATIBLE_ARGUMENT_VALUE,
                value, parameter.getName());
          }
        }
      }
    }

    return d;
  }

  protected Map<String, Argument> mapArguments(
      final FormalParameter[] parameters, final Argument[] arguments,
      final Node location) throws ADLException {
    if (parameters == null || parameters.length == 0) {
      if (arguments != null && arguments.length > 0) {
        throw new ADLException(ADLErrors.INVALID_REFERENCE_NO_PARAMETER,
            location);
      } else {
        return null;
      }

    } else {
      // there are parameters
      if (arguments == null || arguments.length == 0)
        throw new ADLException(ADLErrors.INVALID_REFERENCE_MISSING_ARGUMENT,
            location);

      if (arguments.length > 0 && arguments[0].getName() == null) {
        // argument values are specified by ordinal position.

        if (parameters.length > arguments.length) {
          // missing template values
          throw new ADLException(ADLErrors.INVALID_REFERENCE_MISSING_ARGUMENT,
              location);
        }

        if (parameters.length < arguments.length) {
          throw new ADLException(ADLErrors.INVALID_REFERENCE_TOO_MANY_ARGUMENT,
              location);
        }

        final Map<String, Argument> result = new HashMap<String, Argument>(
            parameters.length);

        for (int i = 0; i < parameters.length; i++) {
          final Argument value = arguments[i];
          // sanity check.
          if (value.getName() != null) {
            throw new CompilerError(GenericErrors.INTERNAL_ERROR,
                new NodeErrorLocator(value),
                "Cannot mix ordinal and name-based template values.");
          }

          final String varName = parameters[i].getName();

          value.setName(varName);
          result.put(varName, value);
        }

        return result;

      } else {
        // template values are specified by name

        final Map<String, Argument> valuesByName = new HashMap<String, Argument>(
            arguments.length);
        for (final Argument value : arguments) {
          // sanity check.
          if (value.getName() == null) {
            throw new CompilerError(GenericErrors.INTERNAL_ERROR,
                new NodeErrorLocator(value),
                "Cannot mix ordinal and name-based argument values.");
          }

          valuesByName.put(value.getName(), value);
        }

        final Map<String, Argument> result = new HashMap<String, Argument>();
        for (final FormalParameter variable : parameters) {
          final Argument value = valuesByName.remove(variable.getName());
          if (value == null) {
            // missing template values
            throw new ADLException(
                ADLErrors.INVALID_REFERENCE_MISSING_ARGUMENT, location,
                variable.getName());
          }
          result.put(variable.getName(), value);
        }
        if (!valuesByName.isEmpty()) {
          // too many template values

          // get the first one
          final Map.Entry<String, Argument> value = valuesByName.entrySet()
              .iterator().next();

          throw new ADLException(ADLErrors.INVALID_REFERENCE_NO_SUCH_PARAMETER,
              value.getValue(), value.getKey());
        }

        return result;
      }
    }
  }

  protected Map<String, FormalParameter> getParameters(final Definition d,
      final Map<Object, Object> context) throws ADLException {
    Map<Definition, Map<String, FormalParameter>> parameters = contextualParameters
        .get(context);
    if (parameters == null) {
      parameters = new IdentityHashMap<Definition, Map<String, FormalParameter>>();
      contextualParameters.set(context, parameters);
    }

    Map<String, FormalParameter> result = parameters.get(d);

    if (result == null) {
      if (d instanceof FormalParameterContainer) {
        final FormalParameter[] formalParameters = ((FormalParameterContainer) d)
            .getFormalParameters();
        if (formalParameters.length > 0) {

          result = new HashMap<String, FormalParameter>(formalParameters.length);
          for (final FormalParameter parameter : formalParameters) {
            if (result.put(parameter.getName(), parameter) != null) {
              throw new ADLException(
                  ADLErrors.DUPLICATED_TEMPALTE_VARIABLE_NAME, parameter,
                  parameter.getName());
            }
          }
        }
      }
      parameters.put(d, result);
    }

    return result;
  }
}
