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

import static org.ow2.mind.compilation.DirectiveHelper.splitOptionString;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.CommonBackendModule;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLFrontendModule;
import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeTest;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AbstractTestMPP {

  protected static final String        DEFAULT_CPPFLAGS = "-g -Wall -Wredundant-decls -Wunreachable-code";

  protected ErrorManager               errorManager;
  protected MPPWrapper                 mppWrapper;
  protected CompilerWrapper            compilerWrapper;
  protected CompilationCommandExecutor executor;

  protected Loader                     adlLoader;

  protected Map<Object, Object>        context;
  protected File                       buildDir;
  protected List<String>               cppFlags;
  protected List<String>               cFlags;

  @BeforeTest(alwaysRun = true)
  public void setUp() {
    final Injector injector = Guice.createInjector(new ADLFrontendModule(),
        new IDLFrontendModule(), new CommonFrontendModule(),
        new PluginLoaderModule(), new CommonBackendModule(), new MPPModule());
    errorManager = injector.getInstance(ErrorManager.class);
    mppWrapper = injector.getInstance(MPPWrapper.class);
    compilerWrapper = injector.getInstance(CompilerWrapper.class);
    executor = injector.getInstance(CompilationCommandExecutor.class);
    adlLoader = injector.getInstance(Loader.class);

    context = new HashMap<Object, Object>();
    final String buildDirName = "target" + File.separator + "build";
    buildDir = new File(buildDirName);
    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);
    context.put(BasicCompilationCommandExecutor.FAIL_FAST_CONTEXT_KEY,
        Boolean.TRUE);

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
    final MPPCommand command = mppWrapper.newMPPCommand(null, context);
    command.setInputFile(newBuildFile(dirName, fileName, ".i"));
    command.setOutputFile(newBuildFile(dirName, fileName, singleton
        ? ".singleton-mpp.c"
        : ".multi-mpp.c"));
    if (singleton) command.setSingletonMode();
    return command;
  }

  protected MPPCommand newMPPCommandForDef(final String dirName,
      final String fileName, final Definition hostDefinition,
      final boolean singleton) {
    final MPPCommand command = mppWrapper
        .newMPPCommand(hostDefinition, context);
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
    errorManager.clear();
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, true));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void mppMulti(final String dirName, final String fileName)
      throws Exception {
    errorManager.clear();
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, false));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void compileSingleton(final String dirName, final String fileName)
      throws Exception {
    errorManager.clear();
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, true));
    commands.add(newGCCCommand(dirName, fileName, true));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void compileMulti(final String dirName, final String fileName)
      throws Exception {
    errorManager.clear();
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommand(dirName, fileName, false));
    commands.add(newGCCCommand(dirName, fileName, false));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void compileSingletonForDef(final String dirName,
      final String fileName, final String hostDefName) throws Exception {
    errorManager.clear();
    final Definition hostDefinition = adlLoader.load(hostDefName, context);
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName));
    commands.add(newMPPCommandForDef(dirName, fileName, hostDefinition, true));
    commands.add(newGCCCommand(dirName, fileName, true));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void compileSplitSingletonForDef(final String dirName,
      final String fileName0, final String fileName1, final String hostDefName)
      throws Exception {
    errorManager.clear();
    final Definition hostDefinition = adlLoader.load(hostDefName, context);
    final Collection<CompilationCommand> commands = new ArrayList<CompilationCommand>();
    commands.add(newCPPCommand(dirName, fileName0));
    commands.add(newCPPCommand(dirName, fileName1));
    commands.add(newMPPCommandForDef(dirName, fileName0, hostDefinition, true));
    commands.add(newMPPCommandForDef(dirName, fileName1, hostDefinition, true));
    commands.add(newGCCCommand(dirName, fileName0, true));
    commands.add(newGCCCommand(dirName, fileName1, true));
    executor.exec(commands, context);
    final List<Error> errors = errorManager.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }
  }

  protected void initSourcePath(final String... rootDirs) {
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
    final ClassLoader srcLoader = new URLClassLoader(
        rootDirList.toArray(new URL[0]), null);

    context.put("classloader", srcLoader);
  }

  protected File getDepsDir(final String resource) {
    try {
      return DepsHelper.unpackDeps(resource, this.getClass().getClassLoader());
    } catch (final Exception e) {
      fail("Can't unpack dependency containing " + resource, e);
      return null;
    }
  }
}
