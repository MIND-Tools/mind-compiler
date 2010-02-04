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

package org.ow2.mind.adl;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.merger.MergeException;

/**
 * Exception thrown when a merge of two AST fails.
 */
public class InvalidMergeException extends MergeException {

  private final Error error;

  /**
   * Constructs a new {@link InvalidMergeException}.
   * 
   * @param template the error templates.
   * @param node the error location.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  public InvalidMergeException(final ErrorTemplate template, final Node node,
      final Object... args) {
    this(template, new NodeErrorLocator(node), null, args);
  }

  /**
   * Constructs a new {@link InvalidMergeException}.
   * 
   * @param template the error templates.
   * @param locator the error location. May be <code>null</code>.
   * @param cause the cause of this error. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  public InvalidMergeException(final ErrorTemplate template,
      final ErrorLocator locator, final Throwable cause, final Object... args) {
    this(new Error(template, locator, cause, args));
  }

  /**
   * Constructs a new {@link InvalidMergeException}.
   * 
   * @param error the error reported by this exception
   */
  public InvalidMergeException(final Error error) {
    super(error.getCause());
    this.error = error;
  }

  /**
   * Returns the {@link Error} object reported by this exception.
   * 
   * @return the {@link Error} object reported by this exception.
   */
  public Error getError() {
    return error;
  }

  @Override
  public String getMessage() {
    return error.toString();
  }

}
