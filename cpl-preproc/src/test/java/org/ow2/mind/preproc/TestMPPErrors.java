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

package org.ow2.mind.preproc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.List;

import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.error.ErrorHelper;
import org.testng.annotations.Test;

public class TestMPPErrors extends AbstractTestMPP {

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    errorManager.clear();
    compileSingleton("error", "error1");
    final List<Error> errors = errorManager.getErrors();
    assertEquals(errors.size(), 1);
    final Error error = errors.get(0);
    assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
    System.out.println(ErrorHelper.formatError(error));
  }

  @Test(groups = {"functional"})
  public void testinitSingleton() throws Exception {
    errorManager.clear();
    compileSingleton("init", "init");
    final List<Error> errors = errorManager.getErrors();
    assertEquals(errors.size(), 1);
    final Error error = errors.get(0);
    assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
    System.out.println(ErrorHelper.formatError(error));
  }

  @Test(groups = {"functional"})
  public void testinitMulti() throws Exception {
    errorManager.clear();
    compileMulti("init", "init");
    final List<Error> errors = errorManager.getErrors();
    assertEquals(errors.size(), 1);
    final Error error = errors.get(0);
    assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
    System.out.println(ErrorHelper.formatError(error));
  }
}
