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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.util.FractalADLLogManager;

/**
 * Simple implementation of {@link ErrorManager} interface. Every methods
 * implemented by this class calls {@link ErrorManager#logError(Error)} or
 * {@link ErrorManager#logWarning(Error)}.
 */
public class SimpleErrorManager implements ErrorManager {

  protected static Logger     logger   = FractalADLLogManager
                                           .getLogger("error");

  protected final List<Error> errors   = new ArrayList<Error>();
  protected final List<Error> warnings = new ArrayList<Error>();

  public void logError(final Error error) throws ADLException {
    errors.add(error);
    if (error.getLocator() instanceof NodeErrorLocator) {
      ErrorHelper.addError(((NodeErrorLocator) error.getLocator()).getNode(),
          error);
    }
    if (logger.isLoggable(Level.FINER)) {
      String errorId;
      if (error.getTemplate() instanceof Enum<?>) {
        errorId = ((Enum<?>) error.getTemplate()).name();
      } else {
        errorId = Integer.toString(error.getTemplate().getErrorId());
      }
      logger.finer("Error " + error.getTemplate().getGroupId() + ":" + errorId);
      logger.finer(ErrorHelper.formatError(error));

      if (error.getLocator().getInputFilePath() != null
          && logger.isLoggable(Level.FINEST)) {
        final File inputFile = new File(error.getLocator().getInputFilePath());
        if (inputFile.exists()) {
          logger.finest("Input File :");
          LineNumberReader reader = null;
          try {
            reader = new LineNumberReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
              logger.finest(line);
            }
          } catch (final IOException e1) {
            // ignore
          } finally {
            if (reader != null) {
              try {
                reader.close();
              } catch (final IOException e) {
                // ignore
              }
            }
          }
        }
      }

      logger.finer("Stack trace : ");
      // capture the current stack-trace
      StackTraceElement[] stackTrace;
      try {
        throw new ADLException(error);
      } catch (final ADLException e) {
        stackTrace = e.getStackTrace();
      }
      // pass traces with "logError" method names
      int i = 0;
      for (i = 0; i < stackTrace.length; i++) {
        if (!stackTrace[i].getMethodName().equals("logError")) break;
      }
      for (; i < stackTrace.length; i++) {
        logger.finer("  " + stackTrace[i].toString());
      }
    }
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

  public void logFatal(final Error error) throws ADLException {
    logError(error);
    throw new ADLException(error);
  }

  public void logFatal(final ErrorTemplate template, final Object... args)
      throws ADLException {
    logFatal(template, null, null, args);
  }

  public void logFatal(final ErrorTemplate template, final Node node,
      final Object... args) throws ADLException {
    logFatal(template, new NodeErrorLocator(node), null, args);
  }

  public void logFatal(final ErrorTemplate template,
      final ErrorLocator locator, final Object... args) throws ADLException {
    logFatal(template, locator, null, args);
  }

  public void logFatal(final ErrorTemplate template, final Throwable cause,
      final Object... args) throws ADLException {
    logFatal(template, null, cause, args);
  }

  public void logFatal(final ErrorTemplate template,
      final ErrorLocator locator, final Throwable cause, final Object... args)
      throws ADLException {
    logFatal(new Error(template, locator, cause, args));
  }

  public void logWarning(final Error warning) {
    warnings.add(warning);
    if (warning.getLocator() instanceof NodeErrorLocator) {
      ErrorHelper.addError(((NodeErrorLocator) warning.getLocator()).getNode(),
          warning);
    }
    if (logger.isLoggable(Level.FINER)) {
      logger.fine(ErrorHelper.formatError(warning));

      logger.fine("Stack trace : ");
      // capture the current stack-trace
      StackTraceElement[] stackTrace;
      try {
        throw new ADLException(warning);
      } catch (final ADLException e) {
        stackTrace = e.getStackTrace();
      }
      // pass traces with "logError" method names
      int i = 0;
      for (i = 0; i < stackTrace.length; i++) {
        if (!stackTrace[i].getMethodName().equals("logWarning")) break;
      }
      for (; i < stackTrace.length; i++) {
        logger.fine("  " + stackTrace[i].toString());
      }
    }
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
