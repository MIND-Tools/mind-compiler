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

package org.ow2.mind.unit;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.ow2.mind.error.ErrorHelper;

public class ExpectedErrorHelper {

  protected static final Pattern SEMANTIC_ERROR_PATTERN = Pattern
                                                            .compile("//#\\s*SemanticError\\s+GroupId=(\\w+)\\s+ErrorId=(\\w+)\\s+line=(\\d+)");

  public static void checkErrors(final URL file, final List<Error> errors)
      throws Exception {
    final List<ExpectedError> expectedErrors = parserErrorLines(file);
    if (errors.isEmpty() && !expectedErrors.isEmpty()) {
      fail("Successful loading of erroneous " + file + ".");
    }

    for (final Error error : errors) {
      checkError(error, expectedErrors);
    }
    assertTrue(expectedErrors.isEmpty(), "Exepected errors not found : "
        + expectedErrors);
  }

  public static List<ExpectedError> parserErrorLines(final URL file)
      throws Exception {
    final List<ExpectedError> expectedErrors = new ArrayList<ExpectedError>();

    final LineNumberReader reader = new LineNumberReader(new InputStreamReader(
        file.openStream()));

    String line = reader.readLine();
    while (line != null && line.startsWith("//#")) {
      final Matcher matcher = SEMANTIC_ERROR_PATTERN.matcher(line);
      if (!matcher.matches())
        fail("Invalid SemanticError specification in " + file);
      final ExpectedError expectedError = new ExpectedError();
      expectedError.groupId = matcher.group(1);
      expectedError.errorId = matcher.group(2);
      expectedError.line = Integer.parseInt(matcher.group(3));
      expectedErrors.add(expectedError);

      line = reader.readLine();
    }
    return expectedErrors;
  }

  public static void checkError(final Error error,
      final List<ExpectedError> expectedErrors) {

    final ErrorLocator locator = error.getLocator();
    assertNotNull(locator);
    final int line = locator.getBeginLine();
    final String groupId = error.getTemplate().getGroupId();
    final String errorId = ((Enum<?>) error.getTemplate()).name();
    final Iterator<ExpectedError> iter = expectedErrors.iterator();
    while (iter.hasNext()) {
      final ExpectedError expectedError = iter.next();
      if (expectedError.line == line && expectedError.errorId.equals(errorId)
          && expectedError.groupId.equals(groupId)) {
        iter.remove();
        return;
      }
    }

    // not found
    fail("Unexpected error :  GroupId=" + groupId + " ErrorId=" + errorId
        + " line=" + line + "\n" + ErrorHelper.formatError(error));
  }

  public static final class ExpectedError {
    public String groupId;
    public String errorId;
    public int    line;

    @Override
    public String toString() {
      return "GroupId=" + groupId + " ErrorId=" + errorId + " line=" + line;
    }
  }
}
