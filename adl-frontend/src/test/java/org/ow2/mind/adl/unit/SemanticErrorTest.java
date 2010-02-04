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

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import junit.framework.AssertionFailedError;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.unit.UnitTestDataProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SemanticErrorTest extends AbstractADLLoaderTest {

  @DataProvider(name = "unit-test")
  protected Object[][] dataProvider() throws Exception {
    return UnitTestDataProvider.listADLs("unit/error/semantic");
  }

  @Test(dataProvider = "unit-test", groups = {"functional"})
  public void semanticErrorTest(final String rootDir, final String adlName)
      throws Exception {
    initSourcePath(rootDir);
    try {
      loader.load(adlName, context);
    } catch (final ADLException e) {
      final Error error = e.getError();
      if (error == null) {
        throw new AssertionFailedError(
            "Loader returned an exception without error.");
      }
      ErrorLocator locator = error.getLocator();
      if (locator instanceof ChainedErrorLocator) {
        locator = ((ChainedErrorLocator) locator).getRootLocator();
      }

      Node node = null;
      if (locator == null) {
        throw new AssertionFailedError(
            "Loader returned an error without locator. The error message was \n"
                + error.getMessage());
      }
      if (locator instanceof BasicErrorLocator) {
        throw new AssertionFailedError(
            "There is no node assotiated to the raised error. Received error message is \n"
                + error.getMessage());
      } else {

        node = ((NodeErrorLocator) locator).getNode();
        if (node == null) {
          throw new AssertionFailedError(
              "The node causing the error doesn't contain an error locator.\n The expected error was \n"
                  + e.getError());
        }
      }
      final SemanticError annotation = AnnotationHelper.getAnnotation(node,
          SemanticError.class);
      if (annotation == null) {
        throw new AssertionFailedError(
            "The node causing the error doesn't contain an annotation specifying the expected error.\n The expected error was \n"
                + e.getError());
      }

      assertEquals(annotation.ErrorId, ((Enum) error.getTemplate()).name());
      assertEquals(annotation.GroupId, error.getTemplate().getGroupId());
      return;
    }
    fail("Successful loading of erroneous adl file.");
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

}
