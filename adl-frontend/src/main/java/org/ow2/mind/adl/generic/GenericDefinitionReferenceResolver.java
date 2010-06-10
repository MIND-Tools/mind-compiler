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

package org.ow2.mind.adl.generic;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isClient;
import static org.objectweb.fractal.adl.types.TypeInterfaceUtil.isServer;
import static org.ow2.mind.adl.ast.ASTHelper.isAbstract;
import static org.ow2.mind.adl.ast.ASTHelper.isType;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setUsedTypeParameter;

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
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.binding.BindingChecker;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;

/**
 * Delegating {@link DefinitionReferenceResolver} component that instantiates
 * generic definitions if the given {@link DefinitionReference} contains
 * {@link TypeArgument}.<br>
 * This component checks that type arguments of the definition reference match
 * correctly formal type parameters of the referenced definition and then
 * instantiates it.
 * 
 * @see TemplateInstantiator
 * @see TemplateInstantiatorImpl
 */
public class GenericDefinitionReferenceResolver
    extends
      AbstractDefinitionReferenceResolver {

  protected final ContextLocal<Map<Definition, Map<String, FormalTypeParameter>>> contextualTypeParameters    = new ContextLocal<Map<Definition, Map<String, FormalTypeParameter>>>();

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #recursiveResolverItf} client interface. */
  public static final String                                                      RECURSIVE_RESOLVER_ITF_NAME = "rescursive-resolver";

  /**
   * The DefinitionReferenceResolver interface used to resolve type argument
   * value.
   */
  public DefinitionReferenceResolver                                              recursiveResolverItf;

  /**
   * The interface used to check interface compatibility when instantiating
   * templates.
   */
  public BindingChecker                                                           bindingCheckerItf;

  /** The interface used to actually instantiate generic definitions. */
  public TemplateInstantiator                                                     templateInstantiatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    return resolve(reference, encapsulatingDefinition,
        getTypeParameters(encapsulatingDefinition, context), context);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<String, FormalTypeParameter> topLevelTypeParameters,
      final Map<Object, Object> context) throws ADLException {

    // delegate resolution of definition reference to retrieve the referenced
    // definition.
    Definition d = clientResolverItf.resolve(reference,
        encapsulatingDefinition, context);

    // Instantiate referenced definition (if it is actually a generic
    // definition).
    final TypeArgument[] typeArguments = (reference instanceof TypeArgumentContainer)
        ? ((TypeArgumentContainer) reference).getTypeArguments()
        : null;
    final FormalTypeParameter[] formalTypeParameters = (d instanceof FormalTypeParameterContainer)
        ? ((FormalTypeParameterContainer) d).getFormalTypeParameters()
        : null;

    if (formalTypeParameters == null || formalTypeParameters.length == 0) {
      if (typeArguments != null && typeArguments.length > 0) {
        throw new ADLException(
            ADLErrors.INVALID_REFERENCE_NO_TEMPLATE_VARIABLE, reference,
            d.getName());
      }
    } else {
      // referenced definition has type parameter

      // First, map type arguments to formal type parameters.
      // typeArgumentMap associates formal type parameter names to type
      // arguments;
      final Map<String, TypeArgument> typeArgumentMap = mapTypeArguments(
          reference, formalTypeParameters, typeArguments);

      // typeArgumentValues associates formal type parameter names to type
      // argument values (either a TypeArgument, or a referenced
      // FormalTypeParameter).
      final Map<String, Object> typeArgumentValues = new HashMap<String, Object>();

      // Second, check type argument values
      for (final FormalTypeParameter formalTypeParameter : formalTypeParameters) {
        final TypeArgument typeArgument = typeArgumentMap
            .get(formalTypeParameter.getName());
        assert typeArgument != null;
        final Definition formalTypeParamterType;
        if (formalTypeParameter.getDefinitionReference() != null) {
          formalTypeParamterType = recursiveResolverItf.resolve(
              formalTypeParameter.getDefinitionReference(), d, context);
          assert formalTypeParamterType != null;
        } else {
          // If the formal type parameter do not define a type to which it must
          // conform, do not check type compatibility. This can happen for
          // special definitions like "Factory".
          formalTypeParamterType = null;
        }

        // determine the definition of the type argument
        final Definition typeArgumentDefinition;
        if (typeArgument.getTypeParameterReference() != null) {
          // The type argument references a formal type parameter
          final String ref = typeArgument.getTypeParameterReference();

          final FormalTypeParameter referencedParameter = topLevelTypeParameters
              .get(ref);
          if (referencedParameter == null) {
            throw new ADLException(ADLErrors.UNDEFINED_TEMPALTE_VARIABLE,
                typeArgument, ref);
          }

          setUsedTypeParameter(referencedParameter);
          typeArgumentDefinition = recursiveResolverItf.resolve(
              referencedParameter.getDefinitionReference(),
              encapsulatingDefinition, context);
          assert typeArgumentDefinition != null;

          typeArgumentValues.put(formalTypeParameter.getName(),
              referencedParameter);
        } else {
          typeArgumentValues.put(formalTypeParameter.getName(), typeArgument);
          final DefinitionReference typeArgumentDefRef = typeArgument
              .getDefinitionReference();
          if (typeArgumentDefRef != null) {
            // The type argument references a definition.
            typeArgumentDefinition = recursiveResolverItf.resolve(
                typeArgumentDefRef, encapsulatingDefinition, context);

            if ((isType(typeArgumentDefinition) || isAbstract(typeArgumentDefinition))
                && encapsulatingDefinition != null) {
              throw new ADLException(
                  ADLErrors.INVALID_TEMPLATE_VALUE_TYPE_DEFINITON,
                  typeArgument, typeArgumentDefRef.getName());
            }

          } else {
            // The value of the TypeArgument is ANY.
            typeArgumentDefinition = null;
          }
        }

        // check compatibility of typeArgumentDefinition against type of formal
        // type parameter
        if (typeArgumentDefinition != null && formalTypeParamterType != null)
          checkTypeCompatibility(formalTypeParamterType,
              typeArgumentDefinition, typeArgument);

      }

      // Finally instantiate template
      d = templateInstantiatorItf.instantiateTemplate(d, typeArgumentValues,
          context);

      // update referenced name
      reference.setName(d.getName());

    }
    return d;
  }

  protected Definition getTypeArgumentDefinition(
      final Definition encapsulatingDefinition,
      final TypeArgument typeArgument, final Map<Object, Object> context)
      throws ADLException {
    final DefinitionReference typeArgumentDefRef = typeArgument
        .getDefinitionReference();
    if (typeArgumentDefRef != null) {
      // The type argument references a definition.
      return recursiveResolverItf.resolve(typeArgumentDefRef,
          encapsulatingDefinition, context);

    } else {
      // The value of the TypeArgument is ANY.
      return null;
    }
  }

  protected Map<String, TypeArgument> mapTypeArguments(
      final DefinitionReference reference,
      final FormalTypeParameter[] formalTypeParameters,
      final TypeArgument[] typeArguments) throws ADLException {
    if (typeArguments == null || typeArguments.length == 0) {
      if (formalTypeParameters.length > 0) {
        throw new ADLException(
            ADLErrors.INVALID_REFERENCE_MISSING_TEMPLATE_VALUE, reference);
      } else {
        return new HashMap<String, TypeArgument>();
      }
    }

    if (typeArguments[0].getTypeParameterName() == null) {
      // template values are specified by ordinal position.

      if (formalTypeParameters.length > typeArguments.length) {
        // missing template values
        throw new ADLException(
            ADLErrors.INVALID_REFERENCE_MISSING_TEMPLATE_VALUE, reference);
      }

      if (formalTypeParameters.length < typeArguments.length) {
        throw new ADLException(
            ADLErrors.INVALID_REFERENCE_TOO_MANY_TEMPLATE_VALUE, reference);
      }

      final Map<String, TypeArgument> result = new HashMap<String, TypeArgument>(
          formalTypeParameters.length);
      for (int i = 0; i < formalTypeParameters.length; i++) {
        final TypeArgument typeArgument = typeArguments[i];
        // sanity check.
        if (typeArgument.getTypeParameterName() != null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              new NodeErrorLocator(typeArgument),
              "Cannot mix ordinal and name-based template values.");
        }

        final String varName = formalTypeParameters[i].getName();
        typeArgument.setTypeParameterName(varName);
        result.put(varName, typeArgument);
      }

      return result;

    } else {
      // template values are specified by name

      final Map<String, TypeArgument> valuesByName = new HashMap<String, TypeArgument>(
          typeArguments.length);
      for (final TypeArgument value : typeArguments) {
        // sanity check.
        if (value.getTypeParameterName() == null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              new NodeErrorLocator(value),
              "Cannot mix ordinal and name-based template values.");
        }

        valuesByName.put(value.getTypeParameterName(), value);
      }

      final Map<String, TypeArgument> result = new HashMap<String, TypeArgument>();
      for (final FormalTypeParameter tmpl : formalTypeParameters) {
        final TypeArgument value = valuesByName.remove(tmpl.getName());
        if (value == null) {
          // missing template values
          throw new ADLException(
              ADLErrors.INVALID_REFERENCE_MISSING_TEMPLATE_VALUE, reference,
              tmpl.getName());
        }
        result.put(tmpl.getName(), value);
      }
      if (!valuesByName.isEmpty()) {
        // too many template values

        // get the first one
        final Map.Entry<String, TypeArgument> value = valuesByName.entrySet()
            .iterator().next();

        throw new ADLException(
            ADLErrors.INVALID_REFERENCE_NO_SUCH_TEMPLATE_VARIABLE,
            value.getValue(), value.getKey());
      }

      return result;
    }
  }

  protected void checkTypeCompatibility(final Definition templateType,
      final Definition typeValue, final Node locator) throws ADLException {

    // checks that every server interfaces of templateType is provided by
    // typeValue.
    final Map<String, MindInterface> valueServerInterfaces = new HashMap<String, MindInterface>();
    for (final Interface itf : castNodeError(typeValue,
        InterfaceContainer.class).getInterfaces()) {
      if (isServer(itf)) {
        valueServerInterfaces.put(itf.getName(),
            castNodeError(itf, MindInterface.class));
      }
    }

    for (final Interface itf : castNodeError(templateType,
        InterfaceContainer.class).getInterfaces()) {
      if (!isServer(itf)) continue;

      final MindInterface valueItf = valueServerInterfaces.get(itf.getName());
      if (valueItf == null) {
        throw new ADLException(
            ADLErrors.INVALID_TEMPLATE_VALUE_MISSING_SERVER_INTERFACE, locator,
            typeValue.getName(), itf.getName());
      }
      bindingCheckerItf.checkCompatibility(itf, valueItf, locator);
    }

    // checks that each client interface of typeValue is present in
    // templateType or is optional.
    final Map<String, MindInterface> templateClientInterfaces = new HashMap<String, MindInterface>();
    for (final Interface itf : castNodeError(templateType,
        InterfaceContainer.class).getInterfaces()) {
      if (isClient(itf)) {
        templateClientInterfaces.put(itf.getName(),
            castNodeError(itf, MindInterface.class));
      }
    }

    for (final Interface valueItf : castNodeError(typeValue,
        InterfaceContainer.class).getInterfaces()) {
      if (!isClient(valueItf)) continue;

      final MindInterface itf = templateClientInterfaces.remove(valueItf
          .getName());
      if (itf == null) {
        if (!TypeInterfaceUtil.isOptional(valueItf)) {
          throw new ADLException(
              ADLErrors.INVALID_TEMPLATE_VALUE_CLIENT_INTERFACE_MUST_BE_OPTIONAL,
              locator, valueItf.getName(), new NodeErrorLocator(valueItf));
        }
      } else {
        bindingCheckerItf.checkFromSubcomponentToCompositeBinding(valueItf,
            itf, null, locator);
      }
    }
    if (!templateClientInterfaces.isEmpty()) {
      final MindInterface missingItf = templateClientInterfaces.values()
          .iterator().next();
      throw new ADLException(
          ADLErrors.INVALID_TEMPLATE_VALUE_MISSING_CLIENT_INTERFACE, locator,
          typeValue.getName(), missingItf.getName());
    }
  }

  protected Map<String, FormalTypeParameter> getTypeParameters(
      final Definition d, final Map<Object, Object> context)
      throws ADLException {
    Map<Definition, Map<String, FormalTypeParameter>> typeParameters = contextualTypeParameters
        .get(context);
    if (typeParameters == null) {
      typeParameters = new IdentityHashMap<Definition, Map<String, FormalTypeParameter>>();
      contextualTypeParameters.set(context, typeParameters);
    }

    Map<String, FormalTypeParameter> result = typeParameters.get(d);

    if (result == null) {
      if (d instanceof FormalTypeParameterContainer) {
        final FormalTypeParameter[] formalTypeParameters = ((FormalTypeParameterContainer) d)
            .getFormalTypeParameters();
        if (formalTypeParameters.length > 0) {
          final Import[] imports = (d instanceof ImportContainer)
              ? ((ImportContainer) d).getImports()
              : null;

          result = new HashMap<String, FormalTypeParameter>(
              formalTypeParameters.length);
          for (final FormalTypeParameter typeParameter : formalTypeParameters) {

            // checks that formal type parameter do not hide an import.
            if (imports != null) {
              for (final Import imp : imports) {
                if (typeParameter.getName().equals(imp.getSimpleName())) {
                  // TODO use dedicated method to print warning
                  System.out.println("At " + typeParameter.astGetSource()
                      + ": WARNING template variable hides import at "
                      + imp.astGetSource());
                }
              }
            }

            if (result.put(typeParameter.getName(), typeParameter) != null) {
              throw new ADLException(
                  ADLErrors.DUPLICATED_TEMPALTE_VARIABLE_NAME, typeParameter,
                  typeParameter.getName());
            }
          }
        }
      }
      typeParameters.put(d, result);
    }

    return result;
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

    if (itfName.equals(RECURSIVE_RESOLVER_ITF_NAME)) {
      recursiveResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(BindingChecker.ITF_NAME)) {
      bindingCheckerItf = (BindingChecker) value;
    } else if (itfName.equals(TemplateInstantiator.ITF_NAME)) {
      templateInstantiatorItf = (TemplateInstantiator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 3];
    list[0] = RECURSIVE_RESOLVER_ITF_NAME;
    list[1] = BindingChecker.ITF_NAME;
    list[2] = TemplateInstantiator.ITF_NAME;
    System.arraycopy(superList, 0, list, 3, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(RECURSIVE_RESOLVER_ITF_NAME)) {
      return recursiveResolverItf;
    } else if (itfName.equals(BindingChecker.ITF_NAME)) {
      return bindingCheckerItf;
    } else if (itfName.equals(TemplateInstantiator.ITF_NAME)) {
      return templateInstantiatorItf;
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

    if (itfName.equals(RECURSIVE_RESOLVER_ITF_NAME)) {
      recursiveResolverItf = null;
    } else if (itfName.equals(BindingChecker.ITF_NAME)) {
      bindingCheckerItf = null;
    } else if (itfName.equals(TemplateInstantiator.ITF_NAME)) {
      templateInstantiatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
