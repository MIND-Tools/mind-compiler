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

import java.net.URL;
import java.net.URLClassLoader;

import org.testng.annotations.BeforeMethod;

public abstract class AbstractFunctionalTest {

  protected static final String COMMON_ROOT_DIR = "common/";

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
    for (String rootDir : rootDirs) {
      if (!rootDir.endsWith("/")) rootDir += "/";
      final ClassLoader srcLoader = new URLClassLoader(new URL[]{
          getClass().getClassLoader().getResource(COMMON_ROOT_DIR),
          getClass().getClassLoader().getResource(rootDir)}, null);
      runner.context.put("classloader", srcLoader);
    }
  }

  protected boolean isRunningOnWindows() {
    return System.getProperty("os.name").contains("Windows");
  }
}
