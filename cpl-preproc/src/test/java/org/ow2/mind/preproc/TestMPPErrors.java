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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import org.objectweb.fractal.adl.ADLException;
import org.testng.annotations.Test;

public class TestMPPErrors extends AbstractTestMPP {

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    try {
      compileSingleton("error", "error1");
      fail("An ADLException was expected here");
    } catch (final ADLException e) {
      assertNotNull(e.getError());
      assertSame(e.getError().getTemplate(), MPPErrors.PARSE_ERROR);
      System.out.println(e.getError().toString());
    }
  }

  @Test(groups = {"functional"})
  public void testinitSingleton() throws Exception {
    try {
      compileSingleton("init", "init");
      fail("An ADLException was expected here");
    } catch (final ADLException e) {
      assertNotNull(e.getError());
      assertSame(e.getError().getTemplate(), MPPErrors.PARSE_ERROR);
      System.out.println(e.getError().toString());
    }
  }

  @Test(groups = {"functional"})
  public void testinitMulti() throws Exception {
    try {
      compileMulti("init", "init");
      fail("An ADLException was expected here");
    } catch (final ADLException e) {
      assertNotNull(e.getError());
      assertSame(e.getError().getTemplate(), MPPErrors.PARSE_ERROR);
      System.out.println(e.getError().toString());
    }
  }
}
