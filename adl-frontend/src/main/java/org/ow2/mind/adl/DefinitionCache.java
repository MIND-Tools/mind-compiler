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

import org.objectweb.fractal.adl.Definition;

/**
 * Interface provides by component that manage a cache of {@link Definition}.
 */
public interface DefinitionCache {

  /** Default name of this interface. */
  String ITF_NAME = "definition-cache";

  /**
   * Adds the given definition in the cache.
   * 
   * @param d the definition to add in the cache.
   * @param context additional parameters.
   */
  void addInCache(Definition d, Map<Object, Object> context);

  /**
   * retrieves the definition with the given name from the cache.
   * 
   * @param name a definition name.
   * @param context additional parameters.
   * @return the definition with the given name from the cache or
   *         <code>null</code> if the cache does not contains a definition for
   *         the given name.
   */
  Definition getInCache(String name, Map<Object, Object> context);
}
