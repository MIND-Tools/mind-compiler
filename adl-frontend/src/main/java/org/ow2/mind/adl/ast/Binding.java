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
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl.ast;

import org.objectweb.fractal.adl.Node;

public interface Binding extends Node {

  String THIS_COMPONENT = "this";

  String getFromComponent();

  void setFromComponent(String fromComponent);

  String getFromInterface();

  void setFromInterface(String fromInterface);

  String getFromInterfaceNumber();

  void setFromInterfaceNumber(String fromInterfaceNumber);

  String getToComponent();

  void setToComponent(String toComponent);

  String getToInterface();

  void setToInterface(String toInterface);

  String getToInterfaceNumber();

  void setToInterfaceNumber(String toInterfaceNumber);
}
