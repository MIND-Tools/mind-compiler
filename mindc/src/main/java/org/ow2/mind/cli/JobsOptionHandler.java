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
 * Handles "jobs" option. Registers value in context.
 */
public class JobsOptionHandler implements CommandOptionHandler {

  /** The ID of the "jobs" option. */
  public static final String  JOBS_ID          = "org.ow2.mind.mindc.Jobs";

  private static final String JOBS_CONTEXT_KEY = "jobs";

  /**
   * Returns the number of concurrent jobs that has been registered in the given
   * context.
   * 
   * @param context the current context.
   * @return the number of concurrent jobs (default is "1" if not jobs info is
   *         registered in context).
   */
  public static int getJobs(final Map<Object, Object> context) {
    final Integer jobs = (Integer) context.get(JOBS_CONTEXT_KEY);
    return (jobs == null) ? 1 : jobs;
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), JOBS_ID);
    final CmdArgument jobOpt = Assert.assertInstanceof(cmdOption,
        CmdArgument.class);

    Integer jobs = null;
    try {
      jobs = Integer.decode(jobOpt.getValue(cmdLine));
    } catch (final NumberFormatException e) {
      throw new InvalidCommandLineException("Invalid jobs value '"
          + jobOpt.getValue(cmdLine) + "' is not a valid number", 1);
    }
    context.put(JOBS_CONTEXT_KEY, jobs);
  }
}
