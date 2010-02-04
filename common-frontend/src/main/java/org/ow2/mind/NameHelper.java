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

package org.ow2.mind;

import java.util.regex.Pattern;

/**
 * Utility class to manipulate fully-qualified names.
 */
public final class NameHelper {

  private NameHelper() {
  }

  /** The regular expression of a name. */
  public static final String   NAME_REQEXP  = "([a-zA-Z_]\\w*(\\$\\d*)?)|([a-zA-Z_]\\w*(\\.[a-zA-Z_]\\w*(\\$\\d*)?)+)";
  private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REQEXP);

  /**
   * Checks if the given string matches the {@link #NAME_REQEXP} regular
   * expression.
   * 
   * @param name the string to check
   * @return <code>true</code> if and only if the given string matches the
   *         {@link #NAME_REQEXP} regular expression.
   */
  public static final boolean isValid(final String name) {
    return NAME_PATTERN.matcher(name).matches();
  }

  /**
   * Returns the package name part of the given fully-qualified name. If the
   * given fully-qualified name does not contain a package name, this method
   * returns <code>null</code>.
   * 
   * @param name a valid fully-qualified name (see {@link #isValid(String)}).
   * @return the package name part of the given fully-qualified name.
   */
  public static final String getPackageName(final String name) {
    if (!isValid(name))
      throw new IllegalArgumentException("Name \"" + name
          + "\" is not a valid name.");
    final int i = name.lastIndexOf('.');
    return i == -1 ? null : name.substring(0, i);
  }

  /**
   * Split the given fully-qualified name in an array of the identifiers that it
   * is made of.
   * 
   * @param name a valid fully-qualified name (see {@link #isValid(String)}).
   * @return an array of the identifiers that compose the given fully qualified
   *         name.
   */
  public static final String[] splitName(final String name) {
    if (!isValid(name))
      throw new IllegalArgumentException("Name \"" + name
          + "\" is not a valid name.");
    return name.split("\\.");
  }

  public static final String toValidName(String name) {
    int i = name.indexOf('<');
    if (i >= 0) {
      // name contains template parameter
      final String tmplValues = name.substring(i);
      final String defName = name.substring(0, i);
      if (!isValid(defName))
        throw new IllegalArgumentException("Name \"" + defName
            + "\" is not a valid name.");
      return defName + "_tmpl_" + Integer.toHexString(tmplValues.hashCode());
    }
    i = name.indexOf('$');
    if (i >= 0) {
      // name references an anonymous definition
      name = name.replace("$", "_anon_");
    }

    if (!isValid(name))
      throw new IllegalArgumentException("Name \"" + name
          + "\" is not a valid name.");

    return name;
  }
}
