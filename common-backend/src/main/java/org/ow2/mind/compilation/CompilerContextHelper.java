/**
 * Copyright (C) 2009 STMicroelectronics
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

package org.ow2.mind.compilation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CompilerContextHelper {

  public static final String LINKER_COMMAND_CONTEXT_KEY    = "linker-command";
  public static final String ASSEMBLER_COMMAND_CONTEXT_KEY = "assembler-command";
  public static final String COMPILER_COMMAND_CONTEXT_KEY  = "compiler-command";
  public static final String DEFAULT_COMPILER_COMMAND      = "gcc";
  public static final String DEFAULT_ASSEMBLER_COMMAND     = "gcc";
  public static final String DEFAULT_LINKER_COMMAND        = "gcc";

  public static final String EXECUTABLE_NAME_CONTEXT_KEY   = "executable-name";

  public static final String C_FLAGS_CONTEXT_KEY           = "c-flags";
  public static final String AS_FLAGS_CONTEXT_KEY          = "as-flags";
  public static final String CPP_FLAGS_CONTEXT_KEY         = "cpp-flags";
  public static final String LD_FLAGS_CONTEXT_KEY          = "ld-flags";
  public static final String LINKER_SCRIPT_CONTEXT_KEY     = "linker-script";

  private CompilerContextHelper() {
  }

  public static void setCPPFlags(final Map<Object, Object> context,
      final List<String> flags) {
    context.put(CPP_FLAGS_CONTEXT_KEY, flags);
  }

  public static void addCPPFlags(final Map<Object, Object> context,
      final List<String> flags) {
    final List<String> f = getCPPFlags(context);
    if (f.isEmpty()) {
      setCPPFlags(context, flags);
    } else {
      f.addAll(flags);
    }
  }

  @SuppressWarnings("unchecked")
  public static List<String> getCPPFlags(final Map<Object, Object> context) {
    List<String> flags = (List<String>) context.get(CPP_FLAGS_CONTEXT_KEY);
    if (flags == null) flags = Collections.emptyList();
    return flags;
  }

  public static void setCFlags(final Map<Object, Object> context,
      final List<String> flags) {
    context.put(C_FLAGS_CONTEXT_KEY, flags);
  }

  public static void addCFlags(final Map<Object, Object> context,
      final List<String> flags) {
    final List<String> f = getCFlags(context);
    if (f.isEmpty()) {
      setCFlags(context, flags);
    } else {
      f.addAll(flags);
    }
  }

  @SuppressWarnings("unchecked")
  public static List<String> getCFlags(final Map<Object, Object> context) {
    List<String> flags = (List<String>) context.get(C_FLAGS_CONTEXT_KEY);
    if (flags == null) flags = Collections.emptyList();
    return flags;
  }

  public static void setASFlags(final Map<Object, Object> context,
      final List<String> flags) {
    context.put(AS_FLAGS_CONTEXT_KEY, flags);
  }

  public static void addASFlags(final Map<Object, Object> context,
      final List<String> flags) {
    final List<String> f = getASFlags(context);
    if (f.isEmpty()) {
      setASFlags(context, flags);
    } else {
      f.addAll(flags);
    }
  }

  public static List<String> getASFlags(final Map<Object, Object> context) {
    List<String> flags = (List<String>) context.get(AS_FLAGS_CONTEXT_KEY);
    if (flags == null) flags = Collections.emptyList();
    return flags;
  }

  public static void setLDFlags(final Map<Object, Object> context,
      final List<String> flags) {
    context.put(LD_FLAGS_CONTEXT_KEY, flags);
  }

  public static void addLDFlags(final Map<Object, Object> context,
      final List<String> flags) {
    final List<String> f = getLDFlags(context);
    if (f.isEmpty()) {
      setLDFlags(context, flags);
    } else {
      f.addAll(flags);
    }
  }

  public static List<String> getLDFlags(final Map<Object, Object> context) {
    List<String> flags = (List<String>) context.get(LD_FLAGS_CONTEXT_KEY);
    if (flags == null) flags = Collections.emptyList();
    return flags;
  }

  public static void setCompilerCommand(final Map<Object, Object> context,
      final String compilerCmd) {
    context.put(COMPILER_COMMAND_CONTEXT_KEY, compilerCmd);
  }

  public static String getCompilerCommand(final Map<Object, Object> context) {
    String compilerCmd = (String) context.get(COMPILER_COMMAND_CONTEXT_KEY);
    if (compilerCmd == null) {
      compilerCmd = DEFAULT_COMPILER_COMMAND;
    }
    return compilerCmd;
  }

  public static void setAssemblerCommand(final Map<Object, Object> context,
      final String assemblerCmd) {
    context.put(ASSEMBLER_COMMAND_CONTEXT_KEY, assemblerCmd);
  }

  public static String getAssemblerCommand(final Map<Object, Object> context) {
    String assemblerCmd = (String) context.get(ASSEMBLER_COMMAND_CONTEXT_KEY);
    if (assemblerCmd == null) {
      assemblerCmd = DEFAULT_ASSEMBLER_COMMAND;
    }
    return assemblerCmd;
  }

  public static void setLinkerCommand(final Map<Object, Object> context,
      final String linkerCmd) {
    context.put(LINKER_COMMAND_CONTEXT_KEY, linkerCmd);
  }

  public static String getLinkerCommand(final Map<Object, Object> context) {
    String compilerCmd = (String) context.get(LINKER_COMMAND_CONTEXT_KEY);
    if (compilerCmd == null) {
      compilerCmd = DEFAULT_LINKER_COMMAND;
    }
    return compilerCmd;
  }

  public static void setLinkerScript(final Map<Object, Object> context,
      final String linkerScript) {
    context.put(LINKER_SCRIPT_CONTEXT_KEY, linkerScript);
  }

  public static String getLinkerScript(final Map<Object, Object> context) {
    return (String) context.get(LINKER_SCRIPT_CONTEXT_KEY);
  }

  public static void setExecutableName(final Map<Object, Object> context,
      final String executableName) {
    context.put(EXECUTABLE_NAME_CONTEXT_KEY, executableName);
  }

  public static String getExecutableName(final Map<Object, Object> context) {
    return (String) context.get(EXECUTABLE_NAME_CONTEXT_KEY);
  }
}
