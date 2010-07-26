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

package org.ow2.mind.compilation;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.error.ErrorManager;

public class BasicCompilationCommandExecutor
    implements
      CompilationCommandExecutor,
      BindingController {

  protected static Logger    depLogger                  = FractalADLLogManager
                                                            .getLogger("dep");

  public static final String CONCURENT_JOBS_CONTEXT_KEY = "jobs";

  public static final String FAIL_FAST_CONTEXT_KEY      = "fail-fast";

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager        errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the CompilationCommandExecutor interface
  // ---------------------------------------------------------------------------

  public boolean exec(final Collection<CompilationCommand> commands,
      final Map<Object, Object> context) throws ADLException,
      InterruptedException {
    final Map<CommandInfo, Collection<CommandInfo>> depGraph = new HashMap<CommandInfo, Collection<CommandInfo>>();
    final LinkedList<CommandInfo> readyTask = new LinkedList<CommandInfo>();
    final boolean force = ForceRegenContextHelper.getForceRegen(context);
    buildDepGraph(commands, depGraph, readyTask, force);

    if (depGraph.isEmpty() && readyTask.isEmpty()) {
      if (depLogger.isLoggable(Level.INFO))
        depLogger.info("Nothing to be done, compiled files are up-to-dates.");
      return true;
    }

    int jobs = 1;
    Object o = context.get(CONCURENT_JOBS_CONTEXT_KEY);
    if (o instanceof Integer) {
      jobs = (Integer) o;
    }
    boolean failFast = false;
    o = context.get(FAIL_FAST_CONTEXT_KEY);
    if (o instanceof Boolean) {
      failFast = (Boolean) o;
    }
    return execDepGraph(jobs, depGraph, readyTask, failFast);
  }

  protected void buildDepGraph(final Collection<CompilationCommand> commands,
      final Map<CommandInfo, Collection<CommandInfo>> depGraph,
      final List<CommandInfo> readyTask, final boolean forced) {

    final Map<CompilationCommand, CommandInfo> cmdInfos = new IdentityHashMap<CompilationCommand, CommandInfo>();
    for (final CompilationCommand cmd : commands) {
      cmd.prepare();
      cmdInfos.put(cmd, new CommandInfo(cmd));
    }

    /*
     * Build the fileProviders map that associates files to the command that
     * produces them
     */
    final Map<File, CommandInfo> fileProducers = new HashMap<File, CommandInfo>();
    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      for (final File outputFile : cmd.getOutputFiles()) {
        final CommandInfo previousProviders = fileProducers.put(outputFile,
            cmdInfo);
        if (previousProviders != null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Multiple provider of the same output-file \"" + outputFile
                  + "\" (" + cmd.getDescription() + " and "
                  + previousProviders.command.getDescription() + ").");
        }
      }
    }

    /*
     * The fileConsumers map associated a File to the tasks that consume it.
     */
    final Map<File, Collection<CommandInfo>> fileConsumers = new HashMap<File, Collection<CommandInfo>>();

    /*
     * Build the depGraph map that associates commands that produce files to the
     * collection of commands that consume them . Use CommandInfo class to store
     * additional info on command dependencies. Build also the readyTask list
     * that contains the commands that are ready to be executed (i.e. the
     * commands that do not depend on other commands).
     */
    for (final CompilationCommand cmd : commands) {
      /*
       * Create a CommandInfo object for the current command. This object
       * contains the number of commands, this one depends on. (i.e the number
       * of commands that must be executed before this one can be executed).
       */
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      for (final File inputFile : cmd.getInputFiles()) {
        /* Fill-in the fileConsumer map. */
        Collection<CommandInfo> fileConsumer = fileConsumers.get(inputFile);
        if (fileConsumer == null) {
          fileConsumer = new ArrayList<CommandInfo>();
          fileConsumers.put(inputFile, fileConsumer);
        }
        fileConsumer.add(cmdInfo);

        /* Find command that provides this input files */
        final CommandInfo provider = fileProducers.get(inputFile);
        if (provider != null) {
          /*
           * Add the current cmd command as a command that depends on the
           * provider command in the depGraph map.
           */
          Collection<CommandInfo> deps = depGraph.get(provider);
          if (deps == null) {
            deps = new LinkedList<CommandInfo>();
            depGraph.put(provider, deps);
          }
          deps.add(cmdInfo);
          /* Add the provider task as a dependency of the current cmd command. */
          cmdInfo.dependencies.add(provider);
        } else if (!inputFile.exists()) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Missing input-file \"" + inputFile
                  + "\" of compilation command : " + cmd.getDescription() + ".");
        }
      }
      if (forced && cmdInfo.dependencies.isEmpty()) {
        /*
         * The current cmd command has no dependency, it is ready to be
         * executed.
         */
        readyTask.add(cmdInfo);
      }
    }

    /* If in forced mode, do not expunge up-to-date tasks, execute all of them. */
    if (forced) return;

    /*
     * Expunge tasks that are up-to-dates. Travel the readyTask list and checks
     * if output files are more recent than input files.
     */
    // cache of output file timestamp
    final Map<File, Long> outputFileTimestamps = new HashMap<File, Long>();
    // cache of input file timestamp
    final Map<File, Long> inputFileTimestamps = new HashMap<File, Long>();

    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      getOutputCommandTimestamp(cmdInfo, fileConsumers, outputFileTimestamps);
    }
    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      getInputCommandTimestamp(cmdInfo, fileProducers, inputFileTimestamps);
    }

    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);

      if (cmdInfo.maxInputTimestamp > cmdInfo.maxOutputTimestamp) {
        cmdInfo.setMustBeExecuted(depGraph, fileProducers);
      }
    }

    final Collection<CommandInfo> expungedTasks = new ArrayList<CommandInfo>();
    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      if (cmdInfo.mustBeExecuted) {
        if (depLogger.isLoggable(Level.FINE))
          depLogger.fine("Task '" + cmdInfo.command.getDescription()
              + "' Input file '" + cmdInfo.maxInputFile
              + "' is more recent than output file '" + cmdInfo.maxOutputFile
              + "', recompile.");
      } else {

        expungedTasks.add(cmdInfo);
        depGraph.remove(cmdInfo);
        if (depLogger.isLoggable(Level.FINE))
          depLogger.fine("Command '" + cmd.getDescription()
              + "' is up to date, do not recompile.");
      }
    }

    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = cmdInfos.get(cmd);
      cmdInfo.dependencies.removeAll(expungedTasks);
      if (cmdInfo.dependencies.isEmpty() && !expungedTasks.contains(cmdInfo)) {
        /*
         * The current cmd command has no dependency, it is ready to be
         * executed.
         */
        readyTask.add(cmdInfo);
      }
    }
  }

  /**
   * Returns the timestamp of the given outputFile. the Timestamp of an
   * outputFile is defines as follow :
   * <ul>
   * <li>If the file exists, its timestamp is the value returned by
   * {@link File#lastModified()}.</li>
   * <li>Otherwise
   * <ul>
   * <li>The timestamp is the maximum timestamp of the consumer commands (the
   * commands that use this file as input file), as defined in
   * {@link #getOutputCommandTimestamp(CommandInfo, Map, Map)}, or zero if one
   * of its consumer command timestamp is null.</li>
   * <li>If the file is a final file (i.e. it is not the input-file of a
   * compilation command), its timestamp is zero (since it does not exist).</li>
   * </ul>
   * </ul>
   */
  protected long getOutputFileTimestamp(final File outputFile,
      final Map<File, Collection<CommandInfo>> fileConsumers,
      final Map<File, Long> outputFileTimestamps) {
    Long ts = outputFileTimestamps.get(outputFile);
    if (ts == null) {
      if (outputFile.exists()) {
        ts = outputFile.lastModified();
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Output file '" + outputFile
              + "' exists, its timestamp is " + ts + ".");

      } else {
        /*
         * the outputFile does not exist. Its timestamp is the max of the
         * timestamps of its consumer commands
         */
        final Collection<CommandInfo> consumers = fileConsumers.get(outputFile);
        if (consumers == null) {
          /*
           * the outputFile is not consumed by any command. It is a final output
           * file that does not exist. Its timestamp is 0.
           */
          ts = 0L;
        } else {
          long maxTs = 0L;
          for (final CommandInfo cmdInfo : consumers) {
            final long cmdTs = getOutputCommandTimestamp(cmdInfo,
                fileConsumers, outputFileTimestamps);
            if (cmdTs == 0L) {
              maxTs = 0L;
              break;
            }
            if (cmdTs > maxTs) {
              maxTs = cmdTs;
            }
          }
          ts = maxTs;
        }
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Output file '" + outputFile
              + "' does not exist, its inferred timestamp is " + ts + ".");

      }
      outputFileTimestamps.put(outputFile, ts);
    }
    return ts;
  }

  protected long getOutputCommandTimestamp(final CommandInfo cmdInfo,
      final Map<File, Collection<CommandInfo>> fileConsumers,
      final Map<File, Long> outputFileTimestamps) {
    if (cmdInfo.maxOutputTimestamp == -1L) {
      if (cmdInfo.command.forceExec()) {
        cmdInfo.maxOutputTimestamp = 0L;
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Task '" + cmdInfo.command.getDescription()
              + "' is forced, set outputTimestamp to 0.");
      } else {
        /* For each file produced by the command */
        for (final File outputFile : cmdInfo.command.getOutputFiles()) {
          /* Get the timestamp of the file. */
          final long outputFileTs = getOutputFileTimestamp(outputFile,
              fileConsumers, outputFileTimestamps);
          if (outputFileTs == 0L) {
            cmdInfo.maxOutputTimestamp = 0L;
            cmdInfo.maxOutputFile = outputFile;
            if (depLogger.isLoggable(Level.FINEST))
              depLogger.finest("Task '" + cmdInfo.command.getDescription()
                  + "' output file '" + outputFile
                  + "' must be regenerated, set output timestamp to 0.");
            break;
          }
          if (outputFileTs > cmdInfo.maxOutputTimestamp) {
            cmdInfo.maxOutputTimestamp = outputFileTs;
            cmdInfo.maxOutputFile = outputFile;
            if (depLogger.isLoggable(Level.FINEST))
              depLogger.finest("Task '" + cmdInfo.command.getDescription()
                  + "' set output timestamp to timestamp of output file '"
                  + outputFile + "' : " + outputFileTs);
          }
        }
      }
    }
    return cmdInfo.maxOutputTimestamp;
  }

  /**
   * Returns the timestamp of the given inputFile. the Timestamp of an inputFile
   * is defines as follow :
   * <ul>
   * <li>If the file exists, its timestamp is the value returned by
   * {@link File#lastModified()}.</li>
   * <li>Otherwise the timestamp is the timestamp of the producer commands (the
   * commands that produce this file as output file), as defined in
   * {@link #getInputCommandTimestamp(CommandInfo, Map, Map)}.</li>
   * </ul>
   */
  protected long getInputFileTimestamp(final File inputFile,
      final Map<File, CommandInfo> fileProducers,
      final Map<File, Long> inputFileTimestamps) {
    Long ts = inputFileTimestamps.get(inputFile);
    if (ts == null) {
      if (inputFile.exists()) {
        ts = inputFile.lastModified();
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Input file '" + inputFile
              + "' exists, its timestamp is " + ts + ".");
      } else {
        /*
         * the inputFile does not exist. Its timestamp is the timestamps of its
         * producer command
         */
        final CommandInfo producer = fileProducers.get(inputFile);
        ts = getInputCommandTimestamp(producer, fileProducers,
            inputFileTimestamps);
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Input file '" + inputFile
              + "' does not exist, its inferred timestamp is " + ts + ".");
      }
      inputFileTimestamps.put(inputFile, ts);
    }
    return ts;
  }

  protected long getInputCommandTimestamp(final CommandInfo cmdInfo,
      final Map<File, CommandInfo> fileProducers,
      final Map<File, Long> inputFileTimestamps) {
    if (cmdInfo.maxInputTimestamp == -1L) {
      if (cmdInfo.command.forceExec()) {
        cmdInfo.maxInputTimestamp = Long.MAX_VALUE;
        if (depLogger.isLoggable(Level.FINEST))
          depLogger.finest("Task '" + cmdInfo.command.getDescription()
              + "' is forced, set inputTimestamp to MAX.");
      } else {
        /* For each file consumed by the command */
        for (final File inputFile : cmdInfo.command.getInputFiles()) {
          /* Get the timestamp of the file. */
          final long inputFileTs = getInputFileTimestamp(inputFile,
              fileProducers, inputFileTimestamps);
          if (inputFileTs > cmdInfo.maxInputTimestamp) {
            cmdInfo.maxInputTimestamp = inputFileTs;
            cmdInfo.maxInputFile = inputFile;
            if (depLogger.isLoggable(Level.FINEST))
              depLogger.finest("Task '" + cmdInfo.command.getDescription()
                  + "' set task input timestamp to timestamp of input file '"
                  + inputFile + "' : " + inputFileTs);
          }
        }
      }
    }
    return cmdInfo.maxInputTimestamp;
  }

  protected boolean execDepGraph(final int nbJobs,
      final Map<CommandInfo, Collection<CommandInfo>> depGraph,
      final LinkedList<CommandInfo> readyTask, final boolean failFast)
      throws ADLException, InterruptedException {
    if (nbJobs == 1) {
      return execDepGraphSynchronous(depGraph, readyTask, failFast);
    } else {
      final ExecutionState exceptionHolder = new ExecutionState(readyTask,
          depGraph, nbJobs, failFast);
      return exceptionHolder.terminate();
    }
  }

  protected boolean execDepGraphSynchronous(
      final Map<CommandInfo, Collection<CommandInfo>> depGraph,
      final LinkedList<CommandInfo> readyTask, final boolean failFast)
      throws ADLException, InterruptedException {
    boolean result = true;
    while (!readyTask.isEmpty()) {
      final CommandInfo cmdInfo = readyTask.removeFirst();

      boolean execOK;
      try {
        execOK = cmdInfo.command.exec();
      } catch (final ADLException e) {
        execOK = false;
      }
      if (execOK) {
        commandEnded(cmdInfo, depGraph, readyTask);
      } else {
        commandFailed(cmdInfo, depGraph, readyTask);
        result = false;
        if (failFast) return result;
      }
    }
    assert depGraph.isEmpty();
    return result;
  }

  protected boolean commandEnded(final CommandInfo cmd,
      final Map<CommandInfo, Collection<CommandInfo>> depGraph,
      final LinkedList<CommandInfo> readyTask) {
    boolean commandUnlocked = false;
    final Collection<CommandInfo> deps = depGraph.remove(cmd);
    if (deps != null) {
      final Iterator<CommandInfo> iter = deps.iterator();
      while (iter.hasNext()) {
        final CommandInfo cmdInfo = iter.next();
        assert cmdInfo.dependencies.contains(cmd);
        cmdInfo.dependencies.remove(cmd);
        if (cmdInfo.dependencies.isEmpty()) {
          // every dependency of command are done, place it in ready task.
          iter.remove();
          readyTask.addLast(cmdInfo);
          commandUnlocked = true;
        }
      }
    }
    return commandUnlocked;
  }

  protected void commandFailed(final CommandInfo cmd,
      final Map<CommandInfo, Collection<CommandInfo>> depGraph,
      final LinkedList<CommandInfo> readyTask) {
    final Collection<CommandInfo> deps = depGraph.remove(cmd);
    if (deps != null) {
      for (final CommandInfo cmdInfo : deps) {
        commandFailed(cmdInfo, depGraph, readyTask);
      }
    }
  }

  protected final class ExecutionState {
    final LinkedList<CommandInfo>                   readyTask;
    final Map<CommandInfo, Collection<CommandInfo>> depGraph;
    final boolean                                   failFast;
    boolean                                         result    = true;

    final Lock                                      lock      = new ReentrantLock();
    Condition                                       condition = lock
                                                                  .newCondition();
    Exception                                       exception;
    int                                             nbRunningThread;

    ExecutionState(final LinkedList<CommandInfo> readyTask,
        final Map<CommandInfo, Collection<CommandInfo>> depGraph,
        final int nbJob, final boolean failFast) {
      this.readyTask = readyTask;
      this.depGraph = depGraph;
      this.failFast = failFast;

      lock.lock();
      try {
        for (int i = 0; i < nbJob; i++) {
          new Thread() {
            @Override
            public void run() {
              work();
            }
          }.start();
        }
        nbRunningThread = nbJob;
      } finally {
        lock.unlock();
      }
    }

    void work() {
      lock.lock();
      try {
        // execute a command if :
        // - a task is ready to be executed
        // - AND
        // * we are not in fail-fast mode
        // * OR we are in fail-fast mode AND there is no error (i.e.
        // result == true AND exception == null)
        //
        while (!readyTask.isEmpty()
            && (!failFast || (result && exception == null))) {
          final CommandInfo cmdInfo = readyTask.removeFirst();
          lock.unlock();

          boolean execOK;
          try {
            execOK = cmdInfo.command.exec();
          } catch (final ADLException e) {
            execOK = false;
          } catch (final Exception e) {
            lock.lock();
            if (exception != null) {
              exception = e;
            }
            break;
          }
          if (!execOK) {
            lock.lock();
            commandFailed(cmdInfo, depGraph, readyTask);
            result = false;
            if (failFast) {
              break;
            } else {
              continue;
            }
          }

          lock.lock();
          commandEnded(cmdInfo, depGraph, readyTask);
        }
      } finally {
        nbRunningThread--;
        if (nbRunningThread <= 0) {
          condition.signalAll();
        }
        lock.unlock();
      }
    }

    boolean terminate() throws ADLException, InterruptedException {
      lock.lock();
      try {
        while (nbRunningThread > 0) {
          condition.await();
        }
      } finally {
        lock.unlock();
      }
      if (exception != null) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, exception,
            "Unexpected error");
      }
      return result;
    }
  }

  protected static final class CommandInfo {
    final CompilationCommand command;
    Collection<CommandInfo>  dependencies       = new ArrayList<CommandInfo>();
    File                     maxOutputFile;
    long                     maxOutputTimestamp = -1L;
    File                     maxInputFile;
    long                     maxInputTimestamp  = -1L;
    boolean                  mustBeExecuted     = false;

    CommandInfo(final CompilationCommand command) {
      this.command = command;
    }

    void setMustBeExecuted(
        final Map<CommandInfo, Collection<CommandInfo>> depGraph,
        final Map<File, CommandInfo> fileProducers) {
      if (!mustBeExecuted) {
        this.mustBeExecuted = true;
        final Collection<CommandInfo> cmds = depGraph.get(this);
        if (cmds != null) {
          for (final CommandInfo cmdInfo : cmds) {
            cmdInfo.setMustBeExecuted(depGraph, fileProducers);
          }
        }

        for (final File inputFile : command.getInputFiles()) {
          if (!inputFile.exists()) {
            /*
             * The inputFile in not present, the task that produces it must be
             * executed also.
             */
            fileProducers.get(inputFile).setMustBeExecuted(depGraph,
                fileProducers);
          }
        }
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      return errorManagerItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      errorManagerItf = (ErrorManager) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      errorManagerItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }
}
