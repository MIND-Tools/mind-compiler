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

import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractor;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractor.AbstractDelegatingAnonymousDefinitionExtractor;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.GenericASTHelper;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

import com.google.inject.Inject;

/**
 * Delegating {@link AnonymousDefinitionExtractor} component that copies
 * {@link FormalTypeParameter} from the <code>encapsulatingDefinition</code> to
 * the extracted anonymous definition. Moreover, this component adds the
 * appropriate {@link TypeArgument type arguments} to the
 * {@link DefinitionReference} that references the anonymous definition.
 */
public class GenericAnonymousDefinitionExtractor
    extends
      AbstractDelegatingAnonymousDefinitionExtractor {

  @Inject
  protected NodeFactory nodeFactoryItf;

  @Inject
  protected NodeMerger  nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the AnonymousDefinitionResolver interface
  // ---------------------------------------------------------------------------

  public Definition extractAnonymousDefinition(final Component component,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) {
    Definition anonymousDefinition = clientExtractorItf
        .extractAnonymousDefinition(component, encapsulatingDefinition, context);
    if (anonymousDefinition instanceof ComponentContainer
        && encapsulatingDefinition instanceof FormalTypeParameterContainer) {
      final FormalTypeParameter[] typeParameters = ((FormalTypeParameterContainer) encapsulatingDefinition)
          .getFormalTypeParameters();
      if (typeParameters.length > 0) {

        // Adds formal type parameters of encapsulating definition to anonymous
        // definition.

        final FormalTypeParameterContainer subCompTypeParams = GenericASTHelper
            .turnsToFormalTypeParameterContainer(anonymousDefinition,
                nodeFactoryItf, nodeMergerItf);
        anonymousDefinition = (Definition) subCompTypeParams;

        for (final FormalTypeParameter typeParameter : typeParameters) {
          subCompTypeParams.addFormalTypeParameter(typeParameter);
        }

        // Add corresponding type arguments to definition reference.
        final TypeArgumentContainer defRefArgs = GenericASTHelper
            .turnsToTypeArgumentContainer(component.getDefinitionReference(),
                nodeFactoryItf, nodeMergerItf);
        component.setDefinitionReference((DefinitionReference) defRefArgs);

        for (final FormalTypeParameter typeParameter : typeParameters) {
          final TypeArgument arg = GenericASTHelper
              .newTypeArgument(nodeFactoryItf);
          arg.setTypeParameterName(typeParameter.getName());
          arg.setTypeParameterReference(typeParameter.getName());
          defRefArgs.addTypeArgument(arg);
        }
      }
    }

    return anonymousDefinition;
  }
}
