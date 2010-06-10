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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.Error;

public final class ErrorASTHelper {
  private ErrorASTHelper() {
  }

  public static final String ERROR_DECORATION_NAME   = "error-list";
  public static final String WARNING_DECORATION_NAME = "warning-list";

  public static void addError(final Node node, final Error error) {
    getErrorListDecoration(node).add(error);
  }

  public static List<Error> getErrors(final Node node) {
    if (node.astGetDecoration(ERROR_DECORATION_NAME) == null) {
      return Collections.emptyList();
    }
    return new ArrayList<Error>(getErrorListDecoration(node));
  }

  @SuppressWarnings("unchecked")
  private static List<Error> getErrorListDecoration(final Node node) {
    List<Error> errors = (List<Error>) node
        .astGetDecoration(ERROR_DECORATION_NAME);
    if (errors == null) {
      errors = new ArrayList<Error>();
      node.astSetDecoration(ERROR_DECORATION_NAME, errors);
    }
    return errors;
  }

  public static void addWarning(final Node node, final Error warning) {
    getWarningListDecoration(node).add(warning);
  }

  public static List<Error> getWarnings(final Node node) {
    if (node.astGetDecoration(WARNING_DECORATION_NAME) == null) {
      return Collections.emptyList();
    }
    return new ArrayList<Error>(getWarningListDecoration(node));
  }

  @SuppressWarnings("unchecked")
  private static List<Error> getWarningListDecoration(final Node node) {
    List<Error> warnings = (List<Error>) node
        .astGetDecoration(WARNING_DECORATION_NAME);
    if (warnings == null) {
      warnings = new ArrayList<Error>();
      node.astSetDecoration(WARNING_DECORATION_NAME, warnings);
    }
    return warnings;
  }

}
