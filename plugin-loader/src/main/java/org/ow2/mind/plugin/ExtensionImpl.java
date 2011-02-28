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

package org.ow2.mind.plugin;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class ExtensionImpl implements Extension {

  private static final String              ID_ATTR_NAME    = "id";
  private static final String              NAME_ATTR_NAME  = "name";
  private static final String              POINT_ATTR_NAME = "point";

  private final Plugin                     plugin;
  private final String                     pointID;
  private final String                     id;
  private final String                     name;
  private final List<ConfigurationElement> configurationElements;

  ExtensionImpl(final PluginImpl plugin, final Element element) {
    this.plugin = plugin;
    pointID = element.getAttribute(POINT_ATTR_NAME);
    if (pointID == null) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Invalid extenstion node, missing point ID in'"
              + element.getBaseURI() + "'.");
    }
    id = element.getAttribute(ID_ATTR_NAME);
    name = element.getAttribute(NAME_ATTR_NAME);

    final NodeList childNodes = element.getChildNodes();
    configurationElements = new ArrayList<ConfigurationElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final org.w3c.dom.Node node = childNodes.item(i);
      if (!(node instanceof Element)) continue;

      configurationElements.add(new ConfigurationElementImpl(plugin, this,
          (Element) node));
    }
  }

  public String getExtensionPointID() {
    return pointID;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public Iterable<ConfigurationElement> getConfigurationElements() {
    return Iterables.unmodifiableIterable(configurationElements);
  }

  public Iterable<ConfigurationElement> getConfigurationElements(
      final String name) {
    return Iterables.filter(configurationElements,
        new Predicate<ConfigurationElement>() {
          public boolean apply(final ConfigurationElement input) {
            return input.getName().equals(name);
          }
        });
  }

}
