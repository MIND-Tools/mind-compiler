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

/**
 * PluginManager can used to retrieve configurations of plugins and extension
 * points.
 */
public interface PluginManager {

  /**
   * Returns the {@link ExtensionPoint} object corresponding to the given
   * extension-point identifier.
   * 
   * @param extensionPoint an extension-point identifier.
   * @return an {@link ExtensionPoint} object or <code>null</code> if no
   *         extension-point can be found with the given identifier.
   */
  ExtensionPoint getExtensionPoint(String extensionPoint);

  /**
   * Returns the {@link Extension} objects that are bound to the given
   * extension-point.
   * 
   * @param extensionPoint an extension-point identifier.
   * @return the {@link Extension} objects, or <code>null</code> if no
   *         extension-point can be found with the given identifier.
   * @see ExtensionPoint#getExtensions()
   */
  Iterable<Extension> getExtensions(String extensionPoint);

  /**
   * Returns all the top-level {@link ConfigurationElement} objects of every
   * extensions that are bound to the given extension-point.
   * 
   * @param extensionPoint an extension-point identifier.
   * @return the {@link ConfigurationElement} objects, or <code>null</code> if
   *         no extension-point can be found with the given identifier.
   * @see ExtensionPoint#getConfigurationElements()
   */
  Iterable<ConfigurationElement> getConfigurationElements(String extensionPoint);

  /**
   * Returns all the top-level {@link ConfigurationElement} objects with the
   * given name of every extensions that are bound to the given extension-point.
   * 
   * @param extensionPoint an extension-point identifier.
   * @param name the name of the {@link ConfigurationElement} to return.
   * @return the {@link ConfigurationElement} objects, or <code>null</code> if
   *         no extension-point can be found with the given identifier.
   * @see ExtensionPoint#getConfigurationElements()
   */
  Iterable<ConfigurationElement> getConfigurationElements(
      String extensionPoint, String name);

  /**
   * Returns the identifiers of every extension-points.
   * 
   * @return the identifiers of every extension-points.
   */
  Iterable<String> getExtensionPointNames();

  /**
   * Creates an instance of the given class. This method is not intended to be
   * used directly by clients. Clients should use
   * {@link ConfigurationElement#createInstance(String, Class)} instead.
   * 
   * @param <T> The type of created object
   * @param clazz the class to instantiate.
   * @return an instance of the given class.
   */
  <T> T getInstance(Class<T> clazz);
}
