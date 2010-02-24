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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;

public class BasicCompilationCommandExecutor
    implements
      CompilationCommandExecutor {

  protected static Logger    depLogger                  = FractalADLLogManager
                                                            .getLogger("dep");

  public static final String CONCURENT_JOBS_CONTEXT_KEY = "jobs";

  // ---------------------------------------------------------------------------
  // Implementation of the CompilationCommandExecutor interface
  // ---------------------------------------------------------------------------

  public void exec(final Collection<CompilationCommand> commands,
      final Map<Object, Object> context) throws ADLException,
      InterruptedException {
    final Map<CompilationCommand, Collection<CommandInfo>> depGraph = new HashMap<CompilationCommand, Collection<CommandInfo>>();
    final LinkedList<CompilationCommand> readyTask = new LinkedList<CompilationCommand>();
    final boolean force = ForceRegenContextHelper.getForceRegen(context);
    buildDepGraph(commands, depGraph, readyTask, force);

    int jobs = 1;
    final Object o = context.get(CONCURENT_JOBS_CONTEXT_KEY);
    if (o instanceof Integer) {
      jobs = (Integer) o;
    }
    execDepGraph(jobs, depGraph, readyTask, force);
  }

  protected void buildDepGraph(final Collection<CompilationCommand> commands,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTasks, final boolean forced) {

    final Map<CompilationCommand, CommandInfo> cmdInfos = new IdentityHashMap<CompilationCommand, CommandInfo>();
    for (final CompilationCommand cmd : commands) {
      cmd.prepare();
      cmdInfos.put(cmd, new CommandInfo(cmd));
    }

    /*
     * Build the fileProviders map that associates files to the command that
     * produces them
     */
    final Map<File, CompilationCommand> fileProviders = new HashMap<File, CompilationCommand>();
    for (final CompilationCommand cmd : commands) {
      for (final File outputFile : cmd.getOutputFiles()) {
        final CompilationCommand previousProviders = fileProviders.put(
            outputFile, cmd);
        if (previousProviders != null) {
          if (depLogger.isLoggable(Level.WARNING))
            depLogger.warning("Mmultiple provider of the same output-file \""
                + outputFile + "\" (" + cmd.getDescription() + " and "
                + previousProviders.getDescription() + ").");
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
        final CompilationCommand provider = fileProviders.get(inputFile);
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
        }
      }
      if (cmdInfo.dependencies.isEmpty()) {
        /*
         * The current cmd command has no dependency, it is ready to be
         * executed.
         */
        readyTasks.addLast(cmd);

        /* Check that every input files of this command are actually available. */
        for (final File inputFile : cmd.getInputFiles()) {
          if (!inputFile.exists()) {
            if (depLogger.isLoggable(Level.WARNING))
              depLogger
                  .warning("Missing input-file \"" + inputFile
                      + "\" of compilation command : " + cmd.getDescription()
                      + ".");
          }
        }
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

    // set of ready tasks that are known as task that must be executed.
    final Set<CompilationCommand> readyTaskToBeExecuted = new HashSet<CompilationCommand>();

    // Iterate until no more command are expunged.
    boolean commandExpunged;
    do {
      commandExpunged = false;

      // for each task in readyTask list.
      final Iterator<CompilationCommand> iter = readyTasks.iterator();
      while (iter.hasNext()) {
        final CompilationCommand readyTask = iter.next();
        // if the task is known as a task that must be executed, pass it.
        if (readyTaskToBeExecuted.contains(readyTask)) continue;

        final CommandInfo cmdInfo = cmdInfos.get(readyTask);

        // get the output timestamp of the command.
        final long cmdTs = getCommandTimestamp(cmdInfo, fileConsumers,
            outputFileTimestamps);
        boolean mustBeExceuted;
        if (cmdTs == 0L) {
          /*
           * the timestamp of the task is null (one of its direct or indirect
           * output file is missing). the task must be executed.
           */
          mustBeExceuted = true;
        } else {
          /*
           * Check if an input file as a timestamp greater than the task
           * timestamp.
           */
          mustBeExceuted = false;
          long maxInputTs = 0L;
          for (final File inputFile : cmdInfo.command.getInputFiles()) {
            /* Look into the inputFileTimestamps cache */
            Long inputTs = inputFileTimestamps.get(inputFile);
            if (inputTs == null) {
              if (!inputFile.exists()) {
                if (depLogger.isLoggable(Level.WARNING))
                  depLogger.warning("Missing input-file \"" + inputFile
                      + "\" of compilation command : "
                      + readyTask.getDescription() + ".");
                break;
              } else {
                inputTs = inputFile.lastModified();
                if (inputTs > cmdTs) {
                  /*
                   * inputFile is more recent than the output files of the task.
                   * the task must be executed
                   */
                  if (depLogger.isLoggable(Level.FINE))
                    depLogger.fine("Input file '" + inputFile
                        + "' is more recent than output files of task '"
                        + readyTask.getDescription() + "', recompile.");
                  mustBeExceuted = true;
                }
              }
              // put the input file timestamp in cache
              inputFileTimestamps.put(inputFile, inputTs);
              if (inputTs > maxInputTs) {
                maxInputTs = inputTs;
              }
            }
          }
          if (!mustBeExceuted) {
            /*
             * The task do not have to be executed (its output files are
             * up-to-date). It will be removed from the readyTask, and the tasks
             * that depends on it may become ready to be executed. So
             * output-files of this task will become input files of other task.
             * But these files may not exists (they may be intermediate
             * temporary files), so the "input file timestamp" of these files is
             * the greatest timestamp of the input files of the current task.
             * Put this timestamp in the input file timestamp cache for these
             * files.
             */
            for (final File outputFile : readyTask.getOutputFiles()) {
              if (!outputFile.exists()) {
                inputFileTimestamps.put(outputFile, maxInputTs);
              }
            }
          }
        }
        if (mustBeExceuted) {
          /*
           * The task is known as a task that must be executed (it is
           * out-of-date).
           */
          readyTaskToBeExecuted.add(readyTask);
        } else {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Command '" + readyTask.getDescription()
                + "' is up to date, do not recompile.");
          /*
           * The task is up-to-date. It is not necessary to execute it. Remove
           * it for the readyTasks list.
           */
          iter.remove();
          commandExpunged = true;
          /*
           * Call the commandEnded method to update the readyTasks list knowing
           * that the current task do not have to be executed (i.e. it has
           * already be executed)
           */
          if (commandEnded(readyTask, depGraph, readyTasks)) {
            /*
             * Break the current loop on readyTasks list since it has been
             * updated by the commandEnded method.
             */
            break;
          }
        }
      }

    } while (commandExpunged == true);
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
   * {@link #getCommandTimestamp(CommandInfo, Map, Map)}, or zero if one of its
   * consumer command timestamp is null.</li>
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
            final long cmdTs = getCommandTimestamp(cmdInfo, fileConsumers,
                outputFileTimestamps);
            if (cmdTs == 0L) {
              maxTs = 0L;
              break;
            }
            if (cmdTs > maxTs) {
              maxTs = cmdTs;
            }
          }
        }
      }
      outputFileTimestamps.put(outputFile, ts);
    }
    return ts;
  }

  protected long getCommandTimestamp(final CommandInfo cmdInfo,
      final Map<File, Collection<CommandInfo>> fileConsumers,
      final Map<File, Long> outputFileTimestamps) {
    if (cmdInfo.timestamp == -1L) {
      if (cmdInfo.command.forceExec()) {
        cmdInfo.timestamp = 0L;
      } else {
        /* For each file produced by the command */
        for (final File outputFile : cmdInfo.command.getOutputFiles()) {
          /* Get the timestamp of the file. */
          final long outputFileTs = getOutputFileTimestamp(outputFile,
              fileConsumers, outputFileTimestamps);
          if (outputFileTs == 0L) {
            cmdInfo.timestamp = 0L;
            break;
          }
          if (outputFileTs > cmdInfo.timestamp) {
            cmdInfo.timestamp = outputFileTs;
          }
        }
      }
    }
    return cmdInfo.timestamp;
  }

  protected void execDepGraph(final int nbJobs,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask, final boolean force)
      throws ADLException, InterruptedException {
    if (nbJobs == 1) {
      execDepGraphSynchronous(depGraph, readyTask, force);
    } else {
      final ExecutionState exceptionHolder = new ExecutionState(readyTask,
          depGraph, nbJobs, force);
      exceptionHolder.terminate();
      assert depGraph.isEmpty();
    }
  }

  protected void execDepGraphSynchronous(
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask, final boolean force)
      throws ADLException, InterruptedException {
    while (!readyTask.isEmpty()) {
      final CompilationCommand cmd = readyTask.removeFirst();

      execCmd(cmd, force);
      commandEnded(cmd, depGraph, readyTask);
    }
    assert depGraph.isEmpty();
  }

  protected void execCmd(final CompilationCommand cmd, final boolean force)
      throws ADLException, InterruptedException {
    if (force) {
      cmd.exec();
      return;
    }

    long outputTs = Long.MAX_VALUE;
    for (final File outputFile : cmd.getOutputFiles()) {
      if (!outputFile.exists()) {
        outputTs = 0L;
        if (depLogger.isLoggable(Level.FINE))
          depLogger.fine("Output file '" + outputFile + "' of task '"
              + cmd.getDescription() + "' is missing, compile.");
        break;
      }
      final long ts = outputFile.lastModified();
      if (ts < outputTs) outputTs = ts;
    }

    boolean recompile;
    if (outputTs > 0L) {
      recompile = false;
      for (final File inputFile : cmd.getInputFiles()) {
        if (inputFile.lastModified() > outputTs) {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Input file '" + inputFile
                + "' is more recent than output files of task '"
                + cmd.getDescription() + "', recompile.");
          recompile = true;
          break;
        }
      }
    } else {
      recompile = true;
    }

    if (recompile) {
      cmd.exec();
    } else {
      if (depLogger.isLoggable(Level.FINE))
        depLogger.fine("Task '" + cmd.getDescription()
            + "'is up-to-date, skip.");
    }
  }

  protected boolean commandEnded(final CompilationCommand cmd,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask) {
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
          readyTask.addLast(cmdInfo.command);
          commandUnlocked = true;
        }
      }
    }
    return commandUnlocked;
  }

  protected final class ExecutionState {
    final LinkedList<CompilationCommand>                   readyTask;
    final Map<CompilationCommand, Collection<CommandInfo>> depGraph;
    final boolean                                          force;

    final Lock                                             lock      = new ReentrantLock();
    Condition                                              condition = lock
                                                                         .newCondition();
    Exception                                              exception;
    int                                                    nbRunningThread;

    ExecutionState(final LinkedList<CompilationCommand> rTask,
        final Map<CompilationCommand, Collection<CommandInfo>> dGraph,
        final int nbJob, final boolean force) {
      this.readyTask = rTask;
      this.depGraph = dGraph;
      this.force = force;

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
        while (!readyTask.isEmpty()) {
          final CompilationCommand cmd = readyTask.removeFirst();
          lock.unlock();

          try {
            execCmd(cmd, force);
          } catch (final Exception e) {
            lock.lock();
            if (exception != null) {
              exception = e;
            }
            break;
          }

          lock.lock();
          commandEnded(cmd, depGraph, readyTask);
        }
      } finally {
        nbRunningThread--;
        if (nbRunningThread <= 0) {
          condition.signalAll();
        }
        lock.unlock();
      }
    }

    void terminate() throws ADLException, InterruptedException {
      lock.lock();
      try {
        while (nbRunningThread > 0) {
          condition.await();
        }
      } finally {
        lock.unlock();
      }
      if (exception != null) {
        if (exception instanceof ADLException)
          throw (ADLException) exception;
        else
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, exception,
              "Unexpected error");
      }
    }
  }

  protected static final class CommandInfo {
    final CompilationCommand       command;
    Collection<CompilationCommand> dependencies = new ArrayList<CompilationCommand>();
    long                           timestamp    = -1L;

    CommandInfo(final CompilationCommand command) {
      this.command = command;
    }
  }
}
