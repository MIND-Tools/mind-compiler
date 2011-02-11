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

import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "force", "keep" and "no-bin" options. Update context accordingly
 * using {@link ForceRegenContextHelper}.
 */
public class ForceRegenOptionhandler implements CommandOptionHandler {

  /** The ID of the "force" option. */
  public static final String FORCE_ID  = "org.ow2.mind.mindc.Force";
  /** The ID of the "keep" option. */
  public static final String KEEP_ID   = "org.ow2.mind.mindc.KeepTempFile";
  /** The ID of the "no-bin" option. */
  public static final String NO_BIN_ID = "org.ow2.mind.mindc.NoBinaryADL";

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    final CmdFlag opt = Assert.assertInstanceof(cmdOption, CmdFlag.class);

    if (FORCE_ID.equals(opt.getId())) {
      ForceRegenContextHelper.setForceRegen(context, opt.isPresent(cmdLine));
    } else if (KEEP_ID.equals(opt.getId())) {
      ForceRegenContextHelper.setKeepTemp(context, opt.isPresent(cmdLine));
    } else if (NO_BIN_ID.equals(opt.getId())) {
      ForceRegenContextHelper.setNoBinaryAST(context, opt.isPresent(cmdLine));
    } else {
      Assert.fail("Unknown id '" + opt.getId() + "'");
    }
  }

}
