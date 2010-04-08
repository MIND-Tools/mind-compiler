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

import static org.ow2.mind.NameHelper.toValidName;

import java.io.File;

import org.antlr.stringtemplate.AttributeRenderer;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.PathHelper;

public class BackendFormatRenderer implements AttributeRenderer {

  public static final String TO_UPPER             = "toUpper";
  public static final String TO_C_NAME            = "toCName";
  public static final String TO_UPPER_C_NAME      = "toUpperCName";
  public static final String NAME_TO_PATH         = "nameToPath";
  public static final String PATH_TO_C_NAME       = "pathToCName";
  public static final String PATH_TO_UPPER_C_NAME = "pathToUpperCName";
  public static final String TO_C_PATH            = "toCPath";
  public static final String SOURCE_TO_LINE       = "sourceTo#line";

  public String toString(final Object o) {
    return o.toString();
  }

  public String toString(final Object o, final String formatName) {
    if (TO_UPPER.equals(formatName)) {
      return o.toString().toUpperCase();
    } else if (TO_C_NAME.equals(formatName)) {
      return toCName(o.toString());
    } else if (TO_UPPER_C_NAME.equals(formatName)) {
      return toUpperCName(o.toString());
    } else if (NAME_TO_PATH.equals(formatName)) {
      return nameToPath(o.toString());
    } else if (PATH_TO_C_NAME.equals(formatName)) {
      return pathToCName(o.toString());
    } else if (PATH_TO_UPPER_C_NAME.equals(formatName)) {
      return pathToUpperCName(o.toString());
    } else if (TO_C_PATH.equals(formatName)) {
      return toCPath(o.toString());
    } else if (SOURCE_TO_LINE.equals(formatName)) {
      return sourceToLine(o.toString());
    }
    return o.toString();
  }

  public static String toCName(final String s) {
    return toValidName(s).replace('.', '_');
  }

  public static String toUpperCName(final String s) {
    return toCName(s).toUpperCase();
  }

  public static String nameToPath(final String s) {
    return toValidName(s).replace('.', File.separatorChar);
  }

  public static String pathToCName(final String s) {
    return s.replace('/', '_').replace('.', '_');
  }

  public static String pathToUpperCName(final String s) {
    return pathToCName(s).toUpperCase();
  }

  public static String toCPath(final String s) {
    if (PathHelper.isRelative(s))
      return "./" + s;
    else
      return s.substring(1);
  }

  public static String sourceToLine(final String s) {
    if (s == null) return "";

    final int i = s.lastIndexOf(':');
    if (i == -1) return "";
    final String inputFilePath = s.substring(0, i);
    final int j = s.indexOf('-', i);
    int lineNumber;
    if (j == -1) {
      try {
        lineNumber = Integer.parseInt(s.substring(i + 1));
      } catch (final NumberFormatException e) {
        return "";
      }
    } else {
      try {
        lineNumber = Integer.parseInt(s.substring(i + 1, j));
      } catch (final NumberFormatException e) {
        return "";
      }
    }
    return "#line " + lineNumber + " \"" + inputFilePath + "\"";
  }

  public static String sourceToLine(final Node n) {
    return sourceToLine(n.astGetSource());
  }
}
