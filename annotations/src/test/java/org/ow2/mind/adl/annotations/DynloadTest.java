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
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl.annotations;

import org.ow2.mind.CompilerRunner;
import org.testng.annotations.Test;

public class DynloadTest extends org.ow2.mind.DynloadTest {

  @Override
  protected void initSourcePath(final CompilerRunner runner) {
    initSourcePath(runner, getDepsDir("fractal/api/Component.itf")
        .getAbsolutePath(), getDepsDir("common/ApplicationType.adl")
        .getAbsolutePath() + "/common", getDepsDir("dynload/DynloadTester.adl")
        .getAbsolutePath() + "/dynload", "functional", "dynload");
  }

  @Test(groups = {"functional"})
  public void testControlledPrimitiveSingleton() throws Exception {
    // this test is not supported on windows.
    if (isRunningOnWindows()) return;

    runTester(dynloadRunner.compile("EmptyControlledSingletonPrimitive"), true,
        false, false);
  }

  @Test(groups = {"functional"})
  public void testControlledPrimitiveMulti() throws Exception {
    // this test is not supported on windows.
    if (isRunningOnWindows()) return;

    runTester(dynloadRunner.compile("EmptyControlledMultiPrimitive"), true,
        false, false);
  }

  @Test(groups = {"functional"})
  public void testFactoryControlledPrimitiveMulti() throws Exception {
    // this test is not supported on windows.
    if (isRunningOnWindows()) return;

    runTester(dynloadRunner.compile("Factory<EmptyControlledMultiPrimitive>"),
        false, true, true);
  }

  @Test(groups = {"functional"})
  public void testControlledFactoryPrimitiveMulti() throws Exception {
    // this test is not supported on windows.
    if (isRunningOnWindows()) return;

    runTester(dynloadRunner.compile("FactoryWithCtrl<EmptyMultiPrimitive>"),
        true, true, false);
  }

  @Test(groups = {"functional"})
  public void testControlledFactoryControlledPrimitiveMulti() throws Exception {
    // this test is not supported on windows.
    if (isRunningOnWindows()) return;

    runTester(
        dynloadRunner.compile("FactoryWithCtrl<EmptyControlledMultiPrimitive>"),
        true, true, true);
  }
}
