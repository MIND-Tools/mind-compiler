/**
 * Cecilia ADL Parser
 * Copyright (C) 2008 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: fractal@objectweb.org
 *
 * Author: Matthieu Leclercq
 */

package org.ow2.mind.compilation;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;

/** {@link ErrorTemplate} group for the compiler package. */
public enum CompilerErrors implements ErrorTemplate {

  /** */
  EXECUTION_ERROR(
      "Unable to execute \"%s\" command. Checks that the command exist and is in the path",
      "command"),

  /** */
  COMPILER_ERROR("Error while compiling the file \"%s\"", "filename"),

  /** */
  LINKER_ERROR("Error while linking the file \"%s\"", "filename"),

  /** */
  ARCHIVER_ERROR("Error while creating archive the file \"%s\"", "filename");

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "CPI";

  private int                id;
  private String             format;

  private CompilerErrors(final String format, final Object... args) {
    this.id = ordinal();
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
