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
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorHelper;
import org.testng.annotations.Test;

public class TestMPPErrors extends AbstractTestMPP {

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    try {
      compileSingleton("error", "error1");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 1);
      final Error error = errors.iterator().next();
      assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
      assertEquals(error.getLocator().getBeginLine(), 2);
      assertTrue(error.getLocator().getInputFilePath()
          .endsWith("error/error1.c"));
      System.out.println(ErrorHelper.formatError(error));
    }
  }

  @Test(groups = {"functional"})
  public void testinitSingleton() throws Exception {
    try {
      compileSingleton("init", "init");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 1);
      final Error error = errors.iterator().next();
      assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
      assertEquals(error.getLocator().getBeginLine(), 6);
      assertTrue(error.getLocator().getInputFilePath().endsWith("init/data.h"));
      System.out.println(ErrorHelper.formatError(error));
    }
  }

  @Test(groups = {"functional"})
  public void testinitMulti() throws Exception {
    try {
      compileMulti("init", "init");
    } catch (final ADLException e) {
      assertTrue(e.getError() instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) e.getError())
          .getErrors();
      assertEquals(errors.size(), 1);
      final Error error = errors.iterator().next();
      assertSame(error.getTemplate(), MPPErrors.PARSE_ERROR);
      assertEquals(error.getLocator().getBeginLine(), 6);
      assertTrue(error.getLocator().getInputFilePath().endsWith("init/data.h"));
      System.out.println(ErrorHelper.formatError(error));
    }
  }
}
