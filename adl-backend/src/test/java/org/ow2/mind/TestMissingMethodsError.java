/**
 * Copyright (C) 2013 STMicroelectronics
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
 * Authors: Stephane Seyvoz
 * Contributors: 
 */

package org.ow2.mind;

import java.util.Collection;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.ErrorTemplate;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.preproc.MPPErrors;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Inject;

public class TestMissingMethodsError extends AbstractFunctionalTest {

  @Inject
  protected ErrorManager errorManager;

  @Test(groups = {"functional"})
  public void testMissingPrintLnFlush() throws Exception {
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        "common", "functional");

    boolean errorOccured = false;

    try {
      runner.compileRunAndCheck("missingmeth.helloworld.HelloworldApplication",
          null);
    } catch (final ADLException e) {
      errorOccured = true;
      // Expected behavior: the compilation HAS to fail
      final Error err = e.getError();
      Assert.assertTrue(err instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) err).getErrors();
      Assert.assertEquals(errors.size(), 1);
      final Error[] errorsAsArray = errors.toArray(new Error[1]);
      final Error theRealError = errorsAsArray[0];
      final ErrorTemplate theErrorTemplate = theRealError.getTemplate();
      Assert.assertTrue(theErrorTemplate instanceof MPPErrors);

      Assert
          .assertTrue(theErrorTemplate.getErrorId() == MPPErrors.MISSING_METHOD_DECLARATION
              .getErrorId());

      Assert
          .assertTrue(theRealError
              .getMessage()
              .equals(
                  "In definition missingmeth.helloworld.Server: METH(s, [println, flush]) method(s) haven't been implemented !"));
    }

    if (!errorOccured) {
      Assert
          .fail("MPPError.MISSING_METHOD_DECLARATION should have been raised !");
    }
  }

  @Test(groups = {"functional"})
  public void testSplitMissingFlush() throws Exception {
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        "common", "functional");

    boolean errorOccured = false;

    try {
      runner.compileRunAndCheck(
          "missingmeth.helloworldSplitImpl.HelloworldApplication", null);
    } catch (final ADLException e) {
      errorOccured = true;
      // Expected behavior: the compilation HAS to fail
      final Error err = e.getError();
      Assert.assertTrue(err instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) err).getErrors();
      Assert.assertEquals(errors.size(), 1);
      final Error[] errorsAsArray = errors.toArray(new Error[1]);
      final Error theRealError = errorsAsArray[0];
      final ErrorTemplate theErrorTemplate = theRealError.getTemplate();
      Assert.assertTrue(theErrorTemplate instanceof MPPErrors);

      Assert
          .assertTrue(theErrorTemplate.getErrorId() == MPPErrors.MISSING_METHOD_DECLARATION
              .getErrorId());

      Assert
          .assertTrue(theRealError
              .getMessage()
              .equals(
                  "In definition missingmeth.helloworldSplitImpl.Server: METH(s, [flush]) method(s) haven't been implemented !"));
    }

    if (!errorOccured) {
      Assert
          .fail("MPPError.MISSING_METHOD_DECLARATION should have been raised !");
    }
  }

  @Test(groups = {"functional"})
  public void testCollMissingPrintLnFlushX2() throws Exception {
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        "common", "functional");

    boolean errorOccured = false;

    try {
      runner.compileRunAndCheck(
          "missingmeth.helloworldColl.HelloworldApplication", null);
    } catch (final ADLException e) {
      errorOccured = true;
      // Expected behavior: the compilation HAS to fail
      final Error err = e.getError();
      Assert.assertTrue(err instanceof ErrorCollection);
      final Collection<Error> errors = ((ErrorCollection) err).getErrors();
      Assert.assertEquals(errors.size(), 1);
      final Error[] errorsAsArray = errors.toArray(new Error[1]);
      final Error theRealError = errorsAsArray[0];
      final ErrorTemplate theErrorTemplate = theRealError.getTemplate();
      Assert.assertTrue(theErrorTemplate instanceof MPPErrors);

      Assert
          .assertTrue(theErrorTemplate.getErrorId() == MPPErrors.MISSING_COLL_METHOD_DECLARATION
              .getErrorId());

      Assert
          .assertTrue(theRealError
              .getMessage()
              .equals(
                  "In definition missingmeth.helloworldColl.Server: METH(s[0], [println, flush]) method(s) haven't been implemented !"));
    }

    if (!errorOccured) {
      Assert
          .fail("MPPError.MISSING_METHOD_DECLARATION should have been raised !");
    }
  }
}
