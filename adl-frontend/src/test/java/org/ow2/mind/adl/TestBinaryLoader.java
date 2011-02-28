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

package org.ow2.mind.adl;

import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestBinaryLoader {

  Loader              loader;
  Instantiator        instantiator;

  Map<Object, Object> context;

  File                binADLDir;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new IDLFrontendModule(),
        new ADLFrontendModule() {
          protected void configureErrorLoader() {
            bind(Loader.class).annotatedWith(Names.named("ErrorLoader"))
                .toChainStartingWith(ErrorLoader.class)
                .endingWith(Loader.class);
          }
        });

    loader = injector.getInstance(Key.get(Loader.class,
        Names.named("ErrorLoader")));
    instantiator = injector.getInstance(Instantiator.class);

    binADLDir = new File("target/test/defs");
    rm(binADLDir);
    initContext();
  }

  protected void initContext() throws MalformedURLException {
    context = new HashMap<Object, Object>();
    context.put("binADLdir", binADLDir);
    final ClassLoader srcClassLoader = new URLClassLoader(new URL[]{binADLDir
        .toURI().toURL()}, getClass().getClassLoader());
    context.put("classloader", srcClassLoader);
  }

  protected static void rm(final File file) {
    if (file.isDirectory()) {
      for (final File subFile : file.listFiles()) {
        rm(subFile);
      }
    } else {
      file.delete();
    }
  }

  @Test(groups = {"functional", "checkin"})
  public void test1() throws Exception {
    final Definition d1 = loader.load("pkg1.generic.Composite4", context);
    assertNotNull(d1);
    final ComponentGraph g1 = instantiator.instantiate(d1, context);
    assertNotNull(g1);

    // reinitialize context
    initContext();

    // reload the same one.
    final Definition d2 = loader.load("pkg1.generic.Composite4", context);
    assertNotNull(d2);
    final ComponentGraph g2 = instantiator.instantiate(d2, context);
    assertNotNull(g2);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Definition d1 = loader.load("pkg1.parameterGeneric.Composite1",
        context);
    assertNotNull(d1);
    final ComponentGraph g1 = instantiator.instantiate(d1, context);
    assertNotNull(g1);

    // reinitialize context
    initContext();

    // reload the same one.
    final Definition d2 = loader.load("pkg1.parameterGeneric.Composite2",
        context);
    assertNotNull(d2);
    final ComponentGraph g2 = instantiator.instantiate(d2, context);
    assertNotNull(g2);
  }
}
