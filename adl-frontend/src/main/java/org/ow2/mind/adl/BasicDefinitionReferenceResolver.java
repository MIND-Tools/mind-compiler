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

import java.util.Map;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

/**
 * Basic implementation of the {@link DefinitionReferenceResolver} interface.
 * This component simply call its {@link #loaderItf} client interface to load
 * the {@link Definition} whose name is the
 * {@link DefinitionReference#getName() name} contained by the reference.
 */
public class BasicDefinitionReferenceResolver
    implements
      DefinitionReferenceResolver {

  @Inject
  protected ErrorManager errorManagerItf;

  @Inject
  protected NodeFactory  nodeFactoryItf;

  @Inject
  protected Loader       loaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    // load referenced ADL
    final Definition d;
    try {
      d = loaderItf.load(reference.getName(), context);
    } catch (final ADLException e) {
      // Log an error only if the exception is ADL_NOT_FOUND
      if (e.getError().getTemplate() == ADLErrors.ADL_NOT_FOUND) {
        errorManagerItf.logError(ADLErrors.ADL_NOT_FOUND, reference,
            reference.getName());
        return ASTHelper.newUnresolvedDefinitionNode(nodeFactoryItf,
            reference.getName());
      } else if (e.getError().getTemplate() == ComponentErrors.DEFINITION_CYCLE) {
        errorManagerItf.logError(ComponentErrors.DEFINITION_CYCLE, reference,
            reference.getName());
        return ASTHelper.newUnresolvedDefinitionNode(nodeFactoryItf,
            reference.getName());
      } else {
        throw e;
      }
    }

    return d;
  }
}
