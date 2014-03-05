/**
 * Copyright (C) 2014 Schneider-Electric
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
 * Authors: Stephane Seyvoz
 * Contributors: 
 */

package org.ow2.mind.cli;

import java.util.Map;

import org.ow2.mind.adl.BasicDefinitionCompiler;
import org.ow2.mind.plugin.util.Assert;

/**
 * This class handles the --keep-source-name flag (defined in the
 * mind-plugin.xml) and stores according information in the context, enabling
 * our alternative naming convention for temporary and binary output files in
 * the adl-backend. This naming convention uses "_<user_file_name>".c/.o
 * suffixing instead of "_impl<i>".c/.o, making debug easier for the users.
 * 
 * @see BasicDefinitionCompiler
 * @author Stephane Seyvoz
 */
public class KeepSourceNameOptionHandler implements CommandOptionHandler {

  /** The ID of the "keep" option. */
  public static final String KEEPSRCNAME_ID = "org.ow2.mind.cli.KeepSourceName";

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {

    final CmdFlag opt = Assert.assertInstanceof(cmdOption, CmdFlag.class);

    if (KEEPSRCNAME_ID.equals(opt.getId())) {
      KeepSrcNameContextHelper.setKeepSourceName(context,
          opt.isPresent(cmdLine));
    } else {
      Assert.fail("Unknown id '" + opt.getId() + "'");
    }

  }

}
