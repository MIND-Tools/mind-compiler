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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLoaderChainFactory.IDLFrontend;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractADLLoaderTest {

  private static final String   COMMON_ROOT_DIR = "unit/common/";

  protected ErrorManager        errorManager;
  protected ADLLocator          adlLocator;
  protected Loader              loader;

  protected Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    // error manager component
    errorManager = ErrorManagerFactory.newSimpleErrorManager();

    // input locators
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final IDLLocator idlLocator = IDLLoaderChainFactory
        .newIDLLocator(inputResourceLocator);
    adlLocator = Factory.newADLLocator(inputResourceLocator);
    final ImplementationLocator implementationLocator = Factory
        .newImplementationLocator(inputResourceLocator);

    // Plugin Manager Components
    final org.objectweb.fractal.adl.Factory pluginFactory = new SimpleClassPluginFactory();

    // loader chains
   final IDLFrontend idlFrontend = IDLLoaderChainFactory.newLoader(errorManager,
        idlLocator, inputResourceLocator, pluginFactory);
    final Loader adlLoader = Factory.newLoader(errorManager,
        inputResourceLocator, adlLocator, idlLocator, implementationLocator,
        idlFrontend.cache, idlFrontend.loader, pluginFactory);
    final ErrorLoader errLoader = new ErrorLoader();
    errLoader.clientLoader = adlLoader;
    errLoader.errorManagerItf = errorManager;

    loader = errLoader;

    context = new HashMap<Object, Object>();
  }

  protected void initSourcePath(String rootDir) {
    if (!rootDir.endsWith("/")) rootDir += "/";
    final ClassLoader srcLoader = new URLClassLoader(new URL[]{
        getClass().getClassLoader().getResource(COMMON_ROOT_DIR),
        getClass().getClassLoader().getResource(rootDir)}, null);
    context.put("classloader", srcLoader);
  }

  protected LineNumberReader readADL(final String adlName) throws IOException {
    final URL adl = adlLocator.findSourceADL(adlName, context);
    assertNotNull(adl, "Can't find ADL " + adlName);
    return new LineNumberReader(new InputStreamReader(adl.openStream()));
  }
}
