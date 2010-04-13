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

package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;
import org.w3c.dom.Element;

/**
 * Helper methods for plugin AST nodes.
 */
public final class PluginASTHelper {
  private PluginASTHelper() {
  }

  /**
   * The name of the {@link Node#astGetDecoration(String) decoration} used to
   * store the configuration DOM tree on {@link Extension} nodes.
   * 
   * @see #getExtensionConfig(Extension)
   * @see #setExtensionConfig(Extension, Element)
   */
  public static final String EXTENSION_CONFIG_DECORATION_NAME = "xml-element";

  /**
   * Sets the DOM tree that contains the configuration of the given
   * {@link Extension} node.
   * 
   * @param extension an extension node.
   * @param config the configuration DOM tree.
   * @see #getExtensionConfig(Extension)
   */
  public static void setExtensionConfig(Extension extension, Element config) {
    extension.astSetDecoration(EXTENSION_CONFIG_DECORATION_NAME, config);
  }

  /**
   * Returns the configuration DOM tree of the given {@link Extension} node.
   * 
   * @param extension an extension node.
   * @return the configuration DOM tree.
   * @see #setExtensionConfig(Extension, Element)
   */
  public static Element getExtensionConfig(Extension extension) {
    return (Element) extension
        .astGetDecoration(EXTENSION_CONFIG_DECORATION_NAME);
  }
}
