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

package org.ow2.mind.adl;

import static org.ow2.mind.adl.ast.ASTHelper.isAbstract;
import static org.ow2.mind.adl.ast.ASTHelper.isType;
import static org.ow2.mind.adl.ast.ASTHelper.isUnresolvedDefinitionNode;
import static org.ow2.mind.adl.ast.ASTHelper.setResolvedComponentDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Delegating loader component that resolves {@link DefinitionReference} of sub
 * components contained in the loaded definition
 */
public class SubComponentResolverLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager                errorManagerItf;

  /**
   * The name of the {@link DefinitionReferenceResolver} to be injected in this
   * class.
   */
  public static final String            SUB_COMPONENT_DEFINITION_RESOLVER = "SubComponentDefinitionResolver";

  @Inject
  @Named(SUB_COMPONENT_DEFINITION_RESOLVER)
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {

    // delegates loading of definition to client loader.
    final Definition d = clientLoader.load(name, context);

    // resolve sub components (if any)
    if (d instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) d).getComponents()) {

        final DefinitionReference subCompDefRef = subComp
            .getDefinitionReference();
        if (subCompDefRef != null) {
          final Definition subCompDef = definitionReferenceResolverItf.resolve(
              subCompDefRef, d, context);

          if (!isUnresolvedDefinitionNode(subCompDef)
              && (isType(subCompDef) || isAbstract(subCompDef)))
            errorManagerItf.logError(
                ADLErrors.INVALID_REFERENCE_FOR_SUB_COMPONENT, subCompDefRef,
                subCompDefRef.getName());

          setResolvedComponentDefinition(subComp, subCompDef);
        }
      }
    }

    return d;
  }
}
