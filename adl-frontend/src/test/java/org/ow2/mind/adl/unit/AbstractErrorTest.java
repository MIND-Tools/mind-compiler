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

package org.ow2.mind.adl.unit;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLFrontendModule;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.SimpleErrorManager;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class AbstractErrorTest {

  private static final String   COMMON_ROOT_DIR = "unit/common/";

  protected ErrorManager        errorManager;
  protected ADLLocator          adlLocator;
  protected Loader              loader;

  protected Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    final Injector injector = Guice.createInjector(new CommonFrontendModule() {
      @Override
      protected void configureErrorManager() {
        bind(ErrorManager.class).to(SimpleErrorManager.class);
      }
    }, new PluginLoaderModule(), new IDLFrontendModule(),
        new ADLFrontendModule());

    loader = injector.getInstance(Loader.class);
    // error manager component
    errorManager = injector.getInstance(ErrorManager.class);
    adlLocator = injector.getInstance(ADLLocator.class);

    context = new HashMap<Object, Object>();
  }

  protected void initSourcePath(String rootDir) {
    if (!rootDir.endsWith("/")) rootDir += "/";
    final ClassLoader srcLoader = new URLClassLoader(new URL[]{
        getClass().getClassLoader().getResource(COMMON_ROOT_DIR),
        getClass().getClassLoader().getResource(rootDir)}, null);
    context.put("classloader", srcLoader);
  }

  protected URL findADL(final String adlName) throws IOException {
    final URL adl = adlLocator.findSourceADL(adlName, context);
    assertNotNull(adl, "Can't find ADL " + adlName);
    return adl;
  }
}
