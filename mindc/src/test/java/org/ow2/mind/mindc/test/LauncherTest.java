
package org.ow2.mind.mindc.test;

import java.util.Map;

import org.ow2.mind.Launcher;
import org.ow2.mind.AbstractLauncher.CmdFlag;
import org.ow2.mind.AbstractLauncher.CmdOption;
import org.ow2.mind.AbstractLauncher.CmdProperties;
import org.ow2.mind.AbstractLauncher.InvalidCommandLineException;
import org.ow2.mind.AbstractLauncher.Options;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    tester = new LauncherTester();
  }

  @Test
  public void test1() throws Exception {
    try {
      tester.nonExitMain();
    } catch (final InvalidCommandLineException e) {

    }
    final Options options = tester.getOptions();
    checkOption(options, CmdFlag.class, "X");
    checkOption(options, CmdProperties.class, "Y");
  }

  private <T extends CmdOption> void checkOption(final Options options,
      final Class<T> optClass, final String shortName) throws Exception {
    for (final CmdOption option : options.getOptions()) {
      if (optClass.isInstance(option)) {
        if (option.getShortName().equals(shortName)) {
          return;
        }
      }
    }
    throw new Exception("'" + shortName + "' option not found.");
  }

  protected class LauncherTester extends Launcher {
    public Map<Object, Object> getContext() {
      return compilerContext;
    }

    public Options getOptions() {
      return options;
    }
  }
}
