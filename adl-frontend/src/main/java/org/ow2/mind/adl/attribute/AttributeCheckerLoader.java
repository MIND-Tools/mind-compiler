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

package org.ow2.mind.adl.attribute;

import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.getInferredParameterType;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.setInferredParameterType;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.setUsedFormalParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IncludeResolver;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.IDLASTHelper.IncludeDelimiter;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.value.ValueKindDecorator;
import org.ow2.mind.value.ast.CompoundValue;
import org.ow2.mind.value.ast.CompoundValueField;
import org.ow2.mind.value.ast.NullLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

import com.google.inject.Inject;

public class AttributeCheckerLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager       errorManagerItf;

  @Inject
  protected NodeFactory        nodeFactoryItf;

  @Inject
  protected IncludeResolver    includeResolverItf;

  @Inject
  protected ValueKindDecorator valueKindDecoratorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    if (d instanceof AttributeContainer)
      checkAttributes((AttributeContainer) d, context);
    return d;
  }

  protected void checkAttributes(final AttributeContainer container,
      final Map<Object, Object> context) throws ADLException {

    final Map<String, FormalParameter> formalParameters = new HashMap<String, FormalParameter>();
    if (container instanceof FormalParameterContainer) {
      for (final FormalParameter parameter : ((FormalParameterContainer) container)
          .getFormalParameters()) {
        formalParameters.put(parameter.getName(), parameter);
      }
    }

    final List<Attribute> uninitializedAttributes = new ArrayList<Attribute>();

    for (final Attribute attr : container.getAttributes()) {

      final String idtPath = attr.getIdt();
      if (idtPath != null) {
        // check idt path
        // create an include node to use the IncludeResolver
        final Include includeNode = IDLASTHelper.newIncludeNode(nodeFactoryItf,
            idtPath, IncludeDelimiter.QUOTE);
        // copy source info for error reporting
        includeNode.astSetSource(attr.astGetSource());
        final IDL idtFile = includeResolverItf.resolve(includeNode, null,
            ((Definition) container).getName(), context);
        attr.setIdt(idtFile.getName());
      }
      final ParameterType type = ParameterType.fromCType(idtPath,
          attr.getType());

      final Value value = attr.getValue();

      if (value != null) {
        valueKindDecoratorItf.setValueKind(value, context);

        if (value instanceof NumberLiteral) {
          if (!type.isCompatible(value))
            errorManagerItf.logError(
                ADLErrors.INVALID_ATTRIBUTE_VALUE_INCOMPATIBLE_TYPE, value);

          if (type.isIntegerType() && type.getCType().startsWith("u")
              && ((NumberLiteral) value).getValue().startsWith("-")) {
            errorManagerItf.logWarning(
                ADLErrors.WARNING_ATTRIBUTE_UNSIGNED_ASSIGNED_TO_NEGATIVE,
                value);
          }
        } else if (value instanceof StringLiteral
            || value instanceof NullLiteral || value instanceof CompoundValue) {
          if (!type.isCompatible(value))
            errorManagerItf.logError(
                ADLErrors.INVALID_ATTRIBUTE_VALUE_INCOMPATIBLE_TYPE, value);
          if (value instanceof CompoundValue) {
            checkCompoundValue((CompoundValue) value, formalParameters);
          }
        } else {
          assert value instanceof Reference;
          final String refParamName = ((Reference) value).getRef();

          final FormalParameter refParam = formalParameters.get(refParamName);
          if (refParam == null) {
            errorManagerItf.logError(ADLErrors.UNDEFINED_PARAMETER, value,
                refParamName);
          } else {
            setUsedFormalParameter(refParam);
            final ParameterType referencedType = getInferredParameterType(refParam);
            if (referencedType == null) {
              setInferredParameterType(refParam, type);
            } else if (type != null && !type.isCompatible(referencedType)) {
              errorManagerItf.logError(ADLErrors.INCOMPATIBLE_ARGUMENT_TYPE,
                  value, refParamName);
            }
          }
        }
      } else {
        // value is null, move attribute at end of list
        uninitializedAttributes.add(attr);
        container.removeAttribute(attr);
      }
    }
    // re-add uninitialized attribute at end of container.
    for (final Attribute uninitializedAttr : uninitializedAttributes) {
      container.addAttribute(uninitializedAttr);
    }
  }

  private void checkCompoundValue(final CompoundValue value,
      final Map<String, FormalParameter> formalParameters) throws ADLException {
    for (final CompoundValueField field : value.getCompoundValueFields()) {
      final Value subValue = field.getValue();
      if (subValue instanceof Reference) {
        final String refParamName = ((Reference) subValue).getRef();

        final FormalParameter refParam = formalParameters.get(refParamName);
        if (refParam == null) {
          errorManagerItf.logError(ADLErrors.UNDEFINED_PARAMETER, value,
              refParamName);
        } else {
          setUsedFormalParameter(refParam);
        }
      } else if (subValue instanceof CompoundValue) {
        checkCompoundValue((CompoundValue) subValue, formalParameters);
      }
    }
  }
}
