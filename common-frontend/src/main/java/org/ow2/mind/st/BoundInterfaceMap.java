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

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroupLoader;

public class BoundInterfaceMap extends HashMap<String, String> {

  private static final long serialVersionUID = -7974148690318566114L;

  public BoundInterfaceMap(final StringTemplateGroup group,
      final String prefix, final String signature,
      final StringTemplateGroupLoader pluginLoader) {

    StringTemplateGroup subgroup;
    // Always load considering the superGroup which is equal to our group in
    // order to support nested template maps with multiple hierarchies.
    subgroup = (StringTemplateGroup) pluginLoader.loadGroup(prefix, group);

    if (!subgroup.implementsInterface(signature)) {
      // Throw exception
    }

    for (final Object o : subgroup.getTemplateNames().toArray()) {
      final String templateName = (String) o;
      final String localTemplateName = "__" + prefix.replace('.', '_') + "_"
          + templateName;
      this.put(templateName, localTemplateName);

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
  }
}
