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

import java.io.File;
import java.util.Map;

import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "out-path" option. Create the output directory and register it in the
 * context.
 */
public class OutPathOptionHandler implements CommandOptionHandler {

  /** The ID of the "out-path" option. */
  public static final String OUT_PATH_ID = "org.ow2.mind.mindc.OutPath";

  /**
   * Returns the out-path that has been registered in the given context.
   * 
   * @param context the current context.
   * @return the out-path.
   */
  public static File getOutPath(final Map<Object, Object> context) {
    return (File) context.get(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY);
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), OUT_PATH_ID);
    final CmdArgument outDirOpt = Assert.assertInstanceof(cmdOption,
        CmdArgument.class);

    final String optValue = outDirOpt.getValue(cmdLine);
    if (optValue == null || optValue.length() == 0) {
      throw new InvalidCommandLineException("Invalid output directory ''", 1);
    }
    final File buildDir = new File(optValue);
    if (buildDir.exists() && !buildDir.isDirectory())
      throw new InvalidCommandLineException("Invalid build directory '"
          + buildDir.getAbsolutePath() + "' not a directory", 6);
    if (!buildDir.exists()) {
      throw new InvalidCommandLineException("Invalid output directory '"
          + optValue + "' does not exist.", 1);
    }
    context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);
  }

}
