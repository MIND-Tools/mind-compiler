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

import java.io.PrintStream;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.Error;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * This error manager log errors and warnings on {@link PrintStream} (
 * <code>System.err</code> by default).
 */
@Singleton
public class StreamErrorManager extends SimpleErrorManager {

  public static final String ERROR_STREAM_NAME      = "ErrorManager.errorStream";
  public static final String WARNING_STREAM_NAME    = "ErrorManager.warningStream";
  public static final String PRINT_STACK_TRACE_NAME = "ErrorManager.printStackTrace";

  @Inject
  @Named(ERROR_STREAM_NAME)
  protected PrintStream      errorStream;

  @Inject
  @Named(WARNING_STREAM_NAME)
  protected PrintStream      warningStream;

  @Inject
  @Named(PRINT_STACK_TRACE_NAME)
  protected boolean          printStackTraces;

  @Override
  public void logError(final Error error) throws ADLException {
    super.logError(error);
    print(errorStream, error);
  }

  @Override
  public void logWarning(final Error warning) {
    super.logWarning(warning);
    print(warningStream, warning);
  }

  protected void print(final PrintStream stream, final Error error) {
    if (printStackTraces) {
      try {
        throw new ADLException(error);
      } catch (final ADLException e) {
        e.printStackTrace(stream);
      }
    } else {
      stream.println(ErrorHelper.formatError(error));
    }
  }

  /**
   * @param errorStream the errorStream to set
   */
  public void setErrorStream(final PrintStream errorStream) {
    this.errorStream = errorStream;
  }

  /**
   * @param warningStream the warningStream to set
   */
  public void setWarningStream(final PrintStream warningStream) {
    this.warningStream = warningStream;
  }

  /**
   * @param printStackTraces the printStackTraces to set
   */
  public void setPrintStackTraces(final boolean printStackTraces) {
    this.printStackTraces = printStackTraces;
  }
}
