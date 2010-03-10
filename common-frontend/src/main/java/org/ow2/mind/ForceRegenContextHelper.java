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

package org.ow2.mind;

import java.util.Map;

public final class ForceRegenContextHelper {
  private ForceRegenContextHelper() {
  }

  public static final String FORCE_REGEN_CONTEXT_HELPER = "force-regen";

  public static boolean getForceRegen(final Map<Object, Object> context) {
    if (context == null) return true;
    final Boolean b = (Boolean) context.get(FORCE_REGEN_CONTEXT_HELPER);
    return b == null || b;
  }

  public static void setForceRegen(final Map<Object, Object> context,
      final boolean forceRegen) {
    context.put(FORCE_REGEN_CONTEXT_HELPER, forceRegen);
  }

  public static final String KEEP_TEMP_CONTEXT_HELPER = "keep-temp";

  public static boolean getKeepTemp(final Map<Object, Object> context) {
    if (context == null) return true;
    final Boolean b = (Boolean) context.get(KEEP_TEMP_CONTEXT_HELPER);
    return b == null || b;
  }

  public static void setKeepTemp(final Map<Object, Object> context,
      final boolean keepTemp) {
    context.put(KEEP_TEMP_CONTEXT_HELPER, keepTemp);
  }

  public static final String NO_BINARY_AST_CONTEXT_KEY = "no-binary-ast";

  public static boolean getNoBinaryAST(final Map<Object, Object> context) {
    if (context == null) return true;
    final Boolean b = (Boolean) context.get(NO_BINARY_AST_CONTEXT_KEY);
    return b == null || b;
  }

  public static void setNoBinaryAST(final Map<Object, Object> context,
      final boolean noBinaryAST) {
    context.put(NO_BINARY_AST_CONTEXT_KEY, noBinaryAST);
  }
}
