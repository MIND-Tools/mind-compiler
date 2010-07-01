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

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.unit.UnitTestDataProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OKTest extends AbstractADLLoaderTest {

  @DataProvider(name = "unit-test")
  protected Object[][] dataProvider() throws Exception {
    return UnitTestDataProvider.listADLs("unit/ok");
  }

  @Test(dataProvider = "unit-test", groups = {"functional"})
  public void okTest(final String rootDir, final String adlName)
      throws Exception {
    initSourcePath(rootDir);
    final Definition d = loader.load(adlName, context);
    assertTrue(errorManager.getErrors().isEmpty());
    assertTrue(errorManager.getWarnings().isEmpty());
    assertNotNull(d);
  }
}
