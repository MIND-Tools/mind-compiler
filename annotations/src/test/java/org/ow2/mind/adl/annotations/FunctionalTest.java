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

package org.ow2.mind.adl.annotations;

import java.io.File;

import org.testng.annotations.Test;

public class FunctionalTest extends org.ow2.mind.FunctionalTest {

  protected String DEFAULT_TEST_DEPS = "../fractal-runtime/src/main/resources"
                                         + File.pathSeparator
                                         + "../adl-backend/src/test/resources/common";

  @Override
  @Test(dataProvider = "functional-test", groups = {"functional"})
  public void functionalTest(final String rootDir, final String adlName)
      throws Exception {
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        getDepsDir("common/ApplicationType.adl").getAbsolutePath() + "/common",
        rootDir);
    processFunctionanTest(adlName);
  }
}
