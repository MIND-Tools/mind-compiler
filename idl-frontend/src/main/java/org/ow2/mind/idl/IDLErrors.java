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

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;

/**
 * {@link ErrorTemplate} for IDL.
 */
public enum IDLErrors implements ErrorTemplate {

  /** */
  IDL_NOT_FOUND(0, "IDL not found \"%s\"", "<idlname>"),

  /** */
  UNEXPECTED_ITF_NAME(1,
      "Invalid interface name. Found \"%s\", where \"%s\" was expected.",
      "<found>", "<expected>"),

  /** */
  INVALID_INCLUDE(2, "Invalid include path \"%s\"", "<includedPath>"),

  /** */
  PARSE_ERROR(3, "Parse error: %s", "<detail>"),

  /** */
  IO_ERROR(4, "Can't read file \"%s\"", "<filename>"),

  /** */
  TYPE_REDEFINITION(5,
      "Redefinition of type \"%s\" previously defined at \"%s\"", "<typename>",
      "<location>"),

  /** */
  UNDEFINED_TYPE(6, "Undefined type \"%s\"", "<typename>"),

  ;

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "MIDL";

  private int                id;
  private String             format;

  private IDLErrors(final int id, final String format, final Object... args) {
    this.id = id;
    this.format = format;

    assert validErrorTemplate(this, args);
  }

  public int getErrorId() {
    return id;
  }

  public String getGroupId() {
    return GROUP_ID;
  }

  public String getFormatedMessage(final Object... args) {
    return String.format(format, args);
  }

  public String getFormat() {
    return format;
  }

}
