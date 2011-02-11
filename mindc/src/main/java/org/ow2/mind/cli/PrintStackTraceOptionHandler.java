/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.cli;

import java.util.Map;

import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "e" option (print-stack-trace). Update context accordingly.
 */
public class PrintStackTraceOptionHandler implements CommandOptionHandler {

  /** The ID of the "force" option. */
  public static final String  PRINT_STACK_TRACE_ID          = "org.ow2.mind.mindc.PrintStackTrace";

  private static final String PRINT_STACK_TRACE_CONTEXT_KEY = "print-stack-trace";

  /**
   * Returns <code>true</code> if the print-stack-trace is enabled in the given
   * context.
   * 
   * @param context a context.
   * @return <code>true</code> if the print-stack-trace is enabled in the given
   *         context.
   */
  public static boolean getPrintStackTrace(final Map<Object, Object> context) {
    final Boolean b = (Boolean) context.get(PRINT_STACK_TRACE_CONTEXT_KEY);
    return (b == null) ? false : b;
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), PRINT_STACK_TRACE_ID, "Invalid ID");
    if (cmdOption.isPresent(cmdLine)) {
      context.put(PRINT_STACK_TRACE_CONTEXT_KEY, Boolean.TRUE);
    }
  }

}
