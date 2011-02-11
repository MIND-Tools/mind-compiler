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
 * Base interface for plugin elements.
 */
public interface Plugin {

  /**
   * Returns the identifier of this plugin.
   * 
   * @return the identifier of this plugin.
   */
  String getId();

  /**
   * Returns the name of this plugin.
   * 
   * @return the name of this plugin.
   */
  String getName();

  /**
   * Returns the {@link Extension} that are defined in this plugin.
   * 
   * @return the {@link Extension} that are defined in this plugin. Returns an
   *         empty iterable if this plugin has no extension.
   */
  Iterable<Extension> getExtensions();

  /**
   * Returns the {@link ExtensionPoint} that are defined in this plugin.
   * 
   * @return the {@link ExtensionPoint} that are defined in this plugin. Returns
   *         an empty iterable if this plugin has no extension-point.
   */
  Iterable<ExtensionPoint> getExtensionPoints();
}
