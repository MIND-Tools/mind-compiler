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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ast.DefinitionReference;

/**
 * Interface used to retrieve the {@link Definition} corresponding to a
 * {@link DefinitionReference}.
 */
public interface DefinitionReferenceResolver {

  /** Default name of this interface. */
  String ITF_NAME = "definition-reference-resolver";

  /**
   * Checks the given {@link DefinitionReference} and return the designated
   * {@link Definition}.
   * 
   * @param reference the {@link DefinitionReference} to resolve.
   * @param encapsulatingDefinition the {@link Definition} into which the given
   *          <code>reference</code> is declared. May be <code>null</code> if
   *          the given <code>reference</code> is "context-free".
   * @param context additional parameters.
   * @return the {@link Definition} designated by the given
   *         <code>reference</code>
   * @throws ADLException if the given <code>reference</code> is invalid.
   */
  Definition resolve(DefinitionReference reference,
      Definition encapsulatingDefinition, Map<Object, Object> context)
      throws ADLException;
}
