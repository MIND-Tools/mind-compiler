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

package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;

/**
 * Base interface for plugin extension elements.
 */
public interface Extension extends Node {
  /**
   * Sets the extension point corresponding to this extension node.
   * 
   * @param point The qualified name of the extension point which corresponds to
   *          this extension.
   */
  void setPoint(String point);

  String getPoint();
}
