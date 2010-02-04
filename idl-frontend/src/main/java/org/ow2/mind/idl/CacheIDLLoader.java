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

package org.ow2.mind.idl;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ContextLocal;
import org.ow2.mind.idl.ast.IDL;

/**
 * Simple delegating loader that manage a cache of already loaded definitions.
 * The cache is different for each given <code>context</code> (see
 * {@link ContextLocal}).
 */
public class CacheIDLLoader extends AbstractIDLLoader {

  protected final ContextLocal<Map<String, IDL>> contextualCache = new ContextLocal<Map<String, IDL>>();

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Map<String, IDL> cache = getCache(context);
    IDL idl = cache.get(name);

    if (idl == null) {
      idl = clientIDLLoaderItf.load(name, context);
      cache.put(name, idl);
    }

    return idl;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Map<String, IDL> getCache(final Map<Object, Object> context) {
    Map<String, IDL> cache = contextualCache.get(context);
    if (cache == null) {
      cache = new HashMap<String, IDL>();
      contextualCache.set(context, cache);
    }
    return cache;
  }
}
