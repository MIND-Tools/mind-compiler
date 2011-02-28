/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target;

import java.util.Map;

import org.ow2.mind.cli.CmdArgument;
import org.ow2.mind.cli.CmdOption;
import org.ow2.mind.cli.CommandLine;
import org.ow2.mind.cli.CommandOptionHandler;
import org.ow2.mind.cli.InvalidCommandLineException;
import org.ow2.mind.cli.SrcPathOptionHandler;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "target-descriptor" option. Reads the target descriptor and register
 * it in the context. This handler depends on {@link SrcPathOptionHandler}.
 */
public class TargetDescriptorOptionHandler implements CommandOptionHandler {

  /** The ID of the "target-descriptor" option. */
  public static final String TARGET_DESCRIPTOR_ID = "org.ow2.mind.mindc.TargetDescriptor";

  /**
   * Returns the name of the target descriptor that has been registered in the
   * given context.
   * 
   * @param context the current context.
   * @return the name of the target descriptor or <code>null</code>.
   */
  public static String getTargetDescriptor(final Map<Object, Object> context) {
    return (String) context.get(TARGET_DESCRIPTOR_ID);
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), TARGET_DESCRIPTOR_ID);
    final CmdArgument targetDescOpt = Assert.assertInstanceof(cmdOption,
        CmdArgument.class);

    final String targetDesc = targetDescOpt.getValue(cmdLine);
    if (targetDesc != null) {
      context.put(TARGET_DESCRIPTOR_ID, targetDesc);
    }
  }

}
