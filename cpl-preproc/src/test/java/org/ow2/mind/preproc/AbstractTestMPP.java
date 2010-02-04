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

package org.ow2.mind.preproc;

import static org.objectweb.fractal.cecilia.adl.directives.DirectiveHelper.splitOptionString;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.preproc.BasicMPPWrapper;
import org.ow2.mind.preproc.MPPCommand;
import org.ow2.mind.preproc.MPPWrapper;
import org.testng.annotations.BeforeTest;

public class AbstractTestMPP {

  protected static final String        DEFAULT_CPPFLAGS = "-g -Wall -Wredundant-decls -Wunreachable-code";

  protected MPPWrapper                 mppWrapper;
  protected CompilerWrapper            compilerWrapper;
  protected CompilationCommandExecutor executor;

  protected Map<Object, Object>        context;
  protected File                       buildDir;
  protected List<String>               cppFlags;
  protected List<String>               cFlags;

  @BeforeTest(alwaysRun = true)
  public void setUp() {
    mppWrapper = new BasicMPPWrapper();
    compilerWrapper = new GccCompilerWrapper();
    executor = new BasicCompilationCommandExecutor();

    context = new HashMap<Object, Object>();
    final String buildDirName = "target" + File.separator + "build";
    buildDir = new File(buildDirName);
    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);

    cppFlags = splitOptionString(DEFAULT_CPPFLAGS);
  }

  // ---------------------------------------------------------------------------
  // Helper methods
  // ---------------------------------------------------------------------------

  protected File locateCFile(final String dirName, final String fileName)
      throws Exception {
    final String name = dirName + "/" + fileName + ".c";
    final URL url = getClass().getClassLoader().getResource(name);
    if (url == null) throw new Exception("Can't find file " + name);
    return new File(url.toURI());
  }

  protected File newBuildFile(final String dirName, final String fileName,
      final String ext) {
    final File file = new File(buildDir, dirName + File.separator + fileName
        + ext);
    file.getParentFile().mkdirs();
    return file;
  }

  protected PreprocessorCommand newCPPCommand(final String dirName,
      final String fileName) throws Exception {
    final File cInputFile = locateCFile(dirName, fileName);
    final PreprocessorCommand command = compilerWrapper
        .newPreprocessorCommand(context);
    command.setInputFile(cInputFile);
    command.setOutputFile(newBuildFile(dirName, fileName, ".i"));
    command.addFlags(cppFlags);

    return command;
  }

  protected MPPCommand newMPPCommand(final String dirName,
      final String fileName, final boolean singleton) {
    final MPPCommand command = mppWrapper.newMPPCommand(context);
    command.setInputFile(newBuildFile(dirName, fileName, ".i"));
    command.setOutputFile(newBuildFile(dirName, fileName, singleton
        ? ".singleton-mpp.c"
        : ".multi-mpp.c"));
    if (singleton) command.setSingletonMode();
    return command;
  }

  protected CompilerCommand newGCCCommand(final String dirName,
      final String fileName, final boolean singleton) throws Exception {
    final CompilerCommand command = compilerWrapper.newCompilerCommand(context);
    command.setInputFile(newBuildFile(dirName, fileName, singleton
        ? ".singleton-mpp.c"
        : ".multi-mpp.c"));
    command.setOutputFile(newBuildFile(dirName, fileName, singleton
        ? ".singleton-mpp.o"
        : ".multi-mpp.o"));
    command.addFlags(cppFlags);

    final String name = "macro_def.h";
    final URL url = getClass().getClassLoader().getResource(name);
    if (url == null) throw new Exception("Can't find file " + name);
    command.addIncludeFile(new File(url.toURI()));

    command.addDefine("COMPONENT_NAME", dirName.replace('-', '_'));
    if (singleton) command.addDefine("SINGLETON");

    return command;
  }

  protected void mppSingleton(final String dirName, final String fileName)
      throws Exception {
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, true));
    executor.exec(commands, context);
  }

  protected void mppMulti(final String dirName, final String fileName)
      throws Exception {
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, false));
    executor.exec(commands, context);
  }

  protected void compileSingleton(final String dirName, final String fileName)
      throws Exception {
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, true));
    commands.add(newGCCCommand(dirName, fileName, true));
    executor.exec(commands, context);
  }

  protected void compileMulti(final String dirName, final String fileName)
      throws Exception {
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, false));
    commands.add(newGCCCommand(dirName, fileName, false));
    executor.exec(commands, context);
  }
}
