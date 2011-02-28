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

import org.ow2.mind.ADLCompiler.CompilationStage;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "check-adl", "def2c" and "def2o" options.
 */
public class StageOptionHandler implements CommandOptionHandler {

  /** The ID of the "check-adl" option. */
  public static final String  CHECK_ADL_ID      = "org.ow2.mind.mindc.CheckADL";
  /** The ID of the "def2c" option. */
  public static final String  DEF_TO_C_ID       = "org.ow2.mind.mindc.Def2C";
  /** The ID of the "def2o" option. */
  public static final String  DEF_TO_O_ID       = "org.ow2.mind.mindc.Def2O";

  private static final String STAGE_CONTEXT_KEY = "compilation-stage";

  /**
   * Returns the {@link CompilationStage} that is registered in the given
   * context.
   * 
   * @param context a context.
   * @return the {@link CompilationStage} that is registered in the given
   *         context (defaults to {@link CompilationStage#COMPILE_EXE}).
   */
  public static CompilationStage getCompilationStage(
      final Map<Object, Object> context) {
    final CompilationStage stage = (CompilationStage) context
        .get(STAGE_CONTEXT_KEY);
    return (stage == null) ? CompilationStage.COMPILE_EXE : stage;
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    if (CHECK_ADL_ID.equals(cmdOption.getId())) {
      final CmdFlag opt = Assert.assertInstanceof(cmdOption, CmdFlag.class);
      if (opt.isPresent(cmdLine)) {
        context.put(STAGE_CONTEXT_KEY, CompilationStage.CHECK_ADL);
      }

    } else if (DEF_TO_C_ID.equals(cmdOption.getId())) {
      final CmdFlag opt = Assert.assertInstanceof(cmdOption, CmdFlag.class);
      if (opt.isPresent(cmdLine)) {
        context.put(STAGE_CONTEXT_KEY, CompilationStage.GENERATE_SRC);
      }

    } else if (DEF_TO_O_ID.equals(cmdOption.getId())) {
      final CmdFlag opt = Assert.assertInstanceof(cmdOption, CmdFlag.class);
      if (opt.isPresent(cmdLine)) {
        context.put(STAGE_CONTEXT_KEY, CompilationStage.COMPILE_DEF);
      }

    } else {
      Assert.fail("Unknown id '" + cmdOption.getId() + "'");
    }

  }

}
