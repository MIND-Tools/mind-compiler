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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.cecilia.adl.directives.DirectiveHelper;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorException;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorLoader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLBackendFactory;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.DefinitionCompiler;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.GraphCompiler;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.annotation.AnnotationLocatorHelper;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.ow2.mind.idl.IDLBackendFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.ow2.mind.preproc.BasicMPPWrapper;
import org.ow2.mind.preproc.MPPWrapper;
import org.ow2.mind.st.BasicASTTransformer;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateASTTransformer;

public class Launcher extends org.objectweb.fractal.cecilia.adl.Launcher {

  // System property name for external mind annotation packages
  protected static final String          MIND_ANNOTATION_PACKAGES = "mind.annotation.packages";

  // command line options
  protected CmdFlag                      generateDefSrcOpt;

  protected CmdFlag                      compileDefOpt;

  protected boolean                      generateSrc;
  protected boolean                      compileDef;

  // compiler components :
  protected Loader                       adlLoader;
  protected IDLLoader                    idlLoader;
  protected Instantiator                 graphInstantiator;
  protected DefinitionSourceGenerator    definitionSourceGenerator;
  protected DefinitionCompiler           definitionCompiler;
  protected GraphCompiler                graphCompiler;
  protected CompilationCommandExecutor   executor;

  protected StringTemplateASTTransformer astTransformer;

  /**
   * Creates a new Compiler launcher context and run compilation.
   * 
   * @param args the command line arguments.
   * @throws Exception
   */
  public Launcher(final String... args) throws Exception {
    super(args);
  }

  @Override
  protected void init(final String... args) throws InvalidCommandLineException,
      CompilerInstantiationException {
    if (logger.isLoggable(Level.CONFIG)) {
      for (final String arg : args) {
        logger.config("[arg] " + arg);
      }
    }

    generateDefSrcOpt = new CmdFlag("d", "def2c",
        "Only generate source code of the given definitions");

    compileDefOpt = new CmdFlag(
        "D",
        "def2o",
        "Generate and compile source code of the given definitions, do not link an executable application");

    addOptions();

    // parse arguments to a CommandLine.
    final CommandLine cmdLine = CommandLine.parseArgs(options, false, args);

    // If help is asked, print it and exit.
    if (helpOpt.isPresent(cmdLine)) {
      printHelp(System.out);
      System.exit(0);
    }

    // get list of ADL
    final List<String> adlList = cmdLine.getArguments();
    adlToExecName = parserADLList(adlList, cmdLine);

    // add source class loader in context
    final ClassLoader sourceClassLoader = getSourceClassLoader(cmdLine);
    compilerContext.put("classloader", sourceClassLoader);

    // load target descriptor (if any)
    final String targetDesc = targetDescOpt.getValue(cmdLine);
    if (targetDesc != null) {
      final TargetDescriptorLoader loader = createTargetDescriptorLoader(compilerContext);
      try {
        targetDescriptor = loader.load(targetDesc, compilerContext);
      } catch (final TargetDescriptorException e) {
        logger.log(Level.FINE, "Error while loading target descriptor", e);
        throw new InvalidCommandLineException(
            "Unable to load target descriptor: " + e.getMessage(), 1);
      }
    }
    if (targetDescriptor != null && targetDescriptor.getLinkerScript() != null) {
      final URL linkerScriptURL = sourceClassLoader
          .getResource(targetDescriptor.getLinkerScript().getPath());
      if (linkerScriptURL == null) {
        throw new InvalidCommandLineException("Invalid linker script: '"
            + targetDescriptor.getLinkerScript().getPath()
            + "'. Cannot find file in the source path", 1);
      }
      targetDescriptor.getLinkerScript().setPath(linkerScriptURL.getPath());
    }

    printStackTrace = printStackTraceOpt.isPresent(cmdLine);

    checkADLMode = checkADLModeOpt.isPresent(cmdLine);
    generateSrc = generateDefSrcOpt.isPresent(cmdLine);
    compileDef = compileDefOpt.isPresent(cmdLine);

    if ((checkADLMode && generateSrc) || (checkADLMode && compileDef)
        || (generateSrc && compileDef)) {
      if (generateSrc) {
        throw new InvalidCommandLineException("Flags --"
            + checkADLModeOpt.getLongName() + ", --"
            + generateDefSrcOpt.getLongName() + " and --"
            + compileDefOpt.getLongName()
            + " can't be specified simultaneously", 1);
      }
      compileDef = true;
    }

    // add build directories to context
    String optValue = outDirOpt.getValue(cmdLine);
    if (nullOrEmpty(optValue)) {
      throw new InvalidCommandLineException("Invalid output directory ''", 1);
    }
    buildDir = new File(optValue);
    checkDir(buildDir);
    if (!buildDir.exists()) {
      throw new InvalidCommandLineException("Invalid output directory '"
          + optValue + "' does not exist.", 1);
    }
    compilerContext
        .put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);

