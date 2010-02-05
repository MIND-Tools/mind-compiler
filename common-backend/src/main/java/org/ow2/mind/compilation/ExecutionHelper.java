/***
 * Cecilia ADL Compiler
 * Copyright (C) 2006-2008 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: fractal@objectweb.org
 *
 * Author:Matthieu Leclercq
 */

package org.ow2.mind.compilation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;

/**
 * This helper class provides method to execute external commands.
 */
public final class ExecutionHelper {
  private ExecutionHelper() {
  }

  // The io logger
  protected static Logger logger = FractalADLLogManager.getLogger("io");

  /**
   * Executes the given command line and returns the exit value. The given
   * command is splited on space character boundary (this method is equivalent
   * to <code>exec(execTitle, command.split("\\s"))</code>)<br>
   * Note that the {@link #exec(String, List<String>)} method is safer since it
   * while not split command line on space character boundary which may produce
   * unexpected result if arguments may contains spaces.
   * 
   * @param command the command to execute.
   * @return the exit value
   * @throws ADLException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List<String>)
   */
  public static int exec(final String command) throws ADLException,
      InterruptedException {
    return exec(null, command);
  }

  /**
   * Executes the given command line and returns the exit value. The given
   * command is splited on space character boundary, unless the space is escaped
   * by a backslash.<br>
   * Note that the {@link #exec(String, List<String>)} method is safer since it
   * while not split command line on space character boundary which may produce
   * unexpected result if arguments may contains spaces.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param command the command to execute.
   * @return the exit value
   * @throws ADLException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List<String>)
   */
  public static int exec(final String execTitle, final String command)
      throws ADLException, InterruptedException {
    return exec(execTitle, DirectiveHelper.splitOptionString(command));
  }

  /**
   * Executes the given command line and returns the exit value.<br>
   * This method will issue some messages on the <code>io</code> logger. If the
   * {@link Level#FINE FINE} level is enabled, the full command line will be
   * logged. If the {@link Level#INFO INFO} level is enabled, the given
   * <code>execTitle</code> will be logged. Finally, if the process produces
   * output on its standard error stream. This output will be logged with the
   * {@link Level#SEVERE SEVERE} level preceded by the <code>execTitle</code> if
   * it has not been logged yet.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param cmdList the command to execute.
   * @return the exit value
   * @throws ADLException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   */
  public static int exec(final String execTitle, final List<String> cmdList)
      throws ADLException, InterruptedException {
    final Process process;
    try {
      process = new ProcessBuilder(cmdList).redirectErrorStream(true).start();
    } catch (final IOException e1) {
      throw new ADLException(CompilerErrors.EXECUTION_ERROR, cmdList.get(0));
    }
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

    // Read output produced by process in a parallele thread in order to avoid
    // the process to block
    final Thread readerThread = new Thread() {
      @Override
      public void run() {
        // output output and error stream on the logger.
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
            process.getInputStream()));
        try {
          String line = reader.readLine();
          if (line != null) {
            // if the title has not been printed yet.
            if (!titleLogged) {
              String command = "";
              for (final String cmd : cmdList) {
                command += cmd + " ";
              }
              logger.severe((execTitle == null) ? command : execTitle);
            }
            do {
              logger.severe(line);
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

    return rValue;
  }

  /**
   * Executes the given command line and returns the exit value.
   * 
   * @param execTitle the message to be logged as a header of the execution. May
   *          be <code>null</code>.
   * @param cmdArray the command to execute.
   * @return the exit value
   * @throws ADLException If an error occurs while running the command.
   * @throws InterruptedException if the calling thread has been interrupted
   *           while waiting for the process to finish.
   * @see #exec(String, List<String>)
   * @see Runtime#exec(String[])
   */
  public static int exec(final String execTitle, final String[] cmdArray)
      throws ADLException, InterruptedException {
    return exec(execTitle, Arrays.asList(cmdArray));
  }
}
