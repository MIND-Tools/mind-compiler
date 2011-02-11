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

package org.ow2.mind.adl.anonymous;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ContextLocal;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.anonymous.ast.AnonymousDefinitionContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

/**
 * Delegating loader that {@link AnonymousDefinitionExtractor resolves}
 * anonymous definitions contained by the sub-components of the loaded
 * definition. This loader keep extracted anonymous definitions in a cache. So
 * when these definitions are loaded (when definition references of
 * sub-components are resolved), this loader is able to intercept the loading
 * operation and return them.
 */
public class AnonymousDefinitionLoader extends AbstractDelegatingLoader {

  protected ContextLocal<Map<String, Definition>> contextualAnonymousDefinitions = new ContextLocal<Map<String, Definition>>();

  @Inject
  protected ErrorManager                          errorManagerItf;

  @Inject
  protected AnonymousDefinitionExtractor          anonymousDefinitionExtractorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    // looks if it is a known anonymous definition.
    final Map<String, Definition> anonymousDefinitions = contextualAnonymousDefinitions
        .get(context);
    if (anonymousDefinitions != null) {
      final Definition anonymousDefinition = anonymousDefinitions.remove(name);
      if (anonymousDefinition != null) {
        // An anonymous definition with the corresponding name has been found in
        // local cache. Return it.
        return anonymousDefinition;
      }
    }

    // The definition to load is not an anonymous definition.
    // delegates loading of definition to client loader.
    final Definition d = clientLoader.load(name, context);

    // If loaded definition contains component nodes, it may contains anonymous
    // definitions
    if (d instanceof ComponentContainer) {
      replaceAnonymousDefinitions((ComponentContainer) d, d, context);
    }

    return d;
  }

  protected void replaceAnonymousDefinitions(
      final ComponentContainer container, final Definition topLevel,
      final Map<Object, Object> context) throws ADLException {
    // for each sub-component
    for (final Component subComp : container.getComponents()) {
      if ((subComp instanceof AnonymousDefinitionContainer)
          && ((AnonymousDefinitionContainer) subComp).getAnonymousDefinition() != null) {
        // The sub-component is defined as an instance of an anonymous
        // definition.

        if (subComp.getDefinitionReference() != null
            || (subComp instanceof FormalTypeParameterReference && ((FormalTypeParameterReference) subComp)
                .getTypeParameterReference() != null)) {
          // this cannot happen if input definition has been read from
          // ADLParser. but it may happen if it has been read from an XML
          // parser.
          errorManagerItf.logError(ADLErrors.INVALID_SUB_COMPONENT, subComp);
        }

        // resolve it.
        final Definition anonymousDefinition = anonymousDefinitionExtractorItf
            .extractAnonymousDefinition(subComp, topLevel, context);

        // put resolved definition in cache
        addAnonymousDefinition(anonymousDefinition, context);

        // The anonymous definition may contain itself sub-component with
        // anonymous definitions, replace recursively sub anonymous definitions
        if (anonymousDefinition instanceof ComponentContainer) {
          replaceAnonymousDefinitions((ComponentContainer) anonymousDefinition,
              topLevel, context);
        }
      }
    }
  }

  protected void addAnonymousDefinition(final Definition anonymousDefinition,
      final Map<Object, Object> context) {
    Map<String, Definition> anonymousDefinitions = contextualAnonymousDefinitions
        .get(context);

    if (anonymousDefinitions == null) {
      anonymousDefinitions = new HashMap<String, Definition>();
      contextualAnonymousDefinitions.set(context, anonymousDefinitions);
    }

    anonymousDefinitions
        .put(anonymousDefinition.getName(), anonymousDefinition);
  }
}
