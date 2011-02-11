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

package org.ow2.mind.adl.imports;

import static org.ow2.mind.NameHelper.getPackageName;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.isOnDemandImport;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.setUsedImport;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver.AbstractDelegatingDefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;

import com.google.inject.Inject;

/**
 * Delegating {@link DefinitionReferenceResolver} that uses {@link Import} nodes
 * of the <code>encapsulatingDefinition</code> to complete the name contained in
 * the definition reference to resolve.
 */
public class ImportDefinitionReferenceResolver
    extends
      AbstractDelegatingDefinitionReferenceResolver {

  @Inject
  protected ADLLocator adlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    reference.setName(resolveName(reference.getName(), encapsulatingDefinition,
        context));
    return clientResolverItf.resolve(reference, encapsulatingDefinition,
        context);
  }

  protected String resolveName(final String name,
      final Definition encapsilatingDefinition,
      final Map<Object, Object> context) {
    if (name.contains(".")) {
      return name;
    }

    final Import[] imports = (encapsilatingDefinition instanceof ImportContainer)
        ? ((ImportContainer) encapsilatingDefinition).getImports()
        : null;

    if (imports != null) {
      for (final Import imp : imports) {
        if (isOnDemandImport(imp)) {
          // on-demand import.

          final String fullyQualifiedName = imp.getPackageName() + '.' + name;
          if (adlLocatorItf.findBinaryADL(fullyQualifiedName, context) != null
              || adlLocatorItf.findSourceADL(fullyQualifiedName, context) != null) {
            return fullyQualifiedName;
          }
        } else {
          if (imp.getSimpleName().equals(name)) {
            // import simple name matches
            setUsedImport(imp);
            return imp.getPackageName() + '.' + name;
          }
        }
      }
    }

    if (encapsilatingDefinition != null) {
      final String packageName = getPackageName(encapsilatingDefinition
          .getName());
      // try in current package.
      if (packageName != null) {
        final String fullyQualifiedName = packageName + '.' + name;
        if (adlLocatorItf.findBinaryADL(fullyQualifiedName, context) != null
            || adlLocatorItf.findSourceADL(fullyQualifiedName, context) != null) {
          return fullyQualifiedName;
        }
      }
    }

    // no import match, return the name as it is (i.e. assume that it refers to
    // an ADL in the 'default package').
    return name;
  }
}
