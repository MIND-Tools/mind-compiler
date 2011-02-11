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

import static org.ow2.mind.adl.imports.ast.ImportASTHelper.turnsToImportContainer;

import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractor.AbstractDelegatingAnonymousDefinitionExtractor;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;

import com.google.inject.Inject;

/**
 * Delegating {@link AnonymousDefinitionExtractor} component that adds
 * {@link Import} nodes contained by <code>encapsulatingDefinition</code> to the
 * anonymous definition.
 */
public class ImportAnonymousDefinitionExtractor
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

    // add imports from encapsulatingDefinition.
    if (encapsulatingDefinition instanceof ImportContainer) {
      final Import[] imports = ((ImportContainer) encapsulatingDefinition)
          .getImports();
      if (imports.length > 0) {
        final ImportContainer importContainer = turnsToImportContainer(
            anonymousDefinition, nodeFactoryItf, nodeMergerItf);
        anonymousDefinition = (Definition) importContainer;
        for (final Import imp : imports)
          importContainer.addImport(imp);
      }
    }
    return anonymousDefinition;
  }
}
