/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;

public enum TargetDescErrors implements ErrorTemplate {

  TARGET_DESC_NOT_FOUND_FATAL(0, "Target descriptor '%s' not found.", "<path>"),

  PARSE_ERROR_FATAL(1, "Parse error in target descriptor '%s': %s", "<path>",
      "<msg>"),

  CYCLE_FATAL(1, "Cycle in target descriptors: %s.", "cycle"),

  INVALID_NAME(2,
      "Invalid target descriptor name. Found '%s', where '%s' was expected.",
      "<found>", "<expected>"),

  INVALID_LINKER_SCRIPT(9, "Invalid Linker script path '%s'.", "<link-script>"),

  LINKER_SCRIPT_NOT_FOUND(10, "Linker script '%s' not found.", "<link-script>"),

  ;

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "TRG";

  private int                id;
  private String             format;

  private TargetDescErrors(final int id, final String format,
      final Object... args) {
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
