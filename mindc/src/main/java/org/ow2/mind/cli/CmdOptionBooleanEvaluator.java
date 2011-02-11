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
import java.util.regex.Pattern;

import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.util.Assert;
import org.ow2.mind.plugin.util.BooleanEvaluator;

/**
 * {@link BooleanEvaluator} that can be used to evaluate the presence of a
 * command-line option. The evaluated {@link ConfigurationElement} must have at
 * least a <code>id</code> attribute that must correspond to the identifier of a
 * command-line option and may have a <code>value</code> or
 * <code>valueRegExp</code> attribute.
 */
public class CmdOptionBooleanEvaluator implements BooleanEvaluator {

  /** The key used to register {@link CommandLine} in context. */
  public static final String  CMD_LINE_CONTEXT_KEY = "command-line";

  private static final String ID                   = "id";
  private static final String VALUE                = "value";
  private static final String VALUE_REGEXP         = "valueRegExp";

  /**
   * Returns the {@link CommandLine} that is registered in the given context.
   * 
   * @param context a compiler context.
   * @return the {@link CommandLine} that is registered in the given context.
   */
  public static CommandLine getCommandLine(final Map<Object, Object> context) {
    return (CommandLine) context.get(CMD_LINE_CONTEXT_KEY);
  }

  public boolean evaluate(final ConfigurationElement configurationElement,
      final PluginManager pluginManager, final Map<Object, Object> context) {
    Assert.assertNotNull(context,
        "CmdOptionBooleanEvaluator requires a context");
    final CommandLine cmdLine = getCommandLine(context);
    Assert.assertNotNull(context,
        "CmdOptionBooleanEvaluator cannot find command-line in the context");

    final String id = configurationElement.getAttribute(ID);
    Assert.assertNotNull(context, "CmdOptionBooleanEvaluator id is null");
    final CmdOption cmdOption = cmdLine.getOptions().getById(id);
    Assert.assertNotNull(context,
        "CmdOptionBooleanEvaluator can't find option with id '" + id + "'.");

    if (!cmdOption.isPresent(cmdLine)) {
      return false;
    }

    final String value = configurationElement.getAttribute(VALUE);
    final String valueRegExp = configurationElement.getAttribute(VALUE_REGEXP);
    if (value != null || valueRegExp != null) {
      final CmdArgument cmdArgument = Assert
          .assertInstanceof(
              cmdOption,
              CmdArgument.class,
              "CmdOptionBooleanEvaluator value can be used only on CmdArgument command option");
      final String cmdValue = cmdArgument.getValue(cmdLine);
      return (value != null) ? value.equals(cmdValue) : Pattern.matches(
          valueRegExp, cmdValue);
    } else {
      return true;
    }
  }
}
