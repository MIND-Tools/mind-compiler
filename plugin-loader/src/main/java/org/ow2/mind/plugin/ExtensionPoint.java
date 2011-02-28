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
 * Authors: Ali Erdem Ozcan
 */

package org.ow2.mind.plugin;

/**
 * Base interface for extension-points element.
 */
public interface ExtensionPoint {

  /**
   * Returns the local-identifier of this extension-point.
   * 
   * @return the local-identifier of this extension-point.
   */
  String getId();

  /**
   * Returns the fully qualified identifier of this extension point. The
   * fully-qualified name of the extension-point is made of the identifier of
   * the plugin that defines it and its local identifier separated by a dot.
   * 
   * @return the fully qualified identifier of this extension point.
   */
  String getQualifiedId();

  /**
   * Returns the name of this extension-point.
   * 
   * @return the name of this extension-point. May be <code>null</code>
   */
  String getName();

  /**
   * Returns the {@link Plugin} that defines this extension-point.
   * 
   * @return the {@link Plugin} that defines this extension-point.
   */
  Plugin getPlugin();

  /**
   * Returns the {@link Extension} objects that are bound to this
   * extension-point.
   * 
   * @return {@link Extension} objects that are bound to this extension-point.
   *         Returns an empty iterable if no extension is bound.
   */
  Iterable<Extension> getExtensions();

  /**
   * Returns all the top-level {@link ConfigurationElement} objects of
   * extensions that are bound to this extension-point.
   * 
   * @return {@link ConfigurationElement} objects. Returns an empty iterable if
   *         no extension is bound.
   */
  Iterable<ConfigurationElement> getConfigurationElements();

  /**
   * Returns all the top-level {@link ConfigurationElement} objects with the
   * given name of extensions that are bound to this extension-point.
   * 
   * @param name the name of the {@link ConfigurationElement} objects to return.
   * @return {@link ConfigurationElement} objects. Returns an empty iterable if
   *         no {@link ConfigurationElement} is found.
   */
  Iterable<ConfigurationElement> getConfigurationElements(String name);

}
