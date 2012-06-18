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
 * Contributors: Julien Tous
 */

package org.ow2.mind.cli;

import java.util.Map;

import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "compiler-command" and "linker-command" options.
 * 
 * @see CompilerContextHelper
 */
public class CompilationCommandOptionHandler implements CommandOptionHandler {

  /** The ID of the "compiler-command" option. */
  public static final String COMPILER_COMMAND_ID  = "org.ow2.mind.mindc.CompilerCommand";
  /** The ID of the "assembler-command" option. */
  public static final String ASSEMBLER_COMMAND_ID = "org.ow2.mind.mindc.AssemblerCommand";
  /** The ID of the "linker-command" option. */
  public static final String LINKER_COMMAND_ID    = "org.ow2.mind.mindc.LinkerCommand";

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    if (COMPILER_COMMAND_ID.equals(cmdOption.getId())) {
      final CmdArgument compilerCmdOpt = Assert.assertInstanceof(cmdOption,
          CmdArgument.class);

      if (compilerCmdOpt.isPresent(cmdLine)) {
        final String value = compilerCmdOpt.getValue(cmdLine);
        if (value.length() == 0)
          throw new InvalidCommandLineException("Invalid compiler ''", 1);
        CompilerContextHelper.setCompilerCommand(context, value);
      }

    } else if (LINKER_COMMAND_ID.equals(cmdOption.getId())) {
      final CmdArgument linkerCmdOpt = Assert.assertInstanceof(cmdOption,
          CmdArgument.class);

      if (linkerCmdOpt.isPresent(cmdLine)) {
        final String value = linkerCmdOpt.getValue(cmdLine);
        if (value.length() == 0)
          throw new InvalidCommandLineException("Invalid linker ''", 1);
        CompilerContextHelper.setLinkerCommand(context, value);
      }

    } else if (ASSEMBLER_COMMAND_ID.equals(cmdOption.getId())) {
      final CmdArgument assemblerCmdOpt = Assert.assertInstanceof(cmdOption,
          CmdArgument.class);

      if (assemblerCmdOpt.isPresent(cmdLine)) {
        final String value = assemblerCmdOpt.getValue(cmdLine);
        if (value.length() == 0)
          throw new InvalidCommandLineException("Invalid assembler ''", 1);
        CompilerContextHelper.setAssemblerCommand(context, value);
      }

    } else {
      Assert.fail("Unknown id '" + cmdOption.getId() + "'");
    }
  }

}
