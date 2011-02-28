/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.plugin.util;

import java.util.Map;

import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;

public interface BooleanEvaluator {

  /**
   * Evaluate the given configuration element.
   * 
   * @param configurationElement a configuration element to evaluate.
   * @param pluginManager the plugin manager
   * @param context additional parameter. May be <code>null</code> if the
   *          evaluation is performed in a compilation step where the context is
   *          not available.
   * @return the evaluation of the given configuration element.
   */
  boolean evaluate(ConfigurationElement configurationElement,
      PluginManager pluginManager, Map<Object, Object> context);
}
