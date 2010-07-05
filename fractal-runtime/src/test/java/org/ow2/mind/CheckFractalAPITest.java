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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CheckFractalAPITest {

  ErrorManager        errorManager;
  IDLLoader           idlLoader;
  Map<Object, Object> context;

  @BeforeTest(alwaysRun = true)
  public void setUp() {
    errorManager = ErrorManagerFactory.newSimpleErrorManager();
    idlLoader = IDLLoaderChainFactory.newLoader(errorManager);
    context = new HashMap<Object, Object>();
  }

  @DataProvider(name = "fractal-api")
  public Object[][] apiDataProvider() throws Exception {
    final URL apiDirURL = getClass().getClassLoader()
        .getResource("fractal/api");
    assertNotNull(apiDirURL);
    final File apiDir = new File(apiDirURL.toURI());
    assertTrue(apiDir.isDirectory());

    final File[] itfFiles = apiDir.listFiles(new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return name.endsWith(".itf");
      }
    });

    final Object[][] result = new Object[itfFiles.length][];
    for (int i = 0; i < itfFiles.length; i++) {
      result[i] = new Object[1];
      final String name = itfFiles[i].getName();
      result[i][0] = "fractal.api." + name.substring(0, name.length() - 4);
    }

    return result;
  }

  @Test(groups = {"checkin", "functional"}, dataProvider = "fractal-api")
  public void checkIDL(final String name) throws Exception {
    errorManager.clear();
    idlLoader.load(name, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }
}
