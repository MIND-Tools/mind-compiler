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

package org.ow2.mind.idl;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.ow2.mind.unit.ExpectedErrorHelper;
import org.ow2.mind.unit.UnitTestDataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SemanticErrorTest {

  protected ErrorManager        errorManager;
  protected IDLLocator          idlLocator;
  protected IDLLoader           idlLoader;

  protected Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new IDLFrontendModule(), new PluginLoaderModule());

    // error manager component
    errorManager = injector.getInstance(ErrorManager.class);

    // input locators
    idlLocator = injector.getInstance(IDLLocator.class);

    // loader chains
    idlLoader = injector.getInstance(IDLLoader.class);

    context = new HashMap<Object, Object>();
  }

  protected void initSourcePath(String rootDir) {
    if (!rootDir.endsWith("/")) rootDir += "/";
    final ClassLoader srcLoader = new URLClassLoader(new URL[]{getClass()
        .getClassLoader().getResource(rootDir)}, null);
    context.put("classloader", srcLoader);
  }

  protected URL findIDL(final String idlName) throws IOException {
    final URL adl = idlLocator.findSourceItf(idlName, context);
    assertNotNull(adl, "Can't find IDL " + idlName);
    return adl;
  }

  @DataProvider(name = "unit-test")
  protected Object[][] dataProvider() throws Exception {
    return UnitTestDataProvider.listIDLs("unit/error/semantic");
  }

  @Test(dataProvider = "unit-test", groups = {"functional"})
  public void semanticErrorTest(final String rootDir, final String idlName)
      throws Exception {
    initSourcePath(rootDir);
    idlLoader.load(idlName, context);
    ExpectedErrorHelper.checkErrors(findIDL(idlName), errorManager.getErrors());
  }
}
