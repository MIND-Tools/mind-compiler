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
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.testng.annotations.BeforeMethod;

public abstract class AbstractFunctionalTest {

  protected CompilerRunner runner;

  @BeforeMethod(alwaysRun = true)
  protected void setup() throws Exception {
    runner = new CompilerRunner();
  }

  protected void initSourcePath(final String... rootDirs) {
    initSourcePath(runner, rootDirs);
  }

  protected void initSourcePath(final CompilerRunner runner,
      final String... rootDirs) {
    initSourcePath(runner, null, rootDirs);
  }

  protected void initSourcePath(final ClassLoader parent,
      final String... rootDirs) {
    initSourcePath(runner, parent, rootDirs);
  }

  protected void initSourcePath(final CompilerRunner runner,
      final ClassLoader parent, final String... rootDirs) {
    final List<URL> rootDirList = new ArrayList<URL>();
    for (String rootDir : rootDirs) {
      final File rootFile = new File(rootDir);
      if (rootFile.isAbsolute()) {
        if (!rootFile.isDirectory()) {
          fail(rootDir + " is not a valid source directory");
        }
        try {
          rootDirList.add(rootFile.toURI().toURL());
        } catch (final MalformedURLException e) {
          fail(rootDir + " is not a valid source directory", e);
        }
      } else {
        if (!rootDir.endsWith("/")) rootDir += "/";
        Enumeration<URL> resources;
        try {
          resources = getClass().getClassLoader().getResources(rootDir);
        } catch (final IOException e) {
          fail("Fail to lookup " + rootDir + "in classpath", e);
          return;
        }
        URL rootDirURL = null;
        while (resources.hasMoreElements()) {
          final URL resource = resources.nextElement();
          if (resource.getProtocol().equals("file")) {
            rootDirURL = resource;
            break;
          }
        }
        assertNotNull(rootDirURL, "Can't find directory " + rootDir
            + " in the classpath");
        rootDirList.add(rootDirURL);
      }
    }

    System.out.println("Init src path : " + rootDirList);
    final ClassLoader srcLoader = new URLClassLoader(rootDirList
        .toArray(new URL[0]), null);

    runner.context.put("classloader", srcLoader);
  }

  protected File getDepsDir(final String resource) {
    try {
      return DepsHelper.unpackDeps(resource, this.getClass().getClassLoader());
    } catch (final Exception e) {
      fail("Can't unpack dependency containing " + resource, e);
      return null;
    }
  }

  protected boolean isRunningOnWindows() {
    return System.getProperty("os.name").contains("Windows");
  }
}
