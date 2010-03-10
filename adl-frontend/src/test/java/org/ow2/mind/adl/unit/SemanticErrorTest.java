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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
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
    final ExpectedError expectedError = parserFirstLine(adlName);
    try {
      loader.load(adlName, context);
      if (expectedError != null)
        fail("Successful loading of erroneous " + adlName + " ADL.");
    } catch (final ADLException e) {
      final Error error = e.getError();
      if (error == null) {
        throw new AssertionFailedError(
            "Loader returned an exception without error.");
      }
      ErrorLocator locator = error.getLocator();
      while (locator instanceof ChainedErrorLocator) {
        locator = ((ChainedErrorLocator) locator).getRootLocator();
      }

      assertNotNull(locator, "Caught ADLException have no locator");
      assertEquals(((Enum<?>) error.getTemplate()).name(),
          expectedError.errorId, "Unexpected errorId of caught ADLException");
      assertEquals(error.getTemplate().getGroupId(), expectedError.groupId,
          "Unexpected groupId of caught ADLException");
      assertEquals(locator.getBeginLine(), expectedError.line,
          "Unexpected line of caught ADLException");
    }
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

  protected ExpectedError parserFirstLine(final String adlName)
      throws Exception {
    final String line = readFirstLine(adlName);
    if (!line.startsWith("//#")) return null;
    final Matcher matcher = SEMANTIC_ERROR_PATTERN.matcher(line);
    if (!matcher.matches())
      throw new Exception("Invalid SemanticError specification in ADL "
          + adlName);
    final ExpectedError expectedError = new ExpectedError();
    expectedError.groupId = matcher.group(1);
    expectedError.errorId = matcher.group(2);
    expectedError.line = Integer.parseInt(matcher.group(3));
    return expectedError;
  }

  protected static class ExpectedError {
    public String groupId;
    public String errorId;
    public int    line;
  }
}
