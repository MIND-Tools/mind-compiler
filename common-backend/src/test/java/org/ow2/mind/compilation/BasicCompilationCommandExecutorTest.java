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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class BasicCompilationCommandExecutorTest {

  private static final boolean LOG          = false;
  private long                 start;

  Set<File>                    createdFiles = new HashSet<File>();
  CompilationCommandExecutor   executor;
  Map<Object, Object>          context;

  @BeforeTest(alwaysRun = true)
  public void setup() {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();
    final BasicCompilationCommandExecutor bcce = new BasicCompilationCommandExecutor();
    bcce.errorManagerItf = errorManager;
    executor = bcce;
    context = new HashMap<Object, Object>();
  }

  @Test(groups = {"perf"})
  public void testPerf() throws Exception {

    // doTest(5, 5, true);doTest(5, 5, false);
    // doTest(10, 10, true);doTest(10, 10, false);
    // doTest(20, 20, true);doTest(20, 20, false);
    // doTest(40, 40, true);doTest(40, 40, false);
    // doTest(100, 100, true);doTest(100, 100, false);

    doTest(5, 1);
    doTest(5, 2);
    doTest(5, 3);
    doTest(5, 4);

    doTest(10, 1);
    doTest(10, 2);
    doTest(10, 3);
    doTest(10, 4);

    doTest(20, 1);
    doTest(20, 2);
    doTest(20, 3);
    doTest(20, 4);
  }

  private void doTest(final int nbJobs, final int nbThread)
      throws InterruptedException, ADLException {

    context.put(BasicCompilationCommandExecutor.CONCURENT_JOBS_CONTEXT_KEY,
        nbThread);
    final Collection<CompilationCommand> cmds = new ArrayList<CompilationCommand>();
    final LinkTask linkTask = new LinkTask();
    for (int i = 0; i < nbJobs; i++) {
      cmds.add(new CPPTask(Integer.toString(i)));
      cmds.add(new MPPTask(Integer.toString(i)));
      final GCCTask gcc = new GCCTask(Integer.toString(i));
      cmds.add(gcc);
      linkTask.inputFiles.addAll(gcc.outputFiles);
    }
    cmds.add(linkTask);
    start = System.currentTimeMillis();
    try {
      executor.exec(cmds, context);
    } finally {
      System.out.printf(
          "nbJobs=%2d, nbThreads=%2d. tasks executed in %5.2f s.%n", nbJobs,
          nbThread, time());
    }
  }

  private double time() {
    return (System.currentTimeMillis() - start) / 1000.0;
  }

  class CPPTask implements CompilationCommand {

    Collection<File> inputFiles, outputFiles;
    final String     name;

    CPPTask(final String name) {
      this.name = name;
      inputFiles = Arrays.asList(new File(name + ".c"));
      outputFiles = Arrays.asList(new File(name + ".i"));
    }

    public Collection<File> getInputFiles() {
      return inputFiles;
    }

    public Collection<File> getOutputFiles() {
      return outputFiles;
    }

    public String getDescription() {
      return "CPP" + name;
    }

    public boolean forceExec() {
      return false;
    }

    public void prepare() {
    }

    public boolean exec() throws ADLException, InterruptedException {
      if (LOG)
        System.out
            .printf(
                "%5.2f: In Call of preproc task %s. Sleep for 0.5s before returning result. Thread=%s%n",
                time(), getDescription(), Thread.currentThread());
      synchronized (this) {
        this.wait(500);
      }
      if (LOG)
        System.out.printf(
            "%5.2f: In Call of preproc task %s. Return result.%n", time(),
            getDescription());
      createdFiles.addAll(outputFiles);
      return true;
    }
  }

  class GCCTask implements CompilationCommand {

    Collection<File> inputFiles, outputFiles;
    final String     name;

    GCCTask(final String name) {
      this.name = name;
      inputFiles = Arrays.asList(new File(name + ".mpp"));
      outputFiles = Arrays.asList(new File(name + ".o"));
    }

    public Collection<File> getInputFiles() {
      return inputFiles;
    }

    public Collection<File> getOutputFiles() {
      return outputFiles;
    }

    public String getDescription() {
      return "GCC" + name;
    }

    public boolean forceExec() {
      return false;
    }

    public void prepare() {
    }

    public boolean exec() throws ADLException, InterruptedException {
      if (LOG)
        System.out
            .printf(
                "%5.2f: In Call of compile task %s. Get preprocessed file. Thread=%s%n",
                time(), getDescription(), Thread.currentThread());
      assertTrue(createdFiles.containsAll(inputFiles));
      if (LOG)
        System.out
            .printf(
                "%5.2f: In Call of compile task %s. Sleep for 0.5s before returning result. Thread=%s%n",
                time(), getDescription(), Thread.currentThread());
      synchronized (this) {
        this.wait(500);
      }
      if (LOG)
        System.out.printf(
            "%5.2f: In Call of compile task %s. Return result.%n", time(),
            getDescription());
      createdFiles.addAll(outputFiles);
      return true;
    }
  }

  class MPPTask extends GCCTask {

    MPPTask(final String name) {
      super(name);
      inputFiles = Arrays.asList(new File(name + ".i"));
      outputFiles = Arrays.asList(new File(name + ".mpp"));
    }

    @Override
    public String getDescription() {
      return "MPP" + name;
    }
  }

  class LinkTask implements CompilationCommand {

    Collection<File> inputFiles  = new ArrayList<File>();
    Collection<File> outputFiles = Arrays.asList(new File("exec"));

    public Collection<File> getInputFiles() {
      return inputFiles;
    }

    public Collection<File> getOutputFiles() {
      return outputFiles;
    }

    public String getDescription() {
      return "LD";
    }

    public boolean forceExec() {
      return false;
    }

    public void prepare() {
    }

    public boolean exec() throws ADLException, InterruptedException {
      if (LOG)
        System.out.printf("%5.2f: In Call of link task. Thread=%s%n", time(),
            Thread.currentThread());
      assertTrue(createdFiles.containsAll(inputFiles));
      if (LOG)
        System.out
            .printf(
                "%5.2f: In Call of link task. Sleep for 0.5s before returning result.%n",
                time());
      synchronized (this) {
        this.wait(500);
      }
      if (LOG)
        System.out.printf("%5.2f: In Call of link task. Return result.%n",
            time());
      createdFiles.addAll(outputFiles);
      return true;
    }
  }
}
