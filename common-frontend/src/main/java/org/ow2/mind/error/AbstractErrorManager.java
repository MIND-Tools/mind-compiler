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
import java.util.List;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.error.NodeErrorLocator;

/**
 * Abstract implementation of {@link ErrorManager} interface. Every methods
 * implemented by this class calls {@link ErrorManager#logError(Error)} or
 * {@link ErrorManager#logWarning(Error)}.
 */
public abstract class AbstractErrorManager implements ErrorManager {

  protected final List<Error> errors   = new ArrayList<Error>();
  protected final List<Error> warnings = new ArrayList<Error>();

  protected abstract void processError(Error error) throws ADLException;

  protected abstract void processWarning(Error warning);

  public void logError(final Error error) throws ADLException {
    errors.add(error);
    if (error.getLocator() instanceof NodeErrorLocator) {
      ErrorASTHelper.addError(
          ((NodeErrorLocator) error.getLocator()).getNode(), error);
    }
    processError(error);
  }

  public void logWarning(final Error warning) {
    warnings.add(warning);
    if (warning.getLocator() instanceof NodeErrorLocator) {
      ErrorASTHelper.addError(
          ((NodeErrorLocator) warning.getLocator()).getNode(), warning);
    }
    processWarning(warning);
  }

  public void logError(final ErrorTemplate template, final Object... args)
      throws ADLException {
    logError(template, null, null, args);
  }

  public void logError(final ErrorTemplate template, final Node node,
      final Object... args) throws ADLException {
    logError(template, new NodeErrorLocator(node), null, args);
  }

  public void logError(final ErrorTemplate template,
      final ErrorLocator locator, final Object... args) throws ADLException {
    logError(template, locator, null, args);
  }

  public void logError(final ErrorTemplate template, final Throwable cause,
      final Object... args) throws ADLException {
    logError(template, null, cause, args);
  }

  public void logError(final ErrorTemplate template,
      final ErrorLocator locator, final Throwable cause, final Object... args)
      throws ADLException {
    logError(new Error(template, locator, cause, args));
  }

  public void logWarning(final ErrorTemplate template, final Object... args) {
    logWarning(template, null, null, args);
  }

  public void logWarning(final ErrorTemplate template, final Node node,
      final Object... args) {
    logWarning(template, new NodeErrorLocator(node), null, args);
  }

  public void logWarning(final ErrorTemplate template,
      final ErrorLocator locator, final Object... args) {
    logWarning(template, locator, null, args);
  }

  public void logWarning(final ErrorTemplate template, final Throwable cause,
      final Object... args) {
    logWarning(template, null, cause, args);
  }

  public void logWarning(final ErrorTemplate template,
      final ErrorLocator locator, final Throwable cause, final Object... args) {
    logWarning(new Error(template, locator, cause, args));
  }

  public List<Error> getErrors() {
    return new ArrayList<Error>(errors);
  }

  public List<Error> getWarnings() {
    return new ArrayList<Error>(warnings);
  }

  public void clear() {
    errors.clear();
    warnings.clear();
  }
}
