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
import java.util.regex.Pattern;

import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;

/**
 * {@link BooleanEvaluator} that tests the content of the context map. The
 * evaluated {@link ConfigurationElement} must have at least a <code>key</code>
 * attribute and may have a <code>value</code> or <code>valueRegExp</code>
 * attribute.
 */
public class ContextBooleanEvaluator implements BooleanEvaluator {

  private static final String KEY          = "key";
  private static final String VALUE        = "value";
  private static final String VALUE_REGEXP = "valueRegExp";

  public boolean evaluate(final ConfigurationElement configurationElement,
      final PluginManager pluginManager, final Map<Object, Object> context) {
    Assert.assertNotNull(context, "ContextBooleanEvaluator requires a context");

    final String key = configurationElement.getAttribute(KEY);
    Assert.assertNotNull(context, "ContextBooleanEvaluator key is null");

    if (!context.containsKey(key)) return false;

    final String contextValue = context.get(key).toString();

    final String value = configurationElement.getAttribute(VALUE);
    if (value != null) {
      return value.equals(contextValue);
    } else {
      final String valueRegExp = configurationElement
          .getAttribute(VALUE_REGEXP);
      if (valueRegExp != null) {
        return Pattern.matches(valueRegExp, contextValue);
      } else {
        return true;
      }
    }
  }
}
