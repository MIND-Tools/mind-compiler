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

package org.ow2.mind.adl.implementation;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.adl.Definition;

public class SharedImplementationDecorationHelper {

  public static String SHARED_IMPLEMENTATION_DECORATION_NAME = "shared-implementation";

  public static void addSharedImplementation(final Definition def,
      final String path) {
    Set<String> decoration = getDecoration(def);
    if (decoration == null) {
      decoration = new HashSet<String>();
      def.astSetDecoration(SHARED_IMPLEMENTATION_DECORATION_NAME, decoration);
    }
    decoration.add(path);
  }

  public static Set<String> getSharedImplementation(final Definition def) {
    final Set<String> decoration = getDecoration(def);
    if (decoration == null) {
      return new HashSet<String>();
    }
    return decoration;
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getDecoration(final Definition def) {
    return (Set<String>) def
        .astGetDecoration(SHARED_IMPLEMENTATION_DECORATION_NAME);
  }
}
