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

package org.ow2.mind.compilation;

import static java.lang.Character.isWhitespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for manipulating directive nodes.
 */
public final class DirectiveHelper {
  private DirectiveHelper() {
  }

  public static List<String> splitOptionString(final String s) {
    if (s == null) {
      return Collections.emptyList();
    }

    final List<String> result = new ArrayList<String>();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      if (isWhitespace(c)) {
        if (sb.length() != 0) {
          result.add(sb.toString());
          sb = new StringBuilder();
        }
      } else if (c == '\\') {
        if (i + 1 < s.length() && isWhitespace(s.charAt(i + 1))) {
          sb.append(s.charAt(i + 1));
          i++;
        } else {
          sb.append('\\');
        }
      } else {
        sb.append(c);
      }
    }
    if (sb.length() != 0) result.add(sb.toString());

    return result;
  }
}
