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
import java.io.PrintStream;
import java.util.Iterator;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;

/**
 * This error manager log errors and warnings on {@link PrintStream} (
 * <code>System.err</code> by default).
 */
public class StreamErrorManager extends AbstractErrorManager {

  protected PrintStream errorStream      = System.err;
  protected PrintStream warningStream    = System.err;
  protected boolean     printStackTraces = false;

  @Override
  protected void processError(final Error error) throws ADLException {
    print(errorStream, error);
  }

  @Override
  protected void processWarning(final Error warning) {
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
      ErrorLocator locator = error.getLocator();
      if (locator instanceof ChainedErrorLocator) {
        locator = ((ChainedErrorLocator) locator).getRootLocator();
        if (locator == null) {
          final Iterator<ErrorLocator> iter = ((ChainedErrorLocator) error
              .getLocator()).getChainedLocations().iterator();
          while (iter.hasNext() && locator == null) {
            locator = iter.next();
          }
        }
      }
      final String cwd = System.getProperty("user.dir") + File.separator;

      String fileLocation = null;

      if (locator != null && locator.getInputFilePath() != null) {
        fileLocation = locator.getInputFilePath();
        if (fileLocation.startsWith(cwd)) {
          fileLocation = fileLocation.substring(cwd.length());
        }
      }

      final StringBuffer sb = new StringBuffer();
      if (locator != null && fileLocation != null) {
        sb.append("At ").append(fileLocation);

        if (locator.getBeginLine() >= 0) {
          sb.append(":").append(locator.getBeginLine());
          if (locator.getBeginColumn() >= 0) {
            sb.append(",").append(locator.getBeginColumn());
          }
        }
        sb.append(":\n |--> ");
        if (locator.getBeginLine() >= 0) {
          final File inputFile = new File(locator.getInputFilePath());
          if (inputFile.exists()) {
            try {
              final LineNumberReader reader = new LineNumberReader(
                  new FileReader(inputFile));
              for (int i = 0; i < locator.getBeginLine() - 1; i++) {
                reader.readLine();
              }
              final String line = reader.readLine().replace("\t", "    ");
              sb.append("  ").append(line).append("\n |-->   ");
              if (locator.getBeginColumn() >= 0) {
                for (int i = 0; i < locator.getBeginColumn() - 1; i++) {
                  sb.append(" ");
                }
                int end = line.length();
                if (locator.getEndColumn() >= 0
                    && locator.getBeginLine() == locator.getEndLine()) {
                  end = locator.getEndColumn();
                }
                for (int i = locator.getBeginColumn(); i < end + 1; i++) {
                  sb.append("-");
                }
                sb.append("\n |--> ");

              }
            } catch (final IOException e1) {
              // ignore
            }
          }
        }
      }
      sb.append(error.getMessage()).append("\n");
      Throwable cause = error.getCause();
      while (cause != null) {
        sb.append("caused by : ");
        sb.append(cause.getMessage()).append('\n');
        cause = cause.getCause();
      }

      stream.println(sb);
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
