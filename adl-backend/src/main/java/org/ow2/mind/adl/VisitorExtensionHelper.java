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

package org.ow2.mind.adl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.util.BooleanEvaluatorHelper;

public class VisitorExtensionHelper {

  public static final String  DEFINITION_SOURCE_GENERATOR_EXTENSION = "org.ow2.mind.adl.definition-source-generators";
  public static final String  INSTANCE_SOURCE_GENERATOR             = "org.ow2.mind.adl.instance-source-generators";

  private static final String CLASS                                 = "class";
  private static final String VISITOR                               = "visitor";
  private static final String ENABLE_WHEN                           = "enableWhen";

  public static Collection<DefinitionSourceGenerator> getDefinitionSourceGeneratorExtensions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context) {
    final Collection<DefinitionSourceGenerator> generators = new ArrayList<DefinitionSourceGenerator>();
    for (final ConfigurationElement configElement : pluginManagerItf
        .getConfigurationElements(DEFINITION_SOURCE_GENERATOR_EXTENSION,
            VISITOR)) {
      final ConfigurationElement condition = configElement
          .getChild(ENABLE_WHEN);
      if (condition == null
          || BooleanEvaluatorHelper.evaluate(condition.getChild(),
              pluginManagerItf, context)) {
        generators.add(configElement.createInstance(CLASS,
            DefinitionSourceGenerator.class));
      }
    }
    return generators;
  }

  public static Collection<InstanceSourceGenerator> getInstanceSourceGeneratorExtensions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context) {
    final Collection<InstanceSourceGenerator> generators = new ArrayList<InstanceSourceGenerator>();
    for (final ConfigurationElement configElement : pluginManagerItf
        .getConfigurationElements(DEFINITION_SOURCE_GENERATOR_EXTENSION,
            VISITOR)) {
      final ConfigurationElement condition = configElement
          .getChild(ENABLE_WHEN);
      if (condition == null
          || BooleanEvaluatorHelper.evaluate(condition.getChild(),
              pluginManagerItf, context)) {
        generators.add(configElement.createInstance(CLASS,
            InstanceSourceGenerator.class));
      }
    }

    return generators;
  }
}
