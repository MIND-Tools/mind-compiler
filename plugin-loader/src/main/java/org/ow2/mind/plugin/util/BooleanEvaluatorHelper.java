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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;

public final class BooleanEvaluatorHelper {

  public static final String BOOLEAN_EVALUATOR_ID = "org.ow2.mind.plugin.BooleanEvaluator";

  private BooleanEvaluatorHelper() {
  }

  private static Map<String, BooleanEvaluator> evaluators = null;

  /**
   * Evaluates the given {@link ConfigurationElement} using the
   * {@link BooleanEvaluator} registered in the {@link PluginManager}. If the
   * given {@link ConfigurationElement} is <code>null</code>, this method
   * returns <code>true</code>.
   * 
   * @param configurationElement the element to evaluate. May be
   *          <code>null</code>.
   * @param pluginManager the plugin manger used to retrieve
   *          {@link BooleanEvaluator}
   * @return the result of {@link BooleanEvaluator#evaluate}
   * @param context additional parameter. May be <code>null</code> if the
   *          evaluation is performed in a compilation step where the context is
   *          not available.
   * @throws CompilerError if no evaluator is registered for the given
   *           configuration element.
   */
  public static boolean evaluate(
      final ConfigurationElement configurationElement,
      final PluginManager pluginManager, final Map<Object, Object> context) {
    if (configurationElement == null) return true;
    final BooleanEvaluator evaluator = getEvaluators(pluginManager).get(
        configurationElement.getName());
    if (evaluator == null) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR,
          "Can't find boolean evaluator for element '"
              + configurationElement.getName() + "'.");
    }
    return evaluator.evaluate(configurationElement, pluginManager, context);
  }

  private static Map<String, BooleanEvaluator> getEvaluators(
      final PluginManager pluginManager) {
    if (evaluators == null) {
      evaluators = new HashMap<String, BooleanEvaluator>();
      for (final ConfigurationElement element : pluginManager
          .getConfigurationElements(BOOLEAN_EVALUATOR_ID)) {
        final String name = element.getAttribute("name");
        final BooleanEvaluator evaluator = element.createInstance("class",
            BooleanEvaluator.class);
        final BooleanEvaluator prevValue = evaluators.put(name, evaluator);
        if (prevValue != null)
          throw new CompilerError(GenericErrors.GENERIC_ERROR,
              "Duplicated BooleanEvaluator for name '" + name + "'.");
      }
    }
    return evaluators;
  }
}
