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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;

public final class ErrorHelper {
  private ErrorHelper() {
  }

  public static String formatError(final Error error) {
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
    final File cwdFile = new File(cwd);

    String fileLocation = null;
    File inputFile = null;

    if (locator != null && locator.getInputFilePath() != null) {

      // Convert paths to handle different operating systems, conventions, and
      // special characters (such as space ' ', plus '+', etc)
      try {
        fileLocation = URLDecoder.decode(locator.getInputFilePath(), "UTF-8");
      } catch (final UnsupportedEncodingException e) {
        fileLocation = locator.getInputFilePath();
      }

      inputFile = new File(fileLocation);

      if (inputFile.getPath().startsWith(cwdFile.getPath())) {
        fileLocation = fileLocation.substring(cwd.length());
      }
    }

    final StringBuilder sb = new StringBuilder();
    if (locator != null && fileLocation != null && inputFile != null) {
      sb.append("At ").append(fileLocation);

      if (locator.getBeginLine() >= 0) {
        sb.append(":").append(locator.getBeginLine());
        if (locator.getBeginColumn() >= 0) {
          sb.append(",").append(locator.getBeginColumn());
        }
      }
      sb.append(":\n |--> ");
      if (locator.getBeginLine() >= 0) {
        if (inputFile.exists()) {
          try {
            final FileReader fileReader = new FileReader(inputFile);
            final LineNumberReader lineNumberReader = new LineNumberReader(
                fileReader);
            for (int i = 0; i < locator.getBeginLine() - 1; i++) {
              lineNumberReader.readLine();
            }
            final String line = lineNumberReader.readLine().replace("\t",
                "    ");
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
            lineNumberReader.close();
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
    return sb.toString();
  }

  public static final String ERROR_DECORATION_NAME   = "error-list";
  public static final String WARNING_DECORATION_NAME = "warning-list";

  // This class is use to avoid serialization of Error/Warning list (see
// MIND-119)
  private static final class NonSerializableErrorList {
    final List<Error> list = new ArrayList<Error>();
  }

  public static void addError(final Node node, final Error error) {
    getErrorListDecoration(node).add(error);
  }

  public static List<Error> getErrors(final Node node) {
    if (node.astGetDecoration(ERROR_DECORATION_NAME) == null) {
      return Collections.emptyList();
    }
    return new ArrayList<Error>(getErrorListDecoration(node));
  }

  private static List<Error> getErrorListDecoration(final Node node) {
    NonSerializableErrorList errors = (NonSerializableErrorList) node
        .astGetDecoration(ERROR_DECORATION_NAME);
    if (errors == null) {
      errors = new NonSerializableErrorList();
      node.astSetDecoration(ERROR_DECORATION_NAME, errors);
    }
    return errors.list;
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

  private static List<Error> getWarningListDecoration(final Node node) {
    NonSerializableErrorList warnings = (NonSerializableErrorList) node
        .astGetDecoration(WARNING_DECORATION_NAME);
    if (warnings == null) {
      warnings = new NonSerializableErrorList();
      node.astSetDecoration(WARNING_DECORATION_NAME, warnings);
    }
    return warnings.list;
  }

}
