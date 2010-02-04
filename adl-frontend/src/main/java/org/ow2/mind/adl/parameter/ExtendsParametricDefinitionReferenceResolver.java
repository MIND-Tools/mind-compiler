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

import static org.objectweb.fractal.adl.NodeUtil.cloneGraph;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeUtil;
import org.ow2.mind.adl.AbstractDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ExtendsLoader;
import org.ow2.mind.adl.SubComponentResolverLoader;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.Value;

/**
 * This delegating {@link DefinitionReferenceResolver} replaces, in the resolved
 * definition, every parameter occurrences by its corresponding value. This
 * <code>DefinitionReferenceResolver</code> should be used only by
 * {@link ExtendsLoader} and should not be used by
 * {@link SubComponentResolverLoader}.
 */
public class ExtendsParametricDefinitionReferenceResolver
    extends
      AbstractDefinitionReferenceResolver {

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    Definition d = clientResolverItf.resolve(reference,
        encapsulatingDefinition, context);

    if (d instanceof FormalParameterContainer) {
      final FormalParameter[] formalParameters = ((FormalParameterContainer) d)
          .getFormalParameters();
      if (formalParameters.length > 0) {
        assert reference instanceof ArgumentContainer;

        d = cloneGraph(d);

        final Map<String, Value> values = new HashMap<String, Value>(
            formalParameters.length);
        for (final Argument argument : ((ArgumentContainer) reference)
            .getArguments()) {
          assert argument.getName() != null;

          values.put(argument.getName(), argument.getValue());
        }

        // replace argument occurrences in sub components.
        if (d instanceof ComponentContainer) {
          for (final Component subComp : ((ComponentContainer) d)
              .getComponents()) {
            final DefinitionReference subCompDefRef = subComp
                .getDefinitionReference();
            if (subCompDefRef instanceof ArgumentContainer) {
              for (final Argument argument : ((ArgumentContainer) subCompDefRef)
                  .getArguments()) {
                final Value argValue = argument.getValue();
                if (argValue instanceof Reference) {
                  final String ref = ((Reference) argValue).getRef();
                  final Value v = values.get(ref);
                  if (v != null) {
                    // normally v is never null...
                    argument.setValue(NodeUtil.cloneTree(v));
                  }
                }
              }
            }
          }
        }

        // replace argument occurrences in attributes.
        if (d instanceof AttributeContainer) {
          for (final Attribute attribute : ((AttributeContainer) d)
              .getAttributes()) {
            final Value attrValue = attribute.getValue();
            if (attrValue instanceof Reference) {
              final String ref = ((Reference) attrValue).getRef();
              final Value v = values.get(ref);
              if (v != null) {
                // normally v is never null...
                attribute.setValue(NodeUtil.cloneTree(v));
              }

            }
          }
        }
      }
    }

    return d;
  }

}
