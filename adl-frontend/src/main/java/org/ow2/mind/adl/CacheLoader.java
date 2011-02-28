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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ContextLocal;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Simple delegating loader that manage a cache of already loaded definitions.
 * This component also provides the {@link DefinitionCache} interface that can
 * be used to add/get definitions to/from the cache. The cache is different for
 * each given <code>context</code> (see {@link ContextLocal}).
 */
@Singleton
public class CacheLoader extends AbstractDelegatingLoader
    implements
      DefinitionCache {

  protected final ContextLocal<Map<String, Definition>> contextualCache    = new ContextLocal<Map<String, Definition>>();

  protected ThreadLocal<Set<String>>                    loadingDefinitions = new ThreadLocal<Set<String>>();

  @Inject
  protected ErrorManager                                errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Map<String, Definition> cache = getCache(context);
    Definition d = cache.get(name);

    if (d == null) {
      Set<String> loadingDefinitions = this.loadingDefinitions.get();
      if (loadingDefinitions == null) {
        loadingDefinitions = new LinkedHashSet<String>();
        this.loadingDefinitions.set(loadingDefinitions);
      }

      if (!loadingDefinitions.add(name)) {
        errorManagerItf.logFatal(ComponentErrors.DEFINITION_CYCLE,
            loadingDefinitions.toString());
      }
      try {
        d = clientLoader.load(name, context);
      } finally {
        loadingDefinitions.remove(name);
      }
      cache.put(name, d);
    }

    return d;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionCache interface
  // ---------------------------------------------------------------------------

  public void addInCache(final Definition d, final Map<Object, Object> context) {
    getCache(context).put(d.getName(), d);
  }

  public Definition getInCache(final String name,
      final Map<Object, Object> context) {
    return getCache(context).get(name);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Map<String, Definition> getCache(final Map<Object, Object> context) {
    Map<String, Definition> cache = contextualCache.get(context);
    if (cache == null) {
      cache = new HashMap<String, Definition>();
      contextualCache.set(context, cache);
    }
    return cache;
  }
}
