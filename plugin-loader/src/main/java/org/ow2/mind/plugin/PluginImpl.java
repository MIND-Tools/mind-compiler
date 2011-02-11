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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

class PluginImpl implements Plugin {

  private final PluginManager        pluginManager;
  private final String               id;
  private final String               name;
  private final URL                  descURL;
  private final ClassLoader          classLoader;
  private final List<Extension>      extensions;
  private final List<ExtensionPoint> extensionPoints;

  PluginImpl(final PluginManager pluginManager, final URL descURL,
      final String id, final String name, final ClassLoader classLoader) {
    this.pluginManager = pluginManager;
    this.id = id;
    this.name = name;
    this.descURL = descURL;
    this.classLoader = classLoader;
    this.extensions = new ArrayList<Extension>();
    this.extensionPoints = new ArrayList<ExtensionPoint>();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Iterable<Extension> getExtensions() {
    return Iterables.unmodifiableIterable(extensions);
  }

  public Iterable<ExtensionPoint> getExtensionPoints() {
    return Iterables.unmodifiableIterable(extensionPoints);
  }

  void addExtension(final Extension extension) {
    extensions.add(extension);
  }

  void addExtensionPoint(final ExtensionPoint extensionPoint) {
    extensionPoints.add(extensionPoint);
  }

  ClassLoader getClassLoader() {
    return classLoader;
  }

  URL getDescriptorURL() {
    return descURL;
  }

  PluginManager getPluginManager() {
    return pluginManager;
  }
}
