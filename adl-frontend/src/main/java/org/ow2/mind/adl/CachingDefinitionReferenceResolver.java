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

import static org.ow2.mind.adl.ast.ASTHelper.getResolvedDefinition;
import static org.ow2.mind.adl.ast.ASTHelper.setResolvedDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.adl.DefinitionReferenceResolver.AbstractDelegatingDefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;

import com.google.inject.Inject;

/**
 * Simple delegating {@link DefinitionReferenceResolver} that attaches resolved
 * definitions as decoration on the given {@link DefinitionReference}. So if
 * latter the same reference is resolved another time, the designated definition
 * is simply retrieved by accessing to this decoration. <br>
 * The decoration attached to the <code>DefinitionReference</code> is
 * serializable, but only the name of the attached definition is actually
 * serialized. This means that the decoration may be present on a given
 * <code>DefinitionReference</code>, but without the designated definition. In
 * this case, this component will delegate loading of the designated definition
 * to its {@link #loaderItf} client interface.
 */
public class CachingDefinitionReferenceResolver
    extends
      AbstractDelegatingDefinitionReferenceResolver {

  @Inject
  protected Loader loaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {

    Definition d = getResolvedDefinition(reference, loaderItf, context);
    if (d == null) {
      d = clientResolverItf
          .resolve(reference, encapsulatingDefinition, context);
      setResolvedDefinition(reference, d);
    }
    return d;
  }
}
