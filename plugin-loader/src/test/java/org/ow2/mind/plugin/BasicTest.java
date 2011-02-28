/**
 * Copyright (C) 2010 STMicroelectronics
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
 */

package org.ow2.mind.plugin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;

import org.ow2.mind.plugin.util.BooleanEvaluatorHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class BasicTest {
  PluginManager pluginManager;

  @BeforeMethod(alwaysRun = true)
  public void setUpNodeFactory() {

    final URL url1 = this.getClass().getClassLoader()
        .getResource("com.st.p2012.memory/");
    assertNotNull(url1);
    final URL url2 = this.getClass().getClassLoader()
        .getResource("org.ow2.mind.mindc/");
    assertNotNull(url2);

    final Injector injector = Guice.createInjector(Modules.override(
        new PluginLoaderModule()).with(new AbstractModule() {

      @Override
      public void configure() {
        bind(ClassLoader.class).annotatedWith(
            Names.named(BasicPluginManager.PLUGIN_CLASS_LOADER)).toInstance(
            new URLClassLoader(new URL[]{url1, url2}));
      }
    }));
    pluginManager = injector.getInstance(PluginManager.class);
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {

    final Iterable<Extension> extensions = pluginManager
        .getExtensions("org.ow2.mind.mindc.cpl");
    assertNotNull(extensions);
    assertEquals(Iterables.size(extensions), 1);
    final Extension extension = extensions.iterator().next();
    assertNotNull(extension);
    assertEquals(extension.getExtensionPointID(), "org.ow2.mind.mindc.cpl");

    final Iterable<ConfigurationElement> elems = extension
        .getConfigurationElements();
    assertEquals(Iterables.size(elems), 1);
    final ConfigurationElement element = elems.iterator().next();
    assertEquals(element.getName(), "cpl");
    assertEquals(element.getAttribute("class"), "foo.bar");
    assertSame(extension.getConfigurationElements("cpl").iterator().next(),
        element);

    final ConfigurationElement child = element.getChild("enableWhen");
    assertNotNull(child);
    final ConfigurationElement condition = child.getChild();
    assertNotNull(condition);
    assertTrue(BooleanEvaluatorHelper.evaluate(condition, pluginManager, null));
  }
}
