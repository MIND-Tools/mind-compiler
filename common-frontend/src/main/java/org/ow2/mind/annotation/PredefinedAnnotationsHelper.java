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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.util.BooleanEvaluatorHelper;

public final class PredefinedAnnotationsHelper {

  public static final String  ANNOTATION_EXTENSIONS = "org.ow2.mind.predefined-annotations";
  private static final String ANNOTATION            = "annotation";
  private static final String PACKAGE               = "package";
  private static final String ENABLE_WHEN           = "enableWhen";

  private static List<String> predefinedAnnotations;

  private PredefinedAnnotationsHelper() {
  }

  public static List<String> getPredefinedAnnotations(
      final PluginManager pluginManagerItf, final Map<Object, Object> context) {
    if (predefinedAnnotations == null)
      initPredefinedAnnotations(pluginManagerItf, context);
    return Collections.unmodifiableList(predefinedAnnotations);
  }

  private static void initPredefinedAnnotations(
      final PluginManager pluginManagerItf, final Map<Object, Object> context) {
    predefinedAnnotations = new ArrayList<String>();
    final Iterable<ConfigurationElement> configurationElements = pluginManagerItf
        .getConfigurationElements(ANNOTATION_EXTENSIONS, ANNOTATION);
    for (final ConfigurationElement configurationElement : configurationElements) {
      final ConfigurationElement condition = configurationElement
          .getChild(ENABLE_WHEN);
      if (condition == null
          || BooleanEvaluatorHelper.evaluate(condition.getChild(),
              pluginManagerItf, context)) {
        predefinedAnnotations.add(configurationElement.getAttribute(PACKAGE));
      }
    }
  }
}
