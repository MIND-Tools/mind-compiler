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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.adl.ast.ASTHelper.isType;
import static org.ow2.mind.adl.ast.ASTHelper.setResolvedComponentDefinition;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;
import org.ow2.mind.error.ErrorManager;

/**
 * This delegating Loader checks that types of formal type arguments are
 * correct. It also checks if a formal type argument hides an import statement.
 */
public class GenericDefinitionLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager                errorManagerItf;

  /** The {@link NodeFactory} interface used by this component. */
  public NodeFactory                 nodeFactoryItf;

  /** The interface used to resolve type of formal type parameters. */
  public DefinitionReferenceResolver definitionReferenceResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {

    // delegates loading of definition to client loader.
    final Definition d = clientLoader.load(name, context);

    final Import[] imports = d instanceof ImportContainer
        ? ((ImportContainer) d).getImports()
        : null;

    // resolve types of formal type parameters (if any)
    if (d instanceof FormalTypeParameterContainer) {
      final FormalTypeParameter[] formalTypeParameters = ((FormalTypeParameterContainer) d)
          .getFormalTypeParameters();
      if (formalTypeParameters.length > 0) {
        final Map<String, Definition> typeParameterTypes = new HashMap<String, Definition>(
            formalTypeParameters.length);
        for (final FormalTypeParameter typeParameter : formalTypeParameters) {

          // checks that formal type parameter do not hide an import.
          if (imports != null) {
            for (final Import imp : imports) {
              if (typeParameter.getName().equals(imp.getSimpleName())) {
                errorManagerItf.logWarning(
                    ADLErrors.WARNING_TEMPLATE_VARIABLE_HIDE, typeParameter,
                    typeParameter.getName(), imp.astGetSource());
              }
            }
          }

          if (typeParameter.getDefinitionReference() == null) {
            // If the formal type parameter do not define a type to which it
            // must conform, pass it. This can happen for special definitions
            // like "Factory".
            continue;
          }

          final Definition typeParameterTypeDefinition = definitionReferenceResolverItf
              .resolve(typeParameter.getDefinitionReference(), d, context);

          if (!isType(typeParameterTypeDefinition))
            errorManagerItf
                .logError(ADLErrors.INVALID_REFERENCE_NOT_A_TYPE,
                    typeParameter, typeParameter.getDefinitionReference()
                        .getName());

          if (typeParameterTypes.put(typeParameter.getName(),
              typeParameterTypeDefinition) != null) {
            errorManagerItf.logError(
                ADLErrors.DUPLICATED_TEMPALTE_VARIABLE_NAME, typeParameter,
                typeParameter.getName());
          }
        }

        if (d instanceof ComponentContainer) {
          for (final Component subComp : ((ComponentContainer) d)
              .getComponents()) {

            if (subComp instanceof FormalTypeParameterReference) {
              final String ref = ((FormalTypeParameterReference) subComp)
                  .getTypeParameterReference();

              if (ref != null) {
                Definition typeParameterType = typeParameterTypes.get(ref);
                if (typeParameterType == null) {
                  errorManagerItf.logError(
                      ADLErrors.UNDEFINED_TEMPALTE_VARIABLE, subComp, ref);
                  typeParameterType = ASTHelper.newUnresolvedDefinitionNode(
                      nodeFactoryItf, null);
                }
                setResolvedComponentDefinition(subComp, typeParameterType);
              }
            }
          }
        }
      }
    }
    return d;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = (ErrorManager) value;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), ErrorManager.ITF_NAME,
        NodeFactory.ITF_NAME, DefinitionReferenceResolver.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      return errorManagerItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
