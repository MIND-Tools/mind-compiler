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

import static org.ow2.mind.NameHelper.getPackageName;
import static org.ow2.mind.NameHelper.toValidName;

import java.util.regex.Pattern;

/**
 * Utility class that provides methods to manipulate path-like strings.
 */
public final class PathHelper {
  private PathHelper() {
  }

  /** The regular expression of a path. */
  public static final String   PATH_REGEXP  = "((\\./)?(\\.\\./)*|/)\\w+(/\\w+)*(\\.\\w+)?";
  private static final Pattern PATH_PATTERN = Pattern.compile(PATH_REGEXP);

  /**
   * Checks if the given string matches the {@link #PATH_REGEXP} regular
   * expression.
   * 
   * @param path the string to check
   * @return <code>true</code> if and only if the given string matches the
   *         {@link #PATH_REGEXP} regular expression.
   */
  public static boolean isValid(final String path) {
    return PATH_PATTERN.matcher(path).matches();
  }

  /**
   * Returns <code>true</code> if the given path is a relative path (i.e. do not
   * starts with <code>"/"</code>)
   * 
   * @param path a valid path (see {@link #isValid(String)}).
   * @return <code>true</code> if the given path is a relative path (i.e. do not
   *         starts with <code>"/"</code>)
   */
  public static boolean isRelative(final String path) {
    return !path.startsWith("/");
  }

  public static String getParent(final String path) {
    final int i = path.lastIndexOf('/');
    if (i == -1) return "";
    if (i == 0) return "/";
    return path.substring(0, i);
  }

  public static String getFileName(final String path) {
    final int i = path.lastIndexOf('/');
    if (i == -1) return path;
    return path.substring(i + 1);
  }

  /**
   * Returns the extension of the file denoted by the given path.
   * 
   * @param path a valid path (see {@link #isValid(String)}).
   * @return the extension of the file denoted by the given path (without the
   *         <code>'.'<code>) or <code>null</code> if the given path do not have
   *         an extension.
   */
  public static String getExtension(final String path) {
    final int i = path.lastIndexOf('.');
    final int j = path.lastIndexOf('/');
    if (i == -1 || j > i) return null;
    return path.substring(i + 1);
  }

  /**
   * Returns the given path without its file extension.
   * 
   * @param path a valid path (see {@link #isValid(String)}).
   * @return the given path without its file extension.
   */
  public static String removeExtension(final String path) {
    final int i = path.lastIndexOf('.');
    final int j = path.lastIndexOf('/');
    if (i == -1 || j > i) return path;
    return path.substring(0, i);
  }

  /**
   * Replaces the file extension of the given path by the given extension
   * 
   * @param path a valid path (see {@link #isValid(String)}).
   * @param extension the extension to set. If <code>extension</code> does not
   *          starts with ".", it is automatically added.
   * @return the given path in which the file extension has been replaced by the
   *         given one.
   */
  public static String replaceExtension(final String path, String extension) {
    if (!extension.startsWith(".")) extension = "." + extension;
    return removeExtension(path) + extension;
  }

  /**
   * Returns the absolute path that is denoted by the given <code>path</code>
   * relatively to the given <code>dirName</code>. If the given path is not a
   * relative path (see {@link #isRelative(String)}), this method returns the
   * given <code>path</code>.
   * 
   * @param dirName the directory that is the starting point of the given
   *          <code>path</code>.
   * @param path a valid path (see {@link #isValid(String)}).
   * @return the absolute path that is denoted by the given
   *         <code>path<code> relatively to the given <code>dirName</code>.
   */
  public static String toAbsolute(String dirName, String path) {
    if (!isRelative(path)) return path;

    // remove '/' at the end of dirName if any
    if (dirName.endsWith("/"))
      dirName = dirName.substring(0, dirName.length() - 1);

    if (!path.startsWith("../")) {
      if (path.startsWith("./")) path = path.substring(2);
      if (!path.startsWith("../")) {
        // path is a relative path that do not starts by "./" or "../"
        return dirName + "/" + path;
      }
    }

    assert path.startsWith("../");
    final int lastSlash = dirName.lastIndexOf('/');
    if (lastSlash <= 0) {
      if (path.startsWith("../../")) {
        throw new IllegalArgumentException("Invalid relative path \"" + path
            + "\" for directory \"" + dirName + "\".");
      }
      return (dirName.startsWith("/")) ? "/" + path.substring(3) : path
          .substring(3);
    }
    return toAbsolute(dirName.substring(0, lastSlash - 1), "./"
        + path.substring(3));
  }

