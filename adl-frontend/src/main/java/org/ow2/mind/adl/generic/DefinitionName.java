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

package org.ow2.mind.adl.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class DefinitionName {

  private static final Pattern                  TYPE_NAME_PATTERN = Pattern
                                                                      .compile("(([a-zA-Z_][a-zA-Z_0-9]*)(\\.([a-zA-Z_][a-zA-Z_0-9]*))*)|\\?");
  private static final Pattern                  NAME_PATTERN      = Pattern
                                                                      .compile("([a-zA-Z_][a-zA-Z_0-9]*)");

  static final char                             INF               = '<';
  static final char                             SUP               = '>';
  static final char                             COMMA             = ',';
  static final char                             DASH              = ':';

  private static final DefinitionNameArgument[] EMPTY_DEF_NAME    = new DefinitionNameArgument[0];

  final String                                  name;
  List<DefinitionNameArgument>                  typeArguments;

  public static DefinitionName fromString(final String name) {
    final int length = name.length();
    int i = 0;
    while (i < length && name.charAt(i) != INF) {
      i++;
    }

    if (i == length) {
      return new DefinitionName(name);
    } else {
      final DefinitionName defName = new DefinitionName(name.substring(0, i));
      i = parseNameList(defName, name, i);
      if (i != length)
        throw new IllegalArgumentException("Invalid definition name \"" + name
            + "\".");
      return defName;
    }
  }

  private static int parseNameList(final DefinitionName defName,
      final String name, final int startIndex) {
    assert startIndex > 0;
    assert name.charAt(startIndex) == INF;

    final int length = name.length();

    if (startIndex == length)
      throw new IllegalArgumentException("Invalid definition name \"" + name
          + "\".");

    int i = startIndex;
    do {
      i = parseName(defName, name, i + 1);
      if (i == length)
        throw new IllegalArgumentException("Invalid definition name \"" + name
            + "\".");
      assert name.charAt(i) == SUP || name.charAt(i) == COMMA;

    } while (name.charAt(i) != SUP);
    return i + 1;
  }

  private static int parseName(final DefinitionName parent, final String name,
      final int startIndex) {
    final int length = name.length();

    int i = startIndex;
    int dashIndex = -1;
    char c = 0;
    while ((i < length) && (c = name.charAt(i)) != INF && c != SUP
        && c != COMMA) {
      if (c == DASH) dashIndex = i;
      i++;
    }

    if (i == length)
      throw new IllegalArgumentException("Invalid definition name \"" + name
          + "\".");

    final DefinitionName defName;
    if (dashIndex == -1) {
      defName = new DefinitionName(name.substring(startIndex, i));
      parent.addTypeArgument(null, defName);
    } else {
      defName = new DefinitionName(name.substring(dashIndex + 1, i));
      parent.addTypeArgument(name.substring(startIndex, dashIndex), defName);
    }

    return c == INF ? parseNameList(defName, name, i) : i;
  }

  public DefinitionName(final String name) {
    if (name == null) throw new IllegalArgumentException("name can't be null");
    if (!TYPE_NAME_PATTERN.matcher(name).matches())
      throw new IllegalArgumentException("\"" + name
          + "\" does not match pattern " + TYPE_NAME_PATTERN.pattern());

    this.name = name;
    this.typeArguments = null;
  }

  public void addTypeArgument(final String argName,
      final DefinitionName typeArgument) {
    if (argName != null && !NAME_PATTERN.matcher(argName).matches())
      throw new IllegalArgumentException("\"" + argName
          + "\" does not match pattern " + NAME_PATTERN.pattern());

    if (typeArguments == null)
      typeArguments = new ArrayList<DefinitionNameArgument>();
    typeArguments.add(new DefinitionNameArgument(argName, typeArgument));
  }

  public String getName() {
    return name;
  }

  public DefinitionNameArgument[] getTypeArguments() {
    if (typeArguments == null || typeArguments.size() == 0)
      return EMPTY_DEF_NAME;
    else
      return typeArguments.toArray(new DefinitionNameArgument[typeArguments
          .size()]);
  }

  @Override
  public String toString() {
    if (typeArguments == null || typeArguments.size() == 0) return name;

    final StringBuilder sb = new StringBuilder();
    toString(sb);
    return sb.toString();
  }

  private void toString(final StringBuilder sb) {
    sb.append(name);

    if (typeArguments != null && typeArguments.size() > 0) {
      sb.append(INF);
      final Iterator<DefinitionNameArgument> i = typeArguments.iterator();
      while (i.hasNext()) {
        final DefinitionNameArgument typeArg = i.next();
        if (typeArg.name != null) sb.append(typeArg.name).append(':');
        typeArg.value.toString(sb);
        if (i.hasNext()) sb.append(COMMA);
      }
      sb.append(SUP);
    }
  }

  @Override
  public int hashCode() {
    int hashCode = name.hashCode();

    if (typeArguments != null) {
      for (final DefinitionNameArgument templateValue : typeArguments) {
        hashCode = 13 * hashCode + templateValue.hashCode();
      }
    }

    return hashCode;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DefinitionName)) return false;

    final DefinitionName other = (DefinitionName) obj;
    if (!name.equals(other.name)) return false;

    if (typeArguments != null && typeArguments.size() > 0) {
      if (other.typeArguments == null
          || other.typeArguments.size() != typeArguments.size()) return false;

      for (int i = 0; i < typeArguments.size(); i++) {
        if (!typeArguments.get(i).equals(other.typeArguments.get(i)))
          return false;
      }

      return true;
    } else {
      return other.typeArguments == null || other.typeArguments.size() == 0;
    }
  }

  public static final class DefinitionNameArgument {
    private final String         name;
    private final DefinitionName value;

    DefinitionNameArgument(final String name, final DefinitionName value) {
      if (value == null)
        throw new IllegalArgumentException("value can't be null");

      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public DefinitionName getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      int hash = value.hashCode();
      if (name != null) hash = 13 * hash + name.hashCode();

      return hash;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) return true;

      if (!(obj instanceof DefinitionNameArgument)) return false;

      final DefinitionNameArgument arg = (DefinitionNameArgument) obj;
      if (name != null) {
        if (arg.name == null || !name.equals(arg.name)) return false;
      }

      return value.equals(arg.value);
    }
  }
}