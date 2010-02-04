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

import static org.ow2.mind.adl.ast.ASTHelper.isType;
import static org.ow2.mind.adl.ast.ASTHelper.setResolvedComponentDefinition;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;

/**
 * This delegating Loader checks that types of formal type arguments are
 * correct. It also checks if a formal type argument hides an import statement.
 */
public class GenericDefinitionLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

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
                // TODO use dedicated method to print warning
                System.out.println("At " + typeParameter.astGetSource()
                    + ": WARNING template variable hides import at "
                    + imp.astGetSource());
              }
            }
          }

          if (typeParameter.getDefinitionReference() == null) {
            // If the formal type parameter do not define a type to which it
            // must conform, pass it. This can happen for special definitions
            // like "Factory".
            continue;
          }

          final Definition typeParameterTypeDefinition;
          try {
            typeParameterTypeDefinition = definitionReferenceResolverItf
                .resolve(typeParameter.getDefinitionReference(), d, context);
          } catch (final ADLException e) {
            ChainedErrorLocator.chainLocator(e, typeParameter);
            throw e;
          }

          if (!isType(typeParameterTypeDefinition))
            throw new ADLException(ADLErrors.INVALID_REFERENCE_NOT_A_TYPE,
                typeParameter, typeParameter.getDefinitionReference().getName());

          if (typeParameterTypes.put(typeParameter.getName(),
              typeParameterTypeDefinition) != null) {
            throw new ADLException(ADLErrors.DUPLICATED_TEMPALTE_VARIABLE_NAME,
                typeParameter, typeParameter.getName());
          }
        }

        if (d instanceof ComponentContainer) {
          for (final Component subComp : ((ComponentContainer) d)
              .getComponents()) {

            if (subComp instanceof FormalTypeParameterReference) {
              final String ref = ((FormalTypeParameterReference) subComp)
                  .getTypeParameterReference();

              if (ref != null) {
                final Definition typeParameterType = typeParameterTypes
                    .get(ref);
                if (typeParameterType == null) {
                  throw new ADLException(ADLErrors.UNDEFINED_TEMPALTE_VARIABLE,
                      subComp, ref);
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

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 1];
    list[0] = DefinitionReferenceResolver.ITF_NAME;
    System.arraycopy(superList, 0, list, 1, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
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

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
