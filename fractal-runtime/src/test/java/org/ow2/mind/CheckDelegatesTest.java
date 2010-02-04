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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.cecilia.adl.directives.DirectiveHelper;
import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CheckDelegatesTest {

  public static final String DEFAULT_CFLAGS  = "-std=c89 -g -Wall -Werror -Wredundant-decls -Wunreachable-code -Wstrict-prototypes -Wwrite-strings";
  public static final String CFLAGS_PROPERTY = "mind.test.cflags";

  CompilerWrapper            compilerWrapper;
  CompilationCommandExecutor commandExecutor;
  Map<Object, Object>        context;

  @BeforeTest(alwaysRun = true)
  public void setUp() {
    compilerWrapper = new GccCompilerWrapper();
    commandExecutor = new BasicCompilationCommandExecutor();
    context = new HashMap<Object, Object>();

    final String cFlags = System.getProperty(CFLAGS_PROPERTY, DEFAULT_CFLAGS);
    CompilerContextHelper.setCFlags(context, DirectiveHelper
        .splitOptionString(cFlags));
  }

  @Test(groups = {"checkin", "functional"})
  public void compileCIdelegate() throws Exception {
    compileDelegate("CIdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileBCdelegate() throws Exception {
    compileDelegate("BCdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileACdelegate() throws Exception {
    compileDelegate("ACdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileLCCdelegate() throws Exception {
    compileDelegate("LCCdelegate");
  }

  private void compileDelegate(final String delegateName)
      throws URISyntaxException, IOException, ADLException,
      InterruptedException {
    final CompilerCommand command = compilerWrapper.newCompilerCommand(context);
    final File src = new File(getClass().getClassLoader().getResource(
        "fractal/internal/" + delegateName + ".c").toURI());
    final File outputFile = File.createTempFile(delegateName, ".o");
    command.setInputFile(src).setOutputFile(outputFile);
    commandExecutor.exec(Arrays.asList((CompilationCommand) command), context);
  }

}
