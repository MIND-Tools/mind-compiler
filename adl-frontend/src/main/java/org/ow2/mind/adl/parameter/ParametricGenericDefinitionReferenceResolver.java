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

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver.AbstractDelegatingDefinitionReferenceResolver;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;

/**
 * This delegating {@link DefinitionReferenceResolver} extracts {@link Argument}
 * nodes of the {@link TypeArgument type arguments} contained by the definition
 * reference to resolve, and adds them to the definition reference to resolve. <br>
 * i.e. transforms
 * 
 * <pre>
 * D1&lt;P1(10, 11), P2(&quot;foo&quot;)&gt;
 * </pre>
 * 
 * to
 * 
 * <pre>
 * D1&lt;P1, P2&gt;(10, 11, &quot;foo&quot;)
 * </pre>
 * 
 * @see ParametricTemplateInstantiator
 */
public class ParametricGenericDefinitionReferenceResolver
    extends
      AbstractDelegatingDefinitionReferenceResolver {

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    final TypeArgument[] typeArguments = (reference instanceof TypeArgumentContainer)
        ? ((TypeArgumentContainer) reference).getTypeArguments()
        : null;

    final Definition d = clientResolverItf.resolve(reference,
        encapsulatingDefinition, context);

    if (!ASTHelper.isUnresolvedDefinitionNode(d) && typeArguments != null
        && typeArguments.length > 0) {
      ArgumentContainer topArgumentContainer = null;

      for (final TypeArgument typeArgument : typeArguments) {
        if (typeArgument.getDefinitionReference() instanceof ArgumentContainer) {
          final String typeParameterName = typeArgument.getTypeParameterName();
          assert typeParameterName != null;

          final ArgumentContainer argumentContainer = (ArgumentContainer) typeArgument
              .getDefinitionReference();
          for (final Argument argument : argumentContainer.getArguments()) {
            assert argument.getName() != null;
            argumentContainer.removeArgument(argument);

            argument.setName(typeParameterName + "$" + argument.getName());

            if (topArgumentContainer == null) {
              // cast reference node to ArgumentContainer. If the reference node
              // is not an ArgumentContainer, this is an error since it is
              // impossible to replace it by a node that actually implements
              // ArgumentContainer.
              topArgumentContainer = castNodeError(reference,
                  ArgumentContainer.class);
            }
            topArgumentContainer.addArgument(argument);
          }
        }
      }
    }
    return d;
  }
}
