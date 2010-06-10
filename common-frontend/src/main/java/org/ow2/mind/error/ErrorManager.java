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

import java.util.List;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.ErrorTemplate;

/**
 * The ErrorManager interface is used to log errors and warnings detected on
 * input files.
 */
public interface ErrorManager {

  String ITF_NAME = "error-manager";

  /**
   * Log an error.
   * 
   * @param template the error templates.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final ErrorTemplate template, final Object... args)
      throws ADLException;

  /**
   * Log an error.
   * 
   * @param template the error templates.
   * @param node the error location.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final ErrorTemplate template, final Node node,
      final Object... args) throws ADLException;

  /**
   * Log an error.
   * 
   * @param template the error templates.
   * @param locator the error location. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final ErrorTemplate template, final ErrorLocator locator,
      final Object... args) throws ADLException;

  /**
   * Log an error.
   * 
   * @param template the error templates.
   * @param cause the cause of this error. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final ErrorTemplate template, final Throwable cause,
      final Object... args) throws ADLException;

  /**
   * Log an error.
   * 
   * @param template the error templates.
   * @param locator the error location. May be <code>null</code>.
   * @param cause the cause of this error. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final ErrorTemplate template, final ErrorLocator locator,
      final Throwable cause, final Object... args) throws ADLException;

  /**
   * Log an error.
   * 
   * @param error the reported error
   * @throws ADLException the ErrorManager may decide to throw an
   *           {@link ADLException} that contains the given error to interrupt
   *           the execution of the front-end.
   */
  void logError(final Error error) throws ADLException;

  /**
   * Log a warning.
   * 
   * @param template the error templates.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  void logWarning(final ErrorTemplate template, final Object... args);

  /**
   * Log a warning.
   * 
   * @param template the error templates.
   * @param node the error location.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  void logWarning(final ErrorTemplate template, final Node node,
      final Object... args);

  /**
   * Log a warning.
   * 
   * @param template the error templates.
   * @param locator the error location. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  void logWarning(final ErrorTemplate template, final ErrorLocator locator,
      final Object... args);

  /**
   * Log a warning.
   * 
   * @param template the error templates.
   * @param cause the cause of this error. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  void logWarning(final ErrorTemplate template, final Throwable cause,
      final Object... args);

  /**
   * Log a warning.
   * 
   * @param template the error templates.
   * @param locator the error location. May be <code>null</code>.
   * @param cause the cause of this error. May be <code>null</code>.
   * @param args the arguments for the formated message (see
   *          {@link ErrorTemplate#getFormatedMessage(Object...)}).
   */
  void logWarning(final ErrorTemplate template, final ErrorLocator locator,
      final Throwable cause, final Object... args);

  /**
   * Log a warning.
   * 
   * @param warning the reported warning
   */
  void logWarning(final Error warning);

  /**
   * Returns every logged errors.
   * 
   * @return every logged errors. The list may be empty.
   */
  List<Error> getErrors();

  /**
   * Returns every logged warnings.
   * 
   * @return every logged warnings. The list may be empty.
   */
  List<Error> getWarnings();

  /**
   * Clear lists of warnings and errors.
   */
  void clear();

}
