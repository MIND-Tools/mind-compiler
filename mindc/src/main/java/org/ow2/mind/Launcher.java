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
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.JavaFactory;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorException;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorLoader;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorLoaderJavaFactory;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.ADLMapping;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.CFlag;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.LdFlag;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.Target;
import org.ow2.mind.adl.ADLBackendFactory;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.DefinitionCompiler;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.GraphCompiler;
import org.ow2.mind.adl.OutputBinaryADLLocator;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.st.ADLLoaderASTTransformer;
import org.ow2.mind.annotation.AnnotationLocatorHelper;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.DirectiveHelper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.ow2.mind.idl.IDLBackendFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.idl.OutputBinaryIDLLocator;
import org.ow2.mind.idl.st.IDLLoaderASTTransformer;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.ow2.mind.preproc.BasicMPPWrapper;
import org.ow2.mind.preproc.MPPWrapper;
import org.ow2.mind.st.BasicASTTransformer;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateASTTransformer;

public class Launcher extends AbstractLauncher {

  protected static final String          PROGRAM_NAME_PROPERTY_NAME = "cecilia.launcher.name";

  // TODO use helper methods for this context entry
  public static final String             EXEC_NAME                  = "executable-name";

  // System property name for external mind annotation packages
  protected static final String          MIND_ANNOTATION_PACKAGES   = "mind.annotation.packages";

  protected final CmdArgument            targetDescOpt              = new CmdArgument(
                                                                        "t",
                                                                        "target-descriptor",
                                                                        "Specify the target descriptor",
                                                                        "<name>");

  protected final CmdArgument            compilerCmdOpt             = new CmdArgument(
                                                                        null,
                                                                        "compiler-command",
                                                                        "the command of the C compiler",
                                                                        "<path>",
                                                                        "gcc",
                                                                        false);

  protected final CmdAppendOption        cFlagsOpt                  = new CmdAppendOption(
                                                                        "c",
                                                                        "c-flags",
                                                                        "the c-flags compiler directives",
                                                                        "<flags>");

  protected final CmdPathOption          includePathOpt             = new CmdPathOption(
                                                                        "I",
                                                                        "inc-path",
                                                                        "the list of path to be added in compiler include paths",
                                                                        "<path list>");

  protected final CmdArgument            linkerCmdOpt               = new CmdArgument(
                                                                        null,
                                                                        "linker-command",
                                                                        "the command of the linker",
                                                                        "<path>",
                                                                        "gcc",
                                                                        false);

  protected final CmdAppendOption        ldFlagsOpt                 = new CmdAppendOption(
                                                                        "l",
                                                                        "ld-flags",
                                                                        "the ld-flags compiler directives",
                                                                        "<flags>");

  protected final CmdPathOption          ldPathOpt                  = new CmdPathOption(
                                                                        "L",
                                                                        "ld-path",
                                                                        "the list of path to be added to linker library search path",
                                                                        "<path list>");

  protected final CmdArgument            linkerScriptOpt            = new CmdArgument(
                                                                        "T",
                                                                        "linker-script",
                                                                        "linker script to use (given path is resolved in source path)",
                                                                        "<path>");

  protected final CmdArgument            concurrentJobCmdOpt        = new CmdArgument(
                                                                        "j",
                                                                        "jobs",
                                                                        "The number of concurrent compilation jobs",
                                                                        "<number>",
                                                                        "1",
                                                                        false);

  protected final CmdFlag                printStackTraceOpt         = new CmdFlag(
                                                                        "e",
                                                                        null,
                                                                        "Print error stack traces");

  protected final CmdFlag                checkADLModeOpt            = new CmdFlag(
                                                                        null,
                                                                        "check-adl",
                                                                        "Only check input ADL(s), do not compile");

  // command line options
  protected final CmdFlag                generateDefSrcOpt          = new CmdFlag(
                                                                        "d",
                                                                        "def2c",
                                                                        "Only generate source code of the given definitions");                                               ;

