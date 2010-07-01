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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.adl.unit;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorHelper;
import org.ow2.mind.unit.UnitTestDataProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SemanticErrorTest extends AbstractADLLoaderTest {

  protected static final Pattern SEMANTIC_ERROR_PATTERN = Pattern
                                                            .compile("//#\\s*SemanticError\\s+GroupId=(\\w+)\\s+ErrorId=(\\w+)\\s+line=(\\d+)");

  @DataProvider(name = "unit-test")
  protected Object[][] dataProvider() throws Exception {
    return UnitTestDataProvider.listADLs("unit/error/semantic");
  }

  @Test(dataProvider = "unit-test", groups = {"functional"})
  public void semanticErrorTest(final String rootDir, final String adlName)
      throws Exception {
    initSourcePath(rootDir);
    final List<ExpectedError> expectedErrors = parserErrorLines(adlName);
    try {
      loader.load(adlName, context);
      if (!expectedErrors.isEmpty())
        fail("Successful loading of erroneous " + adlName + " ADL.");

      final List<Error> errors = errorManager.getErrors();
      for (final Error error : errors) {
        checkError(error, expectedErrors);
      }
    } catch (final ADLException e) {
      final Error error = e.getError();
      if (error == null) {
        fail("Loader returned an exception without error.");
      }
      if (error instanceof ErrorCollection) {
        for (final Error err1 : ((ErrorCollection) error).getErrors()) {
          checkError(err1, expectedErrors);
        }
      } else {
        checkError(error, expectedErrors);
      }
    }
    assertTrue(expectedErrors.isEmpty(), "Test do not raise expected errors : "
        + expectedErrors);
  }

  protected void checkError(final Error error,
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
    fail("Unexpected error : " + ErrorHelper.formatError(error));
  }

  @DataProvider(name = "unsupported-unit-test")
  protected Object[][] unsupportedDataProvider() throws Exception {
    return UnitTestDataProvider.listADLs("unit/unsupported/error/semantic");
  }

  @Test(dataProvider = "unsupported-unit-test", groups = {"unsupported"})
  public void unsupportedSemanticErrorTest(final String rootDir,
      final String adlName) throws Exception {
    semanticErrorTest(rootDir, adlName);
  }

  protected List<ExpectedError> parserErrorLines(final String adlName)
      throws Exception {
    final List<ExpectedError> expectedErrors = new ArrayList<ExpectedError>();

    final LineNumberReader reader = readADL(adlName);

    String line = reader.readLine();
    while (line != null && line.startsWith("//#")) {
      final Matcher matcher = SEMANTIC_ERROR_PATTERN.matcher(line);
      if (!matcher.matches())
        throw new Exception("Invalid SemanticError specification in ADL "
            + adlName);
      final ExpectedError expectedError = new ExpectedError();
      expectedError.groupId = matcher.group(1);
      expectedError.errorId = matcher.group(2);
      expectedError.line = Integer.parseInt(matcher.group(3));
      expectedErrors.add(expectedError);

      line = reader.readLine();
    }
    return expectedErrors;
  }

  protected static class ExpectedError {
    public String groupId;
    public String errorId;
    public int    line;

    @Override
    public String toString() {
      return groupId + ":" + errorId + "@" + line;
    }
  }
}