    // build c-flags
    final List<String> cFlagsList = new ArrayList<String>();
    final List<String> incPaths = new ArrayList<String>();
    if (srcPathOpt.getPathValue(cmdLine) != null) {
      incPaths.addAll(srcPathOpt.getPathValue(cmdLine));
    }
    if (includePathOpt.getValue(cmdLine) != null) {
      incPaths.addAll(includePathOpt.getPathValue(cmdLine));
    }
    incPaths.add(buildDir.getAbsolutePath());

    for (final String inc : incPaths) {
      final File incDir = new File(inc);
      cFlagsList.add("-I");
      cFlagsList.add(incDir.getAbsolutePath());
    }

    optValue = cFlagsOpt.getValue(cmdLine);
    if (!nullOrEmpty(optValue)) {
      cFlagsList.addAll(DirectiveHelper.splitOptionString(optValue));
    }
    CompilerContextHelper.setCFlags(compilerContext, cFlagsList);

    // build ld-flags
    final List<String> ldPaths = ldPathOpt.getPathValue(cmdLine);
    final List<String> ldFlagsList = new ArrayList<String>();
    if (ldPaths != null) {
      for (final String ld : ldPaths) {
        final File ldDir = new File(ld);
        ldFlagsList.add("-L");
        ldFlagsList.add(ldDir.getAbsolutePath());
      }
    }
    optValue = ldFlagsOpt.getValue(cmdLine);
    if (!nullOrEmpty(optValue)) {
      ldFlagsList.addAll(DirectiveHelper.splitOptionString(optValue));
    }
    CompilerContextHelper.setLDFlags(compilerContext, ldFlagsList);

    // add compiler arguments to context

    if (compilerCmdOpt.isPresent(cmdLine)) {
      optValue = compilerCmdOpt.getValue(cmdLine);
      if (optValue.length() == 0)
        throw new InvalidCommandLineException("Invalid compiler ''", 1);
      CompilerContextHelper.setCompilerCommand(compilerContext, optValue);
    }

    if (linkerCmdOpt.isPresent(cmdLine)) {
      optValue = linkerCmdOpt.getValue(cmdLine);
      if (optValue.length() == 0)
        throw new InvalidCommandLineException("Invalid linker ''", 1);
      CompilerContextHelper.setLinkerCommand(compilerContext, optValue);
    }

    if (archiverCmdOpt.isPresent(cmdLine)) {
      optValue = archiverCmdOpt.getValue(cmdLine);
      if (optValue.length() == 0)
        throw new InvalidCommandLineException("Invalid archiver ''", 1);
      compilerContext.put(ARCHIVER_COMMAND, optValue);
    }

    Integer jobs = null;
    try {
      jobs = Integer.decode(concurrentJobCmdOpt.getValue(cmdLine));
    } catch (final NumberFormatException e) {
      throw new InvalidCommandLineException("Invalid jobs value '"
          + concurrentJobCmdOpt.getValue(cmdLine) + "' is not a valid number",
          1);
    }
    compilerContext.put("jobs", jobs);

    // add linker script to the context
    final String linkerScript = linkerScriptOpt.getValue(cmdLine);
    if (linkerScript != null) {
      final URL linkerScriptURL = sourceClassLoader.getResource(linkerScript);
      if (linkerScriptURL == null) {
        throw new InvalidCommandLineException("Invalid linker script: '"
            + linkerScript + "'. Cannot find file in the source path", 1);
      }

      CompilerContextHelper.setLinkerScript(compilerContext, linkerScriptURL
          .getPath());
    }

    AnnotationLocatorHelper
        .addDefaultAnnotationPackage(
            "org.ow2.mind.adl.annotation.predefined",
            compilerContext);

