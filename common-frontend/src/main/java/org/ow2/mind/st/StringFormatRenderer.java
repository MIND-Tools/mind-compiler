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

package org.ow2.mind.st;

public class StringFormatRenderer extends AbstractDelegatingAttributeRenderer {

  public static final String TO_UPPER             = "toUpper";
  public static final String TO_C_NAME            = "toCName";
  public static final String TO_UPPER_C_NAME      = "toUpperCName";
  public static final String PATH_TO_C_NAME       = "pathToCName";
  public static final String PATH_TO_UPPER_C_NAME = "pathToUpperCName";

  @Override
  protected String render(final Object o, final String formatName) {
    if (TO_UPPER.equals(formatName)) {
      return o.toString().toUpperCase();
    } else if (TO_C_NAME.equals(formatName)) {
      return o.toString().replace('.', '_');
    } else if (TO_UPPER_C_NAME.equals(formatName)) {
      return o.toString().replace('.', '_').toUpperCase();
    } else if (PATH_TO_C_NAME.equals(formatName)) {
      return o.toString().replace('/', '_').replace('.', '_');
    } else if (PATH_TO_UPPER_C_NAME.equals(formatName)) {
      return o.toString().replace('/', '_').replace('.', '_').toUpperCase();
    }
    return null;
  }
}
