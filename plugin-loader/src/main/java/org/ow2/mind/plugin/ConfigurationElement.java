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

import java.util.Map;

/**
 * Base interface for configuration element.
 */
public interface ConfigurationElement {

  /**
   * Returns the name of this configuration element.
   * 
   * @return the name of this configuration element.
   */
  String getName();

  /**
   * Returns the value of the attribute with the given name.
   * 
   * @param name an attribute name.
   * @return the value of the attribute with the given name, or
   *         <code>null</code> if this configuration element have no attribute
   *         with the given name.
   */
  String getAttribute(String name);

  /**
   * Returns the value of the attribute with the given name, or the given
   * default value if this configuration element have no attribute with the
   * given name.
   * 
   * @param name an attribute name.
   * @param defaultValue the default value.
   * @return the value of the attribute with the given name, or the given
   *         default value if this configuration element have no attribute with
   *         the given name.
   */
  String getAttribute(String name, String defaultValue);

  /**
   * Returns the map of every attributes.
   * 
   * @return an unmodifiable map associating attribute names to their values.
   */
  Map<String, String> getAttributes();

  /**
   * Interprets the attribute <code>attrName</code> as a fully qualified name of
   * a java class and returns an instance of this class.
   * 
   * @param <T> the expected type of the java class
   * @param attrName the name of the attribute
   * @param expectedType the class that is supposed to be a super class of the
   *          created instance.
   * @return an instance of the class whose name is the value of the attribute
   *         called <code>attrName</code>. Returns <code>null</code> if the
   *         attribute has no value.
   */
  <T> T createInstance(String attrName, Class<T> expectedType);

  /**
   * Returns the configuration element children.
   * 
   * @return the configuration element children. Returns an empty iterable if
   *         this {@link ConfigurationElement} has no child.
   */
  Iterable<ConfigurationElement> getChildren();

  /**
   * Returns the first configuration element child.
   * 
   * @return the first configuration element child. Returns <code>null</code> if
   *         this configuration element has no child.
   */
  ConfigurationElement getChild();

  /**
   * Returns the configuration element children with the given name.
   * 
   * @param name the name of the y objects to return.
   * @return configuration element children. Returns an empty iterable if this y
   *         has no child.
   */
  Iterable<ConfigurationElement> getChildren(String name);

  /**
   * Returns the first configuration element child with the given name.
   * 
   * @param name the name of the configuration element objects to return.
   * @return a configuration element child. Returns <code>null</code> if this
   *         configuration element has no child with the given name.
   */
  ConfigurationElement getChild(String name);

  /**
   * Returns the parent of this configuration element}.
   * 
   * @return the parent of this configuration element. Can be a
   *         {@link ConfigurationElement} or an {@link Extension}.
   */
  Object getParent();
}