    final String annotationPackages = System
        .getProperty(MIND_ANNOTATION_PACKAGES);
    if (annotationPackages != null) {
      for (final String string : annotationPackages.split(File.pathSeparator)) {
        AnnotationLocatorHelper.addDefaultAnnotationPackage(string,
            compilerContext);
      }
    }
    // initialize compiler
    initCompiler(cmdLine);
  }

  /**
   * @param cmdLine
   */
  protected void initCompiler(final CommandLine cmdLine) {
    // input locators
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final IDLLocator idlLocator = IDLLoaderChainFactory.newLocator();
    final ADLLocator adlLocator = Factory.newLocator();
    final ImplementationLocator implementationLocator = new BasicImplementationLocator();

    // output locator
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();

    // Plugin Manager Components
    final org.objectweb.fractal.adl.Factory pluginFactory = new SimpleClassPluginFactory();

    // compilation task factory
    final CompilerWrapper compilerWrapper = new GccCompilerWrapper();
    final MPPWrapper mppWrapper = new BasicMPPWrapper();

    // String Template Component Loaders
    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    // AST Transformer;
    final BasicASTTransformer basicASTTransformer = new BasicASTTransformer();
    basicASTTransformer.nodeFactoryItf = new STNodeFactoryImpl();
    astTransformer = basicASTTransformer;

    // loader chains
    idlLoader = IDLLoaderChainFactory.newLoader(idlLocator);
    adlLoader = Factory.newLoader(inputResourceLocator, adlLocator, idlLocator,
        idlLoader, pluginFactory);

    // instantiator chain
    graphInstantiator = Factory.newInstantiator(adlLoader);

    // Backend
    final IDLVisitor idlCompiler = IDLBackendFactory
        .newIDLCompiler(idlLoader, inputResourceLocator, outputFileLocator,
            basicASTTransformer, stcLoader);
    definitionSourceGenerator = ADLBackendFactory.newDefinitionSourceGenerator(
        inputResourceLocator, outputFileLocator, idlLoader, idlCompiler,
        basicASTTransformer, stcLoader);

    definitionCompiler = ADLBackendFactory.newDefinitionCompiler(
        definitionSourceGenerator, implementationLocator, outputFileLocator,
        compilerWrapper, mppWrapper);
    graphCompiler = ADLBackendFactory.newGraphCompiler(inputResourceLocator,
        implementationLocator, outputFileLocator, compilerWrapper, mppWrapper,
        definitionCompiler, stcLoader);

    executor = ADLBackendFactory.newCompilationCommandExecutor();
  }

  @Override
  public List<Object> compile() throws ADLException,
      InvalidCommandLineException {
    // Check if at least 1 adlName is specified
    if (adlToExecName.size() == 0) {
      throw new InvalidCommandLineException("no definition name is specified.",
          1);
    }

    final List<Object> result = new ArrayList<Object>();
    for (final Map.Entry<String, String> e : adlToExecName.entrySet()) {
      try {
        compile(e.getKey(), e.getValue(), result);
      } catch (final InterruptedException e1) {
        throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
            "Interrupted while executing compilation tasks");
      }
    }
    return result;
  }

  // TODO change visibility of this method to 'protected' in cecilia Launcher
  protected void compile(String adlName, final String execName,
      final List<Object> result) throws ADLException, InterruptedException {
    final HashMap<Object, Object> contextMap = new HashMap<Object, Object>(
        compilerContext);
    if (execName != null) contextMap.put(EXEC_NAME, execName);

    adlName = processContext(targetDescriptor, adlName, contextMap);

    Definition adlDef = adlLoader.load(adlName, contextMap);

    if (checkADLMode) {
      result.add(adlDef);
      return;
    }

    adlDef = astTransformer.toStringTemplateAST(adlDef);

    if (generateSrc) {
      definitionSourceGenerator.visit(adlDef, contextMap);
      return;
    }

    if (compileDef) {
      final Collection<CompilationCommand> commands = definitionCompiler.visit(
          adlDef, contextMap);
      executor.exec(commands, contextMap);
      for (final CompilationCommand command : commands) {
        if (command instanceof CompilerCommand) {
          result.addAll(command.getOutputFiles());
        }
      }
      return;
    }

    final ComponentGraph graph = graphInstantiator.instantiate(adlDef,
        contextMap);
    final Collection<CompilationCommand> commands = graphCompiler.visit(graph,
        contextMap);
    executor.exec(commands, contextMap);
    for (final CompilationCommand command : commands) {
      if (command instanceof LinkerCommand) {
        result.addAll(command.getOutputFiles());
      }
    }
  }

  @Override
  protected void addOptions() {
    options.addOptions(targetDescOpt, compilerCmdOpt, cFlagsOpt,
        includePathOpt, linkerCmdOpt, ldFlagsOpt, ldPathOpt, linkerScriptOpt,
        concurrentJobCmdOpt, printStackTraceOpt, checkADLModeOpt,
        generateDefSrcOpt, compileDefOpt);
  }

  /**
   * Entry point.
   * 
   * @param args
   */
  public static void main(final String... args) {
    try {
      new Launcher(args);
    } catch (final Exception e) {
      // never append
      e.printStackTrace();
    }
  }
}
