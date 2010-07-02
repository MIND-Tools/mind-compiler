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

package org.ow2.mind;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.ExecutionHelper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLibImpl extends AbstractFunctionalTest {

  CompilerWrapper compilerWrapper;

  @Override
  @BeforeMethod(alwaysRun = true)
  protected void setup() throws Exception {
    super.setup();
    compilerWrapper = new GccCompilerWrapper();
  }

  @Test(groups = {"functional"})
  public void test_myLib_O() throws Exception {
    initPath();

    // compile mylib.c to mylib.o
    final File inputFile = new File(getClass().getClassLoader()
        .getResource("libimpl/impl/myLib.c").toURI());
    final File objectFile = new File("target/build-libimpl/impl/myLib.o");
    objectFile.getParentFile().mkdirs();

    final CompilerCommand compilerCommand = compilerWrapper
        .newCompilerCommand(runner.context);
    compilerCommand.setInputFile(inputFile).setOutputFile(objectFile);

    runner.executor.exec(Arrays.asList((CompilationCommand) compilerCommand),
        runner.context);

    runner.compileAndRun(
        "GenericApplication<impl.MyLibTester<impl.MyLibWrapper_O>>",
        "myLibTester_O");
  }

  @Test(groups = {"functional"})
  public void test_myLib_SO() throws Exception {
    if (isRunningOnWindows()) {
      // not supported on windows
      return;
    }
    initPath();

    // compile mylib.c to mylib.so
    final File inputFile = new File(getClass().getClassLoader()
        .getResource("libimpl/impl/myLib.c").toURI());
    final File objectFile = new File("target/build-libimpl/impl/myLib.o");
    final File soFile = new File("target/build-libimpl/impl/myLib.so");
    objectFile.getParentFile().mkdirs();

    final CompilerCommand compilerCommand = compilerWrapper
        .newCompilerCommand(runner.context);
    compilerCommand.setInputFile(inputFile).setOutputFile(objectFile)
        .addFlag("-fPIC");
    final LinkerCommand linkerCommand = compilerWrapper
        .newLinkerCommand(runner.context);
    linkerCommand.addInputFile(objectFile).setOutputFile(soFile)
        .addFlag("--shared");

    runner.executor.exec(Arrays.asList(compilerCommand, linkerCommand),
        runner.context);

    runner.compileAndRun(
        "GenericApplication<impl.MyLibTester<impl.MyLibWrapper_SO>>",
        "myLibTester_SO");
  }

  @Test(groups = {"functional"})
  public void test_myLib_A() throws Exception {
    if (isRunningOnWindows()) {
      // not supported on windows
      return;
    }
    initPath();

    // compile mylib.c to mylib.so
    final File inputFile = new File(getClass().getClassLoader()
        .getResource("libimpl/impl/myLib.c").toURI());
    final File objectFile = new File("target/build-libimpl/impl/myLib.o");
    final File aFile = new File("target/build-libimpl/impl/myLib.a");
    objectFile.getParentFile().mkdirs();

    final CompilerCommand compilerCommand = compilerWrapper
        .newCompilerCommand(runner.context);
    compilerCommand.setInputFile(inputFile).setOutputFile(objectFile)
        .addFlag("-fPIC");

    runner.executor.exec(Arrays.asList((CompilationCommand) compilerCommand),
        runner.context);

    if (aFile.exists()) aFile.delete();
    assertEquals(
        ExecutionHelper.exec(
            "ar -r " + aFile.getAbsolutePath() + " "
                + objectFile.getAbsolutePath()).getExitValue(), 0);

    runner.compileAndRun(
        "GenericApplication<impl.MyLibTester<impl.MyLibWrapper_A>>",
        "myLibTester_A");
  }

  protected void initPath() {
    final File buildDir = new File("target/build-libimpl").getAbsoluteFile();
    buildDir.mkdirs();
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        "common", "libimpl", buildDir.getPath());
  }
}
