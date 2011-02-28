/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ContextLocal;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.target.TargetDescriptorLoader.AbstractDelegatingTargetDescriptorLoader;
import org.ow2.mind.target.ast.Target;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CacheLoader extends AbstractDelegatingTargetDescriptorLoader {

  protected final ContextLocal<Map<String, Target>> contextualCache = new ContextLocal<Map<String, Target>>();

  protected ThreadLocal<Set<String>>                loadingTargets  = new ThreadLocal<Set<String>>();

  @Inject
  protected ErrorManager                            errorManager;

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Map<String, Target> cache = getCache(context);
    Target target = cache.get(name);

    if (target == null) {
      Set<String> loadingTargets = this.loadingTargets.get();
      if (loadingTargets == null) {
        loadingTargets = new LinkedHashSet<String>();
        this.loadingTargets.set(loadingTargets);
      }

      if (!loadingTargets.add(name)) {
        errorManager.logFatal(TargetDescErrors.CYCLE_FATAL,
            loadingTargets.toString());
      }
      try {
        target = clientLoader.load(name, context);
      } finally {
        loadingTargets.remove(name);
      }
      cache.put(name, target);
    }
    return target;
  }

  protected Map<String, Target> getCache(final Map<Object, Object> context) {
    Map<String, Target> cache = contextualCache.get(context);
    if (cache == null) {
      cache = new HashMap<String, Target>();
      contextualCache.set(context, cache);
    }
    return cache;
  }
}
