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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;

public abstract class AbstractFunctionalTest {

  protected static final String TEST_DEPS         = "test.deps";

  // This default test.deps value is intended to be used only while running test
  // from Eclipse.
  // When tests are run with Maven, the test.deps property is set to a directory
  // that contains an unpacked version of fractal-runtime.
  protected static final String DEFAULT_TEST_DEPS = "../fractal-runtime/src/main/resources";

  protected CompilerRunner      runner;

  @BeforeMethod(alwaysRun = true)
  protected void setup() throws Exception {
    runner = new CompilerRunner();
  }

  protected void initSourcePath(final String... rootDirs) {
    initSourcePath(runner, rootDirs);
  }

  protected void initSourcePath(final CompilerRunner runner,
      final String... rootDirs) {
    final List<URL> rootDirList = new ArrayList<URL>();
    for (String rootDir : rootDirs) {
      if (!rootDir.endsWith("/")) rootDir += "/";
      rootDirList.add(getClass().getClassLoader().getResource(rootDir));
    }

    String testDeps = System.getProperty(TEST_DEPS);
    if (testDeps == null) {
      testDeps = getDefaultTestDeps();
    }
    for (final String testDep : testDeps.split(File.pathSeparator)) {
      final File testDepsDir = new File(testDep);
      assertTrue(testDepsDir.isDirectory(), "Invalid " + TEST_DEPS
          + " property : " + testDep);
      try {
        rootDirList.add(testDepsDir.toURI().toURL());
      } catch (final MalformedURLException e) {
        fail("Invalid test.deps property : " + testDep, e);
      }
    }

    final ClassLoader srcLoader = new URLClassLoader(
        rootDirList.toArray(new URL[0]), null);

    runner.context.put("classloader", srcLoader);
  }

  protected String getDefaultTestDeps() {
    return DEFAULT_TEST_DEPS;
  }

  protected boolean isRunningOnWindows() {
    return System.getProperty("os.name").contains("Windows");
  }
}
