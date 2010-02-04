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
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind;

import static org.testng.Assert.assertEquals;

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.annotation.predefined.Compile;
import org.ow2.mind.adl.annotation.predefined.CompileDef;
import org.ow2.mind.adl.annotation.predefined.Run;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.unit.UnitTestDataProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FunctionalTest extends AbstractFunctionalTest {

  @DataProvider(name = "functional-test")
  protected Object[][] dataProvider() throws Exception {
    return UnitTestDataProvider.listADLs("functional");
  }

  @Test(dataProvider = "functional-test", groups = {"functional"})
  public void functionalTest(final String rootDir, final String adlName)
      throws Exception {
    initSourcePath(rootDir);
    final Definition d = runner.load(adlName);
    final CompileDef compileDefAnno = AnnotationHelper.getAnnotation(d,
        CompileDef.class);
    if (compileDefAnno != null) {
      runner.compileDefinition(adlName);
    }

    final Compile compileAnno = AnnotationHelper
        .getAnnotation(d, Compile.class);
    if (compileAnno != null) {
      if (compileAnno.addBootstrap) {
        runner.compile("GenericApplication<" + adlName + ">");
      } else {
        runner.compile(adlName);
      }
    }

    final Run runAnno = AnnotationHelper.getAnnotation(d, Run.class);
    if (runAnno != null) {
      final String adl;
      adl = (runAnno.addBootstrap)
          ? "GenericApplication<" + adlName + ">"
          : adlName;
      final int r = runner.compileAndRun(adl, runAnno.executableName,
          runAnno.params);
      assertEquals(r, runAnno.expectedResult, "Unexpected return value");
    }
  }
}
