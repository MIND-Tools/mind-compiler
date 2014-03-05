/**
 * Copyright (C) 2014 Schneider-Electric
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
 * Authors: Stephane Seyvoz
 * Contributors: 
 */

package org.ow2.mind.cli;

import java.util.Map;

import org.ow2.mind.adl.BasicDefinitionCompiler;

/**
 * This class allows setting and getting information about the "KeepSrcName"
 * naming convention, to be enabled or not in the adl-backend.
 * 
 * @see KeepSourceNameOptionHandler
 * @see BasicDefinitionCompiler
 * @author Stephane Seyvoz
 */
public class KeepSrcNameContextHelper {

  public static final String KEEP_SRC_NAME_CONTEXT_HELPER = "keep-src-name";

  public static boolean getKeepSourceName(final Map<Object, Object> context) {
    if (context == null) return false;
    final Boolean b = (Boolean) context.get(KEEP_SRC_NAME_CONTEXT_HELPER);
    if (b == null)
      return false;
    else
      return b;
  }

  public static void setKeepSourceName(final Map<Object, Object> context,
      final boolean forceRegen) {
    context.put(KEEP_SRC_NAME_CONTEXT_HELPER, forceRegen);
  }

}
