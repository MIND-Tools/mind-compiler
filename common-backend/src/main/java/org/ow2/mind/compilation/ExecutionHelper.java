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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;

/**
 * This helper class provides method to execute external commands.
 */
public final class ExecutionHelper {
  private ExecutionHelper() {
  }

  public static class ExecutionResult {
    final int    rValue;
    final String output;

    protected ExecutionResult(final int rValue, final StringBuilder output) {
      this.rValue = rValue;
      if (output.length() == 0)
        this.output = null;
      else
        this.output = output.toString();
    }

    public int getExitValue() {
      return rValue;
    }

    public String getOutput() {
      return output;
    }
  }

  // The io logger
  protected static Logger logger = FractalADLLogManager.getLogger("io");

  /**
   * Executes the given command line and returns the exit value. The given
   * command is splited on space character boundary (this method is equivalent
   * to <code>exec(execTitle, command.split("\\s"))</code>)<br>
   * Note that the {@link #exec(String, List)} method is safer since it while
   * not split command line on space character boundary which may produce
   * unexpected result if arguments contains spaces.
   * 
   * @param command the command to execute.
   * @return if its exit value is zero, return null, otherwise returns a string
   *         that contains the process output.
   * @throws IOException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List)
   */
  public static ExecutionResult exec(final String command) throws IOException,
      InterruptedException {
    return exec(null, command);
  }

  /**
   * Executes the given command line and returns the exit value. The given
   * command is splited on space character boundary, unless the space is escaped
   * by a backslash.<br>
   * Note that the {@link #exec(String, List)} method is safer since it while
   * not split command line on space character boundary which may produce
   * unexpected result if arguments contains spaces.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param command the command to execute.
   * @return if its exit value is zero, return null, otherwise returns a string
   *         that contains the process output.
   * @throws IOException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List)
   */
  public static ExecutionResult exec(final String execTitle,
      final String command) throws IOException, InterruptedException {
    return exec(execTitle, DirectiveHelper.splitOptionString(command));
  }

  /**
   * Executes the given command line and returns the exit value.<br>
   * This method will issue some messages on the <code>io</code> logger. If the
   * {@link Level#FINE FINE} level is enabled, the full command line will be
   * logged. If the {@link Level#INFO INFO} level is enabled, the given
   * <code>execTitle</code> will be logged.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param cmdList the command to execute.
   * @return if its exit value is zero, return null, otherwise returns a string
   *         that contains the process output.
   * @throws IOException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   */
  public static ExecutionResult exec(final String execTitle,
      final List<String> cmdList) throws IOException, InterruptedException {
    final boolean titleLogged;
    if (logger.isLoggable(Level.INFO) && execTitle != null) {
      logger.info(execTitle);
      titleLogged = true;
    } else {
      titleLogged = false;
    }

    if (logger.isLoggable(Level.FINE)) {
      String command = "";
      for (final String cmd : cmdList) {
        command += cmd + " ";
      }
      logger.fine(command);
    }

    final Process process;
    process = new ProcessBuilder(cmdList).redirectErrorStream(true).start();

    final StringBuilder processOutput = new StringBuilder();
    // Read output produced by process in a parallel thread in order to avoid
    // the process to block
    final Thread readerThread = new Thread() {
      @Override
      public void run() {
        // append output and error stream on the processOutput.
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
            process.getInputStream()));
        try {
          String line = reader.readLine();
          if (line != null) {
            // if the title has not been printed yet.
            if (!titleLogged) {
              if (execTitle != null) logger.severe(execTitle);
            }

            // in Log level "FINE" the command was already previously shown,
            // do not repeat, but display for errors in all other modes
            // for debug purposes
            if (!logger.isLoggable(Level.FINE)) {
              String command = "";
              for (final String cmd : cmdList) {
                command += cmd + " ";
              }
              logger.severe(command);
            }

            do {
              processOutput.append(line).append("\n");
              line = reader.readLine();
            } while (line != null);
          }
          reader.close();
        } catch (final IOException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Can't read error stream of process");
        }
      }
    };

    readerThread.start();
    final int rValue = process.waitFor();
    readerThread.join();

    return new ExecutionResult(rValue, processOutput);
  }

  /**
   * Executes the given command line and returns the exit value.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param cmdArray the command to execute.
   * @return if its exit value is zero, return null, otherwise returns a string
   *         that contains the process output.
   * @throws IOException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List)
   * @see Runtime#exec(String[])
   */
  public static ExecutionResult exec(final String execTitle,
      final String[] cmdArray) throws IOException, InterruptedException {
    return exec(execTitle, Arrays.asList(cmdArray));
  }
}
