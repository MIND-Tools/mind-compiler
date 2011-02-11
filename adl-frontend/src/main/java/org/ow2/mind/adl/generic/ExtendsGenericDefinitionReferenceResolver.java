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

import static org.objectweb.fractal.adl.NodeUtil.cloneTree;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setTemplateName;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeUtil;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver.AbstractDelegatingDefinitionReferenceResolver;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

/**
 * This delegating {@link DefinitionReferenceResolver} replaces, in the resolved
 * definition, every type parameter occurrences by its corresponding value. This
 * <code>DefinitionReferenceResolver</code> should be used only by
 * {@link ExtendsLoader} and should not be used by
 * {@link SubComponentResolverLoader}.
 */
public class ExtendsGenericDefinitionReferenceResolver
    extends
      AbstractDelegatingDefinitionReferenceResolver {

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    Definition d = clientResolverItf.resolve(reference,
        encapsulatingDefinition, context);

    if (d instanceof FormalTypeParameterContainer) {
      final FormalTypeParameter[] formalTypeParameters = ((FormalTypeParameterContainer) d)
          .getFormalTypeParameters();
      if (formalTypeParameters.length > 0) {
        assert reference instanceof TypeArgumentContainer;

        d = NodeUtil.cloneGraph(d);
        setTemplateName(d, null);

        final Map<String, TypeArgument> typeParameterMapping = new HashMap<String, TypeArgument>();
        for (final TypeArgument typeArgument : ((TypeArgumentContainer) reference)
            .getTypeArguments()) {
          assert typeArgument.getTypeParameterName() != null;
          typeParameterMapping.put(typeArgument.getTypeParameterName(),
              typeArgument);
        }

        // replace type argument occurrences that references a formal type
        // parameter of the encapsulating definition.

        if (d instanceof ComponentContainer) {
          for (final Component subComp : ((ComponentContainer) d)
              .getComponents()) {
            if ((subComp instanceof FormalTypeParameterReference)
                && ((FormalTypeParameterReference) subComp)
                    .getTypeParameterReference() != null) {
              final FormalTypeParameterReference typeParamRef = (FormalTypeParameterReference) subComp;
              final TypeArgument typeArgument = typeParameterMapping
                  .get(typeParamRef.getTypeParameterReference());
              assert typeArgument != null;
              if (typeArgument.getTypeParameterReference() != null) {
                typeParamRef.setTypeParameterReference(typeArgument
                    .getTypeParameterReference());
              } else {
                typeParamRef.setTypeParameterReference(null);
                final DefinitionReference defRef = typeArgument
                    .getDefinitionReference();
                if (defRef != null) {
                  subComp.setDefinitionReference(cloneTree(defRef));
                }
              }

            } else {
              replaceTypeparameterReferences(subComp.getDefinitionReference(),
                  typeParameterMapping);
            }
          }
        }
      }
    }

    return d;
  }

  protected void replaceTypeparameterReferences(
      final DefinitionReference defRef,
      final Map<String, TypeArgument> typeParameterMapping) {
    if (defRef instanceof TypeArgumentContainer) {
      for (final TypeArgument typeArgument : ((TypeArgumentContainer) defRef)
          .getTypeArguments()) {
        if (typeArgument.getTypeParameterReference() != null) {
          final TypeArgument referencedTypeArgument = typeParameterMapping
              .get(typeArgument.getTypeParameterReference());
          assert referencedTypeArgument != null;
          if (referencedTypeArgument.getTypeParameterReference() != null) {
            typeArgument.setTypeParameterReference(referencedTypeArgument
                .getTypeParameterReference());
          } else {
            typeArgument.setTypeParameterReference(null);
            typeArgument.setDefinitionReference(typeArgument
                .getDefinitionReference());
          }
        } else if (typeArgument.getDefinitionReference() != null) {
          replaceTypeparameterReferences(typeArgument.getDefinitionReference(),
              typeParameterMapping);
        }
      }
    }
  }

}
