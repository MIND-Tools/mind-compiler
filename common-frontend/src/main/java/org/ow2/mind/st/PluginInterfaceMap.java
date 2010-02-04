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

package org.ow2.mind.st;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroupLoader;

public class PluginInterfaceMap extends HashMap<String, Map<String, String>> {

  /**
   * Generated serial ID
   */
  private static final long       serialVersionUID = -7974148690318566114L;

  final StringTemplateGroup       group;
  final String                    repository;
  final String                    signature;
  final StringTemplateGroupLoader pluginLoader;

  public PluginInterfaceMap(final StringTemplateGroup group,
      final String prefix, final String signature,
      final StringTemplateGroupLoader pluginLoader) {
    this.group = group;
    this.repository = prefix;
    this.signature = signature;
    this.pluginLoader = pluginLoader;
  }

  @Override
  public Map<String, String> get(final Object key) {
    final Map<String, String> result = super.get(key);
    if (result != null) {
      return result;
    }

    if (!tryPut(key)) {
      return null;
    }

    return super.get(key);
  }

  protected boolean tryPut(final Object key) {
    StringTemplateGroup subgroup;

    final String subGroupName = repository + "."
        + ((String) key).substring(0, 1).toUpperCase()
        + ((String) key).substring(1);

    // Always load considering the supoerGroup which is equal to our group in
    // order to support nested template maps with multiple hierarchies.
    subgroup = (StringTemplateGroup) pluginLoader
        .loadGroup(subGroupName, group);

    if (!subgroup.implementsInterface(signature)) {
      System.err.println(subGroupName
          + " doesn' t implement the required interface '" + signature + "'.");
      return false;
    }
    // subgroup.setSuperGroup(group.getStringTemplateGroup());

    final Map<String, String> map = new HashMap<String, String>();
    // Transport the templates

    for (final Object o : subgroup.getTemplateNames().toArray()) {
      final String templateName = (String) o;
      final String localTemplateName = "__" + subGroupName.replace('.', '_')
          + "_" + templateName;
      map.put(templateName, localTemplateName);
      this.put((String) key, map);

      // Get and prepare the receiver template
      final StringTemplate receiverTemplate = subgroup
          .getTemplateDefinition(templateName);

      final StringTemplate skeletonTemplate = subgroup.defineTemplate(
          templateName + "__Skeleton", receiverTemplate.getTemplate());
      for (final String mapName : subgroup.getMapsKeySet()) {
        skeletonTemplate.defineFormalArgument(mapName,
            new MapDelegateStringTemplate(subgroup.getMap(mapName)));
      }

      // Create a stub template
      final StringTemplate stubTemplate = group.defineTemplate(
          localTemplateName, "<__skeleton_template>");
      stubTemplate.setFormalArguments((LinkedHashMap<?, ?>) receiverTemplate
          .getFormalArguments());
      stubTemplate
          .defineFormalArgument("__skeleton_template", skeletonTemplate);

    }

    return true;
  }

  @Override
  public boolean containsKey(final Object key) {
    final boolean result = super.containsKey(key);
    if (result)
      return result;
    else
      return tryPut(key);
  }

}
