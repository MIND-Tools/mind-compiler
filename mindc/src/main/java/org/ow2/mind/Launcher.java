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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.cli.CmdFlag;
import org.ow2.mind.cli.CmdOption;
import org.ow2.mind.cli.CmdOptionBooleanEvaluator;
import org.ow2.mind.cli.CommandLine;
import org.ow2.mind.cli.CommandLineOptionExtensionHelper;
import org.ow2.mind.cli.CommandOptionHandler;
import org.ow2.mind.cli.InvalidCommandLineException;
import org.ow2.mind.cli.Options;
import org.ow2.mind.cli.PrintStackTraceOptionHandler;
import org.ow2.mind.cli.StageOptionHandler;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.inject.GuiceModuleExtensionHelper;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.ow2.mind.plugin.PluginManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Launcher {

  protected static final String PROGRAM_NAME_PROPERTY_NAME = "mindc.launcher.name";
  protected static final String ID_PREFIX                  = "org.ow2.mind.mindc.";

  protected final CmdFlag       helpOpt                    = new CmdFlag(
                                                               ID_PREFIX
                                                                   + "Help",
                                                               "h", "help",
                                                               "Print this help and exit");

  protected final CmdFlag       versionOpt                 = new CmdFlag(
                                                               ID_PREFIX
                                                                   + "Version",
                                                               "v", "version",
                                                               "Print version number and exit");

  protected final CmdFlag       extensionPointsListOpt     = new CmdFlag(
                                                               ID_PREFIX
                                                                   + "PrintExtensionPoints",
                                                               null,
                                                               "extension-points",
                                                               "Print the list of available extension points and exit.");

  protected final Options       options                    = new Options();

  protected static Logger       logger                     = FractalADLLogManager
                                                               .getLogger("launcher");

  protected Map<String, String> adlToExecName;
  protected Map<Object, Object> compilerContext            = new HashMap<Object, Object>();

  // compiler components :
  protected Injector            injector;

  protected ErrorManager        errorManager;
  protected ADLCompiler         adlCompiler;

  protected void init(final String... args) throws InvalidCommandLineException {
    if (logger.isLoggable(Level.CONFIG)) {
      for (final String arg : args) {
        logger.config("[arg] " + arg);
      }
    }

    /****** Initialization of the PluginManager Component *******/

    final Injector bootStrapPluginManagerInjector = getBootstrapInjector();
    final PluginManager pluginManager = bootStrapPluginManagerInjector
        .getInstance(PluginManager.class);

    addOptions(pluginManager);

    // parse arguments to a CommandLine.
    final CommandLine cmdLine = CommandLine.parseArgs(options, false, args);
    checkExclusiveGroups(pluginManager, cmdLine);
    compilerContext
        .put(CmdOptionBooleanEvaluator.CMD_LINE_CONTEXT_KEY, cmdLine);
    invokeOptionHandlers(pluginManager, cmdLine, compilerContext);

    // If help is asked, print it and exit.
    if (helpOpt.isPresent(cmdLine)) {
      printHelp(System.out);
      System.exit(0);
    }

    // If version is asked, print it and exit.
    if (versionOpt.isPresent(cmdLine)) {
      printVersion(System.out);
      System.exit(0);
    }

    // If the extension points list is asked, print it and exit.
    if (extensionPointsListOpt.isPresent(cmdLine)) {
      printExtensionPoints(pluginManager, System.out);
      System.exit(0);
    }

    // get list of ADL
    adlToExecName = parserADLList(cmdLine.getArguments(), cmdLine);

    // initialize compiler
    initInjector(pluginManager, compilerContext);

    initCompiler();
  }

  protected Injector getBootstrapInjector() {
    return Guice.createInjector(new PluginLoaderModule());
  }

  protected void initCompiler() {
    errorManager = injector.getInstance(ErrorManager.class);
    adlCompiler = injector.getInstance(ADLCompiler.class);
  }

  protected void initInjector(final PluginManager pluginManager,
      final Map<Object, Object> compilerContext) {
    injector = Guice.createInjector(GuiceModuleExtensionHelper.getModules(
        pluginManager, compilerContext));
  }

  protected Map<String, String> parserADLList(final List<String> adlList,
      final CommandLine cmdLine) throws InvalidCommandLineException {
    final Map<String, String> adlToExecName = new LinkedHashMap<String, String>();

    // parse adlNames
    for (final String adlName : adlList) {
      final int i = adlName.indexOf(':');
      if (i == 0) {
        throw new InvalidCommandLineException(
            "no adl name is specified for executable "
                + adlName.substring(i + 1) + ".", 1);
      }
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

  public List<Object> compile(final List<Error> errors,
      final List<Error> warnings) throws InvalidCommandLineException {
    // Check if at least 1 adlName is specified
    if (adlToExecName.size() == 0) {
      throw new InvalidCommandLineException("no definition name is specified.",
          1);
    }

    final List<Object> result = new ArrayList<Object>();
    for (final Map.Entry<String, String> e : adlToExecName.entrySet()) {
      try {
        final HashMap<Object, Object> contextMap = new HashMap<Object, Object>(
            compilerContext);
        final String adlName = e.getKey();
        final String execName = e.getValue();

        final List<Object> l = adlCompiler.compile(adlName, execName,
            StageOptionHandler.getCompilationStage(contextMap), contextMap);
        if (!errorManager.getErrors().isEmpty()) {
          break;
        }

        if (l != null) result.addAll(l);

      } catch (final InterruptedException e1) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Interrupted while executing compilation tasks");
      } catch (final ADLException e1) {
        if (!errorManager.getErrors().contains(e1.getError())) {
          // the error has not been logged in the error manager, print it.
          try {
            errorManager.logError(e1.getError());
          } catch (final ADLException e2) {
            // ignore
          }
        }
      }
    }
    if (errors != null) errors.addAll(errorManager.getErrors());
    if (warnings != null) warnings.addAll(errorManager.getWarnings());
    return result;
  }

  protected void addOptions(final PluginManager pluginManagerItf) {
    options.addOptions(helpOpt, versionOpt, extensionPointsListOpt);

    options.addOptions(CommandLineOptionExtensionHelper
        .getCommandOptions(pluginManagerItf));
  }

  protected void checkExclusiveGroups(final PluginManager pluginManagerItf,
      final CommandLine cmdLine) throws InvalidCommandLineException {
    final Collection<Set<String>> exclusiveGroups = CommandLineOptionExtensionHelper
        .getExclusiveGroups(pluginManagerItf);
    for (final Set<String> exclusiveGroup : exclusiveGroups) {
      CmdOption opt = null;
      for (final String id : exclusiveGroup) {
        final CmdOption opt1 = cmdLine.getOptions().getById(id);
        if (opt1.isPresent(cmdLine)) {
          if (opt != null) {
            throw new InvalidCommandLineException("Options '"
                + opt.getPrototype() + "' and '" + opt1.getPrototype()
                + "' cannot be specified simultaneously on the command line.",
                1);
          }
          opt = opt1;
        }
      }
    }
  }

  protected void invokeOptionHandlers(final PluginManager pluginManagerItf,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    final List<CmdOption> toBeExecuted = new LinkedList<CmdOption>(cmdLine
        .getOptions().getOptions());
    final Set<String> executedId = new HashSet<String>(toBeExecuted.size());
    while (!toBeExecuted.isEmpty()) {
      final int toBeExecutedSize = toBeExecuted.size();
      final Iterator<CmdOption> iter = toBeExecuted.iterator();
      while (iter.hasNext()) {
        final CmdOption option = iter.next();
        final List<String> precedenceIds = CommandLineOptionExtensionHelper
            .getPrecedenceIds(option, pluginManagerItf);
        if (executedId.containsAll(precedenceIds)) {
          // task ready to be executed
          for (final CommandOptionHandler handler : CommandLineOptionExtensionHelper
              .getHandler(option, pluginManagerItf)) {
            handler.processCommandOption(option, cmdLine, context);
          }
          executedId.add(option.getId());
          iter.remove();
        }
      }
      if (toBeExecutedSize == toBeExecuted.size()) {
        // nothing has been executed. there is a circular dependency
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Circular dependency in command line option handlers: "
                + toBeExecuted);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  private void printExtensionPoints(final PluginManager pluginManager,
      final PrintStream out) {
    final Iterable<String> extensionPoints = pluginManager
        .getExtensionPointNames();
    System.out.println("Supported extension points are : ");
    for (final String extensionPoint : extensionPoints) {
      System.out.println("\t'" + extensionPoint + "'");
    }
  }

  protected static void checkDir(final File d)
      throws InvalidCommandLineException {
    if (d.exists() && !d.isDirectory())
      throw new InvalidCommandLineException("Invalid build directory '"
          + d.getAbsolutePath() + "' not a directory", 6);
  }

  protected String getVersion() {
    final String pkgVersion = this.getClass().getPackage()
        .getImplementationVersion();
    return (pkgVersion == null) ? "unknown" : pkgVersion;
  }

  protected String getProgramName() {
    return System.getProperty(PROGRAM_NAME_PROPERTY_NAME, getClass().getName());
  }

  protected void printVersion(final PrintStream ps) {
    ps.println(getProgramName() + " version " + getVersion());
  }

  protected void printHelp(final PrintStream ps) {
    printUsage(ps);
    ps.println();
    ps.println("Available options are :");
    int maxCol = 0;

    for (final CmdOption opt : options.getOptions()) {
      final int col = 2 + opt.getPrototype().length();
      if (col > maxCol) maxCol = col;
    }
    for (final CmdOption opt : options.getOptions()) {
      final StringBuffer sb = new StringBuffer("  ");
      sb.append(opt.getPrototype());
      while (sb.length() < maxCol)
        sb.append(' ');
      sb.append("  ").append(opt.getDescription());
      ps.println(sb);
    }
  }

  protected void printUsage(final PrintStream ps) {
    ps.println("Usage: " + getProgramName()
        + " [OPTIONS] (<definition>[:<execname>])+");
    ps.println("  where <definition> is the name of the component to"
        + " be compiled, ");
    ps.println("  and <execname> is the name of the output file to be created.");
  }

  protected void handleException(final InvalidCommandLineException e) {
    logger.log(Level.FINER, "Caught an InvalidCommandLineException", e);
    if (PrintStackTraceOptionHandler.getPrintStackTrace(compilerContext)) {
      e.printStackTrace();
    } else {
      System.err.println(e.getMessage());
      printHelp(System.err);
      System.exit(e.getExitValue());
    }
  }

  /**
   * Entry point.
   * 
   * @param args
   */
  public static void main(final String... args) {
    final Launcher l = new Launcher();
    try {
      l.init(args);
      l.compile(null, null);
    } catch (final InvalidCommandLineException e) {
      l.handleException(e);
    }
    if (!l.errorManager.getErrors().isEmpty()) System.exit(1);
  }

  public static void nonExitMain(final String... args)
      throws InvalidCommandLineException, ADLException {
    nonExitMain(null, null, args);
  }

  public static void nonExitMain(final List<Error> errors,
      final List<Error> warnings, final String... args)
      throws InvalidCommandLineException, ADLException {
    final Launcher l = new Launcher();
    l.init(args);
    l.compile(errors, warnings);
  }

}
