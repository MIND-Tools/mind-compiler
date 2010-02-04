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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;

public class BasicCompilationCommandExecutor
    implements
      CompilationCommandExecutor {

  public static final String CONCURENT_JOBS_CONTEXT_KEY = "jobs";

  // ---------------------------------------------------------------------------
  // Implementation of the CompilationCommandExecutor interface
  // ---------------------------------------------------------------------------

  public void exec(final Collection<CompilationCommand> commands,
      final Map<Object, Object> context) throws ADLException,
      InterruptedException {
    final Map<CompilationCommand, Collection<CommandInfo>> depGraph = new HashMap<CompilationCommand, Collection<CommandInfo>>();
    final LinkedList<CompilationCommand> readyTask = new LinkedList<CompilationCommand>();
    buildDepGraph(commands, depGraph, readyTask);

    int jobs = 1;
    final Object o = context.get(CONCURENT_JOBS_CONTEXT_KEY);
    if (o instanceof Integer) {
      jobs = (Integer) o;
    }
    execDepGraph(jobs, depGraph, readyTask);
  }

  protected void buildDepGraph(final Collection<CompilationCommand> commands,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask) {
    final Map<File, List<CompilationCommand>> fileProviders = new HashMap<File, List<CompilationCommand>>();
    for (final CompilationCommand cmd : commands) {
      for (final File outputFile : cmd.getOutputFiles()) {
        List<CompilationCommand> providers = fileProviders.get(outputFile);
        if (providers == null) {
          providers = new ArrayList<CompilationCommand>();
          fileProviders.put(outputFile, providers);
        }
        providers.add(cmd);
      }
    }

    for (final CompilationCommand cmd : commands) {
      final CommandInfo cmdInfo = new CommandInfo(cmd);
      for (final File inputFile : cmd.getInputFiles()) {
        final List<CompilationCommand> providers = fileProviders.get(inputFile);
        if (providers != null) {
          for (final CompilationCommand provider : providers) {
            Collection<CommandInfo> deps = depGraph.get(provider);
            if (deps == null) {
              deps = new LinkedList<CommandInfo>();
              depGraph.put(provider, deps);
            }
            deps.add(cmdInfo);
            cmdInfo.nbDeps++;
          }
        }
      }
      if (cmdInfo.nbDeps == 0) {
        // command has no dependency
        readyTask.addLast(cmd);
      }
    }
  }

  protected void execDepGraph(final int nbJobs,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask) throws ADLException,
      InterruptedException {
    if (nbJobs == 1) {
      execDepGraphSynchronous(depGraph, readyTask);
    } else {
      final ExecutionState exceptionHolder = new ExecutionState(readyTask,
          depGraph, nbJobs);
      exceptionHolder.terminate();
      assert depGraph.isEmpty();
    }
  }

  protected void execDepGraphSynchronous(
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask) throws ADLException,
      InterruptedException {
    while (!readyTask.isEmpty()) {
      final CompilationCommand cmd = readyTask.removeFirst();
      cmd.exec();
      commandEnded(cmd, depGraph, readyTask);
    }
    assert depGraph.isEmpty();
  }

  protected void commandEnded(final CompilationCommand cmd,
      final Map<CompilationCommand, Collection<CommandInfo>> depGraph,
      final LinkedList<CompilationCommand> readyTask) {
    final Collection<CommandInfo> deps = depGraph.remove(cmd);
    if (deps != null) {
      final Iterator<CommandInfo> iter = deps.iterator();
      while (iter.hasNext()) {
        final CommandInfo cmdInfo = iter.next();
        assert cmdInfo.nbDeps > 0;
        cmdInfo.nbDeps--;
        if (cmdInfo.nbDeps == 0) {
          // every dependency of command are done, place it in ready task.
          iter.remove();
          readyTask.addLast(cmdInfo.command);
        }
      }
    }
  }

  protected final class ExecutionState {
    final LinkedList<CompilationCommand>                   readyTask;
    final Map<CompilationCommand, Collection<CommandInfo>> depGraph;

    final Lock                                             lock      = new ReentrantLock();
    Condition                                              condition = lock
                                                                         .newCondition();
    Exception                                              exception;
    int                                                    nbRunningThread;

    ExecutionState(final LinkedList<CompilationCommand> rTask,
        final Map<CompilationCommand, Collection<CommandInfo>> dGraph,
        final int nbJob) {
      this.readyTask = rTask;
      this.depGraph = dGraph;

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
            cmd.exec();
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
    final CompilationCommand command;
    int                      nbDeps = 0;

    CommandInfo(final CompilationCommand command) {
      this.command = command;
    }
  }
}
