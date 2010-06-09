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

import static org.ow2.mind.compilation.DirectiveHelper.splitOptionString;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.ow2.mind.idl.IDLBackendFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class CheckDelegatesTest {

  public static final String DEFAULT_CFLAGS  = "-g -Wall -Werror -Wredundant-decls -Wunreachable-code -Wstrict-prototypes -Wwrite-strings";
  public static final String CFLAGS_PROPERTY = "mind.test.cflags";

  IDLLoader                  idlLoader;
  IDLVisitor                 idlCompiler;

  CompilerWrapper            compilerWrapper;
  CompilationCommandExecutor commandExecutor;
  Map<Object, Object>        context;
  File                       buildDir;

  @BeforeTest(alwaysRun = true)
  public void setUp() {
    idlLoader = IDLLoaderChainFactory.newLoader();
    idlCompiler = IDLBackendFactory.newIDLCompiler(idlLoader);

    compilerWrapper = new GccCompilerWrapper();
    commandExecutor = new BasicCompilationCommandExecutor();

    context = new HashMap<Object, Object>();
    buildDir = new File("target/build");
    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);

    final String cFlags = System.getProperty(CFLAGS_PROPERTY, DEFAULT_CFLAGS);
    CompilerContextHelper.setCFlags(context, splitOptionString(cFlags));
  }

  @Test(groups = {"checkin", "functional"})
  public void compileCIdelegate() throws Exception {
    compileIDL("fractal.api.Component");
    compileDelegate("CIdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileBCdelegate() throws Exception {
    compileIDL("fractal.api.BindingController");
    compileDelegate("BCdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileCCdelegate() throws Exception {
    compileIDL("fractal.api.ContentController");
    compileIDL("fractal.api.BindingController");
    compileIDL("fractal.api.LifeCycleController");
    compileDelegate("CCdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileACdelegate() throws Exception {
    compileIDL("fractal.api.AttributeController");
    compileDelegate("ACdelegate");
  }

  @Test(groups = {"checkin", "functional"})
  public void compileLCCdelegate() throws Exception {
    compileIDL("fractal.api.LifeCycleController");
    compileDelegate("LCCdelegate");
  }

  private void compileIDL(final String idlName) throws ADLException {
    final IDL idl = idlLoader.load(idlName, context);
    idlCompiler.visit(idl, context);
  }

  private void compileDelegate(final String delegateName)
      throws URISyntaxException, IOException, ADLException,
      InterruptedException {
    final CompilerCommand command = compilerWrapper.newCompilerCommand(context);
    final File src = new File(getClass().getClassLoader()
        .getResource("fractal/internal/" + delegateName + ".c").toURI());
    final File outputFile = new File(buildDir, delegateName + ".o");
    command.setInputFile(src).setOutputFile(outputFile);
    command.addIncludeDir(buildDir);
    command.addIncludeDir(new File("target/classes"));
    commandExecutor.exec(Arrays.asList((CompilationCommand) command), context);
  }

}
