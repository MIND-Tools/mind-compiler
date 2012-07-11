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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.DirectiveHelper;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "cpp-flags", "c-flags", "inc-path", "as-flags", "ld-flags", "ld-path"
 * and "linker-script" options.
 * 
 * @see CompilerContextHelper
 */
public class FlagsOptionHandler implements CommandOptionHandler {

  /** The ID of the "cpp-flags" option. */
  public static final String CPPFLAGS_ID      = "org.ow2.mind.mindc.CPPFlags";
  /** The ID of the "c-flags" option. */
  public static final String CFLAGS_ID        = "org.ow2.mind.mindc.CFlags";
  /** The ID of the "inc-path" option. */
  public static final String INC_PATH_ID      = "org.ow2.mind.mindc.IncPath";
  /** The ID of the "as-flags" option. */
  public static final String ASFLAGS_ID       = "org.ow2.mind.mindc.ASFlags";
  /** The ID of the "ld-flags" option. */
  public static final String LDFLAGS_ID       = "org.ow2.mind.mindc.LDFlags";
  /** The ID of the "ld-path" option. */
  public static final String LD_PATH_ID       = "org.ow2.mind.mindc.LDPath";

  /** The ID of the "linker-script" option. */
  public static final String LINKER_SCRIPT_ID = "org.ow2.mind.mindc.LinkerScript";

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    if (CPPFLAGS_ID.equals(cmdOption.getId())) {
      // process CFlags
      final CmdAppendOption cppFlagsOpt = Assert.assertInstanceof(cmdOption,
          CmdAppendOption.class);

      final List<String> cppFlagsList = new ArrayList<String>(
          CompilerContextHelper.getCPPFlags(context));
      final String value = cppFlagsOpt.getValue(cmdLine);
      if (value != null && value.length() > 0) {
        cppFlagsList.addAll(DirectiveHelper.splitOptionString(value));
      }
      CompilerContextHelper.setCPPFlags(context, cppFlagsList);
    } else if (CFLAGS_ID.equals(cmdOption.getId())) {
      // process CFlags
      final CmdAppendOption cFlagsOpt = Assert.assertInstanceof(cmdOption,
          CmdAppendOption.class);

      final List<String> cFlagsList = new ArrayList<String>(
          CompilerContextHelper.getCFlags(context));
      final String value = cFlagsOpt.getValue(cmdLine);
      if (value != null && value.length() > 0) {
        cFlagsList.addAll(DirectiveHelper.splitOptionString(value));
      }
      CompilerContextHelper.setCFlags(context, cFlagsList);
    } else if (ASFLAGS_ID.equals(cmdOption.getId())) {
      // process ASFlags
      final CmdAppendOption asFlagsOpt = Assert.assertInstanceof(cmdOption,
          CmdAppendOption.class);

      final List<String> asFlagsList = new ArrayList<String>(
          CompilerContextHelper.getASFlags(context));
      final String value = asFlagsOpt.getValue(cmdLine);
      if (value != null && value.length() > 0) {
        asFlagsList.addAll(DirectiveHelper.splitOptionString(value));
      }
      CompilerContextHelper.setASFlags(context, asFlagsList);
    } else if (INC_PATH_ID.equals(cmdOption.getId())) {
      // process inc-path
      final CmdPathOption includePathOpt = Assert.assertInstanceof(cmdOption,
          CmdPathOption.class);

      final List<String> incPaths = new ArrayList<String>();

      // "src-path" is added as "inc-path"
      final CmdPathOption srcPathOpt = (CmdPathOption) cmdLine.getOptions()
          .getById(SrcPathOptionHandler.SRC_PATH_ID);
      if (srcPathOpt.getPathValue(cmdLine) != null) {
        incPaths.addAll(srcPathOpt.getPathValue(cmdLine));
      }
      if (includePathOpt.getValue(cmdLine) != null) {
        incPaths.addAll(includePathOpt.getPathValue(cmdLine));
      }
      // "out-path is added as "inc-path"
      incPaths.add(OutPathOptionHandler.getOutPath(context).getAbsolutePath());

      final List<String> cFlagsList = new ArrayList<String>(
          CompilerContextHelper.getCFlags(context));

      for (final String inc : incPaths) {
        final File incDir = new File(inc);
        cFlagsList.add("-I");
        cFlagsList.add(incDir.getAbsolutePath());
      }

      CompilerContextHelper.setCPPFlags(context, cFlagsList);

    } else if (LDFLAGS_ID.equals(cmdOption.getId())) {
      // process LDFlags
      final CmdAppendOption ldFlagsOpt = Assert.assertInstanceof(cmdOption,
          CmdAppendOption.class);

      final List<String> ldFlagsList = new ArrayList<String>(
          CompilerContextHelper.getLDFlags(context));

      final String value = ldFlagsOpt.getValue(cmdLine);
      if (value != null && value.length() > 0) {
        ldFlagsList.addAll(DirectiveHelper.splitOptionString(value));
      }

      CompilerContextHelper.setLDFlags(context, ldFlagsList);

    } else if (LD_PATH_ID.equals(cmdOption.getId())) {
      // process ld-path
      final CmdPathOption ldPathOpt = Assert.assertInstanceof(cmdOption,
          CmdPathOption.class);

      final List<String> ldPaths = ldPathOpt.getPathValue(cmdLine);
      final List<String> ldFlagsList = new ArrayList<String>(
          CompilerContextHelper.getLDFlags(context));
      if (ldPaths != null) {
        for (final String ld : ldPaths) {
          final File ldDir = new File(ld);
          ldFlagsList.add("-L");
          ldFlagsList.add(ldDir.getAbsolutePath());
        }
      }
      CompilerContextHelper.setLDFlags(context, ldFlagsList);

    } else if (LINKER_SCRIPT_ID.equals(cmdOption.getId())) {
      final CmdArgument linkerScriptOpt = Assert.assertInstanceof(cmdOption,
          CmdArgument.class);

      final String linkerScript = linkerScriptOpt.getValue(cmdLine);
      if (linkerScript != null) {
        final URL linkerScriptURL = SrcPathOptionHandler.getSourceClassLoader(
            context).getResource(linkerScript);
        if (linkerScriptURL == null) {
          throw new InvalidCommandLineException("Invalid linker script: '"
              + linkerScript + "'. Cannot find file in the source path", 1);
        }

        CompilerContextHelper.setLinkerScript(context,
            linkerScriptURL.getPath());
      }

    } else {
      Assert.fail("Unknown id '" + cmdOption.getId() + "'");
    }
  }
}
