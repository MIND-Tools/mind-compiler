/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.error;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import java.util.Collection;
import java.util.Collections;

import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorTemplate;

public class ErrorCollection extends Error {

  protected final Collection<Error> errors;

  public ErrorCollection(final Collection<Error> errors) {
    super(ErrorCollectionTemplate.ERROR_COLLECTION, errors);
    this.errors = errors;
  }

  /**
   * @return the errors
   */
  public Collection<Error> getErrors() {
    return errors;
  }

  private static enum ErrorCollectionTemplate implements ErrorTemplate {
    ERROR_COLLECTION;

    /** The groupId of ErrorTemplates defined in this enumeration. */
    public static final String GROUP_ID = "COLL";

    private int                id;

    private ErrorCollectionTemplate() {
      this.id = ordinal();

      assert validErrorTemplate(this, Collections.EMPTY_LIST);
    }

    public int getErrorId() {
      return id;
    }

    public String getGroupId() {
      return GROUP_ID;
    }

    public String getFormatedMessage(final Object... args) {
      if (args == null || args.length != 1)
        throw new IllegalArgumentException();

      final Collection<?> errors = (Collection<?>) args[0];
      String msg = "";
      for (final Object e : errors) {
        msg += ErrorHelper.formatError((Error) e);
      }

      return msg;
    }

    public String getFormat() {
      return "";
    }
  }
}
