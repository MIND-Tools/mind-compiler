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

package org.ow2.mind.preproc;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;

/**
 * {@link ErrorTemplate} for MIND pre-processor.
 */
public enum MPPErrors implements ErrorTemplate {

  /** */
  INPUT_FILE_NOT_FOUND(0, "Can't open input file %s", "<input file>"),

  /** */
  PARSE_ERROR(1, "Parse error %s %s", "<input file>", "<parse error>"), ;

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "MPP";

  private int                id;
  private String             format;

  private MPPErrors(final int id, final String format, final Object... args) {
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