  /**
   * Returns the directory name that correspond to the package name of the given
   * fully qualified name.
   * 
   * @param fullyQualifiedName a fully qualified name
   * @return the directory name that correspond to the package name of the given
   *         fully qualified name.
   */
  public static String fullyQualifiedNameToDirName(
      final String fullyQualifiedName) {
    final String packageName = getPackageName(toValidName(fullyQualifiedName));
    if (packageName == null) return "/";
    return packageNameToDirName(packageName);
  }

  /**
   * Returns the directory name that correspond to the given package name
   * 
   * @param packageName a package name
   * @return the directory name that correspond to the given package name
   */
  public static String packageNameToDirName(final String packageName) {
    return "/" + packageName.replace('.', '/');
  }

  /**
   * Returns the absolute path that is denoted by the given <code>path</code>
   * relatively to the directory that correspond to the package name of the
   * given fully-qualified name. If the given path is not a relative path (see
   * {@link #isRelative(String)}), this method returns the given
   * <code>path</code>.
   * 
   * @param fullyQualifiedName a fully qualified name
   * @param path a valid path (see {@link #isValid(String)}).
   * @return the absolute path that is denoted by the given <code>path</code>
   *         relatively to the directory that correspond to the package name of
   *         the given fully-qualified name.
   * @see #fullyQualifiedNameToDirName(String)
   * @see #toAbsolute(String, String)
   */
  public static String fullyQualifiedNameToAbsolute(
      final String fullyQualifiedName, final String path) {
    return toAbsolute(fullyQualifiedNameToDirName(fullyQualifiedName), path);
  }

  /**
   * Returns the absolute path that is denoted by the given <code>path</code>
   * relatively to the directory that correspond to the given package name. If
   * the given path is not a relative path (see {@link #isRelative(String)}),
   * this method returns the given <code>path</code>.
   * 
   * @param packageName a package name
   * @param path a valid path (see {@link #isValid(String)}).
   * @return the absolute path that is denoted by the given <code>path</code>
   *         relatively to the directory that correspond to the given package
   *         name.
   * @see #packageNameToDirName(String)
   * @see #toAbsolute(String, String)
   */
  public static String packageNameToAbsolute(final String packageName,
      final String path) {
    return toAbsolute(packageNameToDirName(packageName), path);
  }

  /**
   * Returns the absolute path corresponding to the given fully qualified name.
   * 
   * @param fullyQualifiedName a fully qualified name
   * @param extension the file extension to add at the end of the returned path.
   *          May be <code>null</code>
   * @return the absolute path corresponding to the given fully qualified name.
   */
  public static String fullyQualifiedNameToPath(
      final String fullyQualifiedName, final String extension) {
    return fullyQualifiedNameToPath(fullyQualifiedName, null, extension);
  }

  /**
   * Returns the absolute path corresponding to the given fully qualified name
   * followed by the given suffix.
   * 
   * @param fullyQualifiedName a fully qualified name
   * @param suffix a suffix to add to the fully qualified name.May be
   *          <code>null</code>
   * @param extension the file extension to add at the end of the returned path.
   *          May be <code>null</code>
   * @return the absolute path corresponding to the given fully qualified name.
   */
  public static String fullyQualifiedNameToPath(
      final String fullyQualifiedName, final String suffix,
      final String extension) {
    String path = "/" + toValidName(fullyQualifiedName).replace('.', '/');
    if (suffix != null) {
      path += suffix;
    }
    if (extension != null) {
      if (!extension.startsWith(".")) path += ".";
      path += extension;
    }
    return path;
  }
}
