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
 * Base interface for extensions element.
 */
public interface Extension {

  /**
   * Returns the identifier of the extension-point to which this extension is
   * bound to.
   * 
   * @return the identifier of the extension-point to which this extension is
   *         bound to.
   * @see ExtensionPoint#getQualifiedId()
   */
  String getExtensionPointID();

  /**
   * Returns the identifier of this extension.
   * 
   * @return the identifier of this extension. May be <code>null</code>.
   */
  String getId();

  /**
   * Returns the name of this extension.
   * 
   * @return the name of this extension. May be <code>null</code>.
   */
  String getName();

  /**
   * Returns the {@link Plugin} that defines this extension.
   * 
   * @return the {@link Plugin} that defines this extension.
   */
  Plugin getPlugin();

  /**
   * Returns the top-level {@link ConfigurationElement} objects contained by
   * this extension.
   * 
   * @return {@link ConfigurationElement} objects. Returns an empty iterable if
   *         no {@link ConfigurationElement} is found.
   */
  Iterable<ConfigurationElement> getConfigurationElements();

  /**
   * Returns the top-level {@link ConfigurationElement} objects with the given
   * name that are contained by this extension.
   * 
   * @param name the name of the {@link ConfigurationElement} objects to return.
   * @return {@link ConfigurationElement} objects. Returns an empty iterable if
   *         no {@link ConfigurationElement} is found.
   */
  Iterable<ConfigurationElement> getConfigurationElements(String name);
}
