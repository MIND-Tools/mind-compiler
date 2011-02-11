
package org.ow2.mind.mindc.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.mind.Launcher;
import org.ow2.mind.cli.CmdAppendOption;
import org.ow2.mind.cli.CmdArgument;
import org.ow2.mind.cli.CmdFlag;
import org.ow2.mind.cli.CmdOption;
import org.ow2.mind.cli.CmdPathOption;
import org.ow2.mind.cli.CmdProperties;
import org.ow2.mind.cli.InvalidCommandLineException;
import org.ow2.mind.cli.Options;
import org.ow2.mind.plugin.BasicPluginManager;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

/**
 * Copyright (C) 2009 STMicroelectronics This file is part of "Mind Compiler" is
 * free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. Contact: mind@ow2.org Authors: Ali Erdem
 * Ozcan Contributors:
 */

public class LauncherTest {

  LauncherTester tester;

  @BeforeMethod(alwaysRun = true)
  void setup() throws Exception {
    final List<String> pathList = new ArrayList<String>();
    pathList.add("src/test/resources/test");
    tester = new LauncherTester(pathList);
  }

  @DataProvider(name = "options-list")
  public Object[][] OptionsDataProvider() {
    return new Object[][]{{CmdFlag.class, "X"}, {CmdProperties.class, "Y"},
        {CmdArgument.class, "Z"}, {CmdAppendOption.class, "W"},
        {CmdPathOption.class, "Q"}};
  }

  @Test(dataProvider = "options-list")
  public void testOptions(final Class<? extends CmdOption> optionClass,
      final String shortName) throws Exception {
    try {
      tester.testMain();
    } catch (final InvalidCommandLineException e) {
      // We expect to catch this exeption since we don't give a valid command
      // line.
    }
    final Options options = tester.getOptions();
    checkOption(options, optionClass, shortName);
  }

  private <T extends CmdOption> void checkOption(final Options options,
      final Class<T> optClass, final String shortName) throws Exception {
    for (final CmdOption option : options.getOptions()) {
      if (optClass.isInstance(option)) {
        if (shortName.equals(option.getShortName())) {
          return;
        }
      }
    }
    throw new Exception("'" + shortName + "' option not found.");
  }

  protected class LauncherTester extends Launcher {
    final List<String> pathList;

    protected LauncherTester(final List<String> pathList) {
      this.pathList = pathList;
    }

    @Override
    protected Injector getBootstrapInjector() {
      return Guice.createInjector(Modules.override(new PluginLoaderModule())
          .with(new AbstractModule() {
            @Override
            public void configure() {
              bind(ClassLoader.class).annotatedWith(
                  Names.named(BasicPluginManager.PLUGIN_CLASS_LOADER))
                  .toInstance(getPluginClassLoader(pathList));
            }
          }));
    }

    public Map<Object, Object> getContext() {
      return compilerContext;
    }

    public Options getOptions() {
      return options;
    }

    public void testMain(final String... args)
        throws InvalidCommandLineException {
      init(args);
      compile(null, null);
    }
  }

  protected ClassLoader getPluginClassLoader(final List<String> pathList) {
    final List<String> validatedPaths = new ArrayList<String>(pathList.size());

    // check source paths
    for (final String path : pathList) {
      final File f = new File(path);
      if (!f.exists()) {
        System.out.println("Warning '" + f.getAbsolutePath()
            + "' source path can't be found ");
      } else if (!f.isDirectory()) {
        System.out.println("Warning: \"" + path
            + "\" is not a directory, path ignored.");
      } else {
        validatedPaths.add(path);
      }
    }

    // build URL array of source path
    final URL[] urls = new URL[validatedPaths.size()];
    for (int i = 0; i < urls.length; i++) {
      final String path = validatedPaths.get(i);
      final File f = new File(path);
      try {
        urls[i] = f.toURI().toURL();
      } catch (final MalformedURLException e) {
        // never append
        throw new Error(e);
      }
    }

    return new URLClassLoader(urls, getClass().getClassLoader());

  }
}
