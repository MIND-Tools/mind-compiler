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

package org.ow2.mind.idl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ow2.mind.idl.ast.InterfaceDefinition;

public final class ExtendedInterfaceDecorationHelper {
  private ExtendedInterfaceDecorationHelper() {
  }

  public static final String EXTENDED_INTERFACE_DECORATION_NAME = "extended-interfaces";

  public static void addExtendedInterface(final InterfaceDefinition itf,
      final InterfaceDefinition extendedInterface) {
    Set<String> decoration = getDecoration(itf);
    if (decoration == null) {
      decoration = new HashSet<String>();
      itf.astSetDecoration(EXTENDED_INTERFACE_DECORATION_NAME, decoration);
    }
    decoration.add(extendedInterface.getName());
    final Set<String> extendedDeco = getDecoration(extendedInterface);
    if (extendedDeco != null) decoration.addAll(extendedDeco);
  }

  public static Set<String> getExtendedInterface(final InterfaceDefinition itf) {
    final Set<String> decoration = getDecoration(itf);
    if (decoration == null) {
      return Collections.emptySet();
    }
    return decoration;
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getDecoration(final InterfaceDefinition itf) {
    return (Set<String>) itf
        .astGetDecoration(EXTENDED_INTERFACE_DECORATION_NAME);
  }
}