  protected final CmdFlag                compileDefOpt              = new CmdFlag(
                                                                        "D",
                                                                        "def2o",
                                                                        "Generate and compile source code of the given definitions, do not link an executable application");

  protected final CmdFlag                forceOpt                   = new CmdFlag(
                                                                        "F",
                                                                        "force",
                                                                        "Force the regeneration and the recompilation of every output files");

  protected final CmdFlag                keepTempOpt                = new CmdFlag(
                                                                        "K",
                                                                        "keep",
                                                                        "Keep temporary output files in default output directory");

  protected final CmdFlag                noBinASTOpt                = new CmdFlag(
                                                                        "B",
                                                                        "no-bin",
                                                                        "Do not generate binary ADL/IDL ('.def', '.itfdef' and '.idtdef' files).");

  protected boolean                      generateSrc;
  protected boolean                      compileDef;

  protected static Logger                logger                     = FractalADLLogManager
                                                                        .getLogger("launcher");

  protected Map<String, String>          adlToExecName;
  protected Map<Object, Object>          compilerContext            = new HashMap<Object, Object>();

  protected Target                       targetDescriptor;

  protected boolean                      printStackTrace            = false;

  protected boolean                      checkADLMode               = false;

  protected File                         buildDir;

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
    try {
      init(args);
      compile();
    } catch (final InvalidCommandLineException e) {
      handleException(e);
    } catch (final CompilerInstantiationException e) {
      handleException(e);
    } catch (final ADLException e) {
      handleException(e);
    }
  }

  protected void init(final String... args) throws InvalidCommandLineException,
      CompilerInstantiationException {
    if (logger.isLoggable(Level.CONFIG)) {
      for (final String arg : args) {
        logger.config("[arg] " + arg);
      }
    }

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

    // force mode
    ForceRegenContextHelper.setForceRegen(compilerContext, forceOpt
        .isPresent(cmdLine));
    ForceRegenContextHelper.setKeepTemp(compilerContext, keepTempOpt
        .isPresent(cmdLine));
    ForceRegenContextHelper.setNoBinaryAST(compilerContext, noBinASTOpt
        .isPresent(cmdLine));

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

    AnnotationLocatorHelper.addDefaultAnnotationPackage(
        "org.ow2.mind.adl.annotation.predefined", compilerContext);

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
    final OutputBinaryIDLLocator obil = new OutputBinaryIDLLocator();
    obil.clientLocatorItf = IDLLoaderChainFactory
        .newIDLLocator(inputResourceLocator);
    final IDLLocator idlLocator = obil;
    final ImplementationLocator implementationLocator = new BasicImplementationLocator();

    final ADLLocator frontendLocator = Factory
        .newADLLocator(inputResourceLocator);
    final OutputBinaryADLLocator obal = new OutputBinaryADLLocator();
    obal.clientLocatorItf = frontendLocator;
    final ADLLocator adlLocator = obal;

    // output locator
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();
    obal.outputFileLocatorItf = outputFileLocator;
    obil.outputFileLocatorItf = outputFileLocator;

    // Plugin Manager Components
    final org.objectweb.fractal.adl.Factory pluginFactory = new SimpleClassPluginFactory();

    // compilation task factory
    final GccCompilerWrapper gcw = new GccCompilerWrapper();
    gcw.outputFileLocatorItf = outputFileLocator;
    final CompilerWrapper compilerWrapper = gcw;
    final MPPWrapper mppWrapper = new BasicMPPWrapper();

    // String Template Component Loaders
    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    // AST Transformer;
    final BasicASTTransformer basicASTTransformer = new BasicASTTransformer();
    basicASTTransformer.nodeFactoryItf = new STNodeFactoryImpl();
    astTransformer = basicASTTransformer;

    // loader chains
    final IDLLoaderASTTransformer ilat = new IDLLoaderASTTransformer();
    ilat.clientIDLLoaderItf = IDLLoaderChainFactory.newLoader(idlLocator,
        inputResourceLocator);
    ilat.astTransformerItf = astTransformer;
    idlLoader = ilat;

    final ADLLoaderASTTransformer alat = new ADLLoaderASTTransformer();
    alat.clientLoader = Factory.newLoader(inputResourceLocator, adlLocator,
        idlLocator, implementationLocator, idlLoader, pluginFactory);
    alat.astTransformerItf = astTransformer;
    adlLoader = alat;

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

  protected TargetDescriptorLoader createTargetDescriptorLoader(
      final Map<Object, Object> compilerContext)
      throws CompilerInstantiationException {
    try {
      final JavaFactory factory = new TargetDescriptorLoaderJavaFactory();
      final Map<?, ?> component = (Map<?, ?>) factory.newComponent();
      return (TargetDescriptorLoader) component.get("loader");
    } catch (final Exception e) {
      throw new CompilerInstantiationException(
          "Unable to instantiate target descriptor loader", e, 101);
    }
  }

  protected Map<String, String> parserADLList(final List<String> adlList,
      final CommandLine cmdLine) throws InvalidCommandLineException {
    final Map<String, String> adlToExecName = new LinkedHashMap<String, String>();

    // parse adlNames
    for (final String adlName : adlList) {
      final int i = adlName.indexOf(':');
      if (i == -1) {
        adlToExecName.put(adlName, null);
      } else {
        final String adl = adlName.substring(0, i);
        final String exec = adlName.substring(i + 1);
        adlToExecName.put(adl, exec);
      }
    }

    return adlToExecName;
  }

  protected String processContext(final Target targetDesc,
      final String inputADL, final Map<Object, Object> context) {
    processCFlags(targetDesc, context);
    processLdFlags(targetDesc, context);
    processCompiler(targetDesc, context);
    processLinker(targetDesc, context);
    processLinkerScript(targetDesc, context);
    return processADLMapping(targetDesc, inputADL, context);
  }

  protected void processCFlags(final Target target,
      final Map<Object, Object> context) {
    if (target != null && target.getCFlags().length > 0) {
      final CFlag[] flags = target.getCFlags();

      final List<String> targetFlags = new ArrayList<String>();
      for (final CFlag flag : flags) {
        targetFlags.addAll(DirectiveHelper.splitOptionString(flag.getValue()));
      }

      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Adding target c-flags: " + targetFlags);

      CompilerContextHelper.getCFlags(context);
      List<String> contextFlags = CompilerContextHelper.getCFlags(context);
      ;
      if (contextFlags == null) {
        contextFlags = new ArrayList<String>();
      }
      contextFlags.addAll(targetFlags);
      CompilerContextHelper.setCFlags(context, contextFlags);
    }
  }

  protected void processLdFlags(final Target target,
      final Map<Object, Object> context) {
    if (target != null && target.getLdFlags().length > 0) {
      final LdFlag[] flags = target.getLdFlags();

      final List<String> targetFlags = new ArrayList<String>();
      for (final LdFlag flag : flags) {
        targetFlags.addAll(DirectiveHelper.splitOptionString(flag.getValue()));
      }

      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Adding target ld-flags: " + targetFlags);

      List<String> contextFlags = CompilerContextHelper.getLDFlags(context);
      if (contextFlags == null) {
        contextFlags = new ArrayList<String>();
      }
      contextFlags.addAll(targetFlags);
      CompilerContextHelper.setLDFlags(context, contextFlags);
    }
  }

  protected void processCompiler(final Target target,
      final Map<Object, Object> context) {
    final String opt = CompilerContextHelper.getCompilerCommand(context);
    if (opt == CompilerContextHelper.DEFAULT_COMPILER_COMMAND) {
      if (target != null && target.getCompiler() != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Using target compiler : "
              + target.getCompiler().getPath());
        }
        CompilerContextHelper.setCompilerCommand(context, target.getCompiler()
            .getPath());
      } else {
        CompilerContextHelper.setCompilerCommand(context, compilerCmdOpt
            .getDefaultValue());
      }
    }
  }

  protected void processLinker(final Target target,
      final Map<Object, Object> context) {
    final String opt = CompilerContextHelper.getLinkerCommand(context);
    if (opt == CompilerContextHelper.DEFAULT_LINKER_COMMAND) {
      if (target != null && target.getLinker() != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Using target linker : "
              + target.getLinker().getPath());
        }
        CompilerContextHelper.setLinkerCommand(context, target.getLinker()
            .getPath());
      } else {
        CompilerContextHelper.setLinkerCommand(context, linkerCmdOpt
            .getDefaultValue());
      }
    }
  }

  protected void processLinkerScript(final Target target,
      final Map<Object, Object> context) {
    if (target != null) {
      final String opt = CompilerContextHelper.getLinkerScript(context);
      if (opt == null && target.getLinkerScript() != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Using target linker script : "
              + target.getLinkerScript().getPath());
        }
        CompilerContextHelper.setLinkerScript(context, target.getLinkerScript()
            .getPath());
      }
    }
  }

  protected String processADLMapping(final Target target,
      final String inputADL, final Map<Object, Object> context) {
    if (target != null) {
      final ADLMapping mapping = target.getAdlMapping();
      if (mapping == null) return inputADL;

      if (mapping.getOutputName() != null) {
        final String outputName = ((String) context.get(EXEC_NAME)).replace(
            "${inputADL}", inputADL);
        if (logger.isLoggable(Level.FINE)) {
          logger.log(Level.FINE, "Compiling ADL : " + outputName);
        }
        context.put(EXEC_NAME, outputName);
      }

      return mapping.getMapping().replace("${inputADL}", inputADL);
    } else {
      return inputADL;
    }
  }

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

  protected void addOptions() {
    options.addOptions(targetDescOpt, compilerCmdOpt, cFlagsOpt,
        includePathOpt, linkerCmdOpt, ldFlagsOpt, ldPathOpt, linkerScriptOpt,
        concurrentJobCmdOpt, printStackTraceOpt, checkADLModeOpt,
        generateDefSrcOpt, compileDefOpt, forceOpt, keepTempOpt, noBinASTOpt);
  }

  @Override
  protected void printUsage(final PrintStream ps) {
    final String prgName = System.getProperty(PROGRAM_NAME_PROPERTY_NAME,
        getClass().getName());
    ps.println("Usage: " + prgName + " [OPTIONS] (<definition>[:<execname>])+");
    ps.println("  where <definition> is the name of the component to"
        + " be compiled, ");
    ps
        .println("  and <execname> is the name of the output file to be created.");
  }

  protected void handleException(final InvalidCommandLineException e)
      throws Exception {
    logger.log(Level.FINER, "Caught an InvalidCommandLineException", e);
    if (printStackTrace) {
      e.printStackTrace();
    } else {
      System.err.println(e.getMessage());
      printHelp(System.err);
      System.exit(e.exitValue);
    }
  }

  protected void handleException(final CompilerInstantiationException e)
      throws Exception {
    logger.log(Level.FINER, "Caught a CompilerInstantiationException", e);
    e.printStackTrace();
    System.exit(e.exitValue);
  }

  protected void handleException(final ADLException e) throws Exception {
    logger.log(Level.FINER, "Caught an ADL Exception", e);
    if (printStackTrace) {
      e.printStackTrace();
    } else {
      final StringBuffer sb = new StringBuffer();
      sb.append(e.getMessage()).append('\n');
      Throwable cause = e.getCause();
      while (cause != null) {
        sb.append("caused by : ");
        sb.append(cause.getMessage()).append('\n');
        cause = cause.getCause();
      }
      System.err.println(sb);
    }
    System.exit(1);
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

  protected static boolean nullOrEmpty(final String s) {
    return s == null || s.length() == 0;
  }

  /**
   * Exception thrown when the compiler can't be instantiated.
   */
  public static class CompilerInstantiationException extends Exception {

    final int exitValue;

    /**
     * @param message detail message.
     * @param cause cause.
     * @param exitValue exit value.
     */
    public CompilerInstantiationException(final String message,
        final Throwable cause, final int exitValue) {
      super(message, cause);
      this.exitValue = exitValue;
    }

  }

}
