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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind;

import static org.ow2.mind.plugin.ast.PluginASTHelper.getExtensionConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.AbstractLauncher.CmdAppendOption;
import org.ow2.mind.AbstractLauncher.CmdArgument;
import org.ow2.mind.AbstractLauncher.CmdFlag;
import org.ow2.mind.AbstractLauncher.CmdOption;
import org.ow2.mind.AbstractLauncher.CmdPathOption;
import org.ow2.mind.AbstractLauncher.CmdProperties;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class CommandLineOptionExtensionHelper {
  protected static Collection<CmdOption>                      cmdOptions                     = null;

  public static final String                                  COMMAND_LINE_OPTIONS_EXTENSION = "org.ow2.mind.mindc.command-line-options";

  protected static final Map<CmdOption, CommandOptionHandler> handlerMap                     = new HashMap<CmdOption, CommandOptionHandler>();

  private CommandLineOptionExtensionHelper() {
  }

  /**
   * Returns the collection of {@link CmdOption} objects that defined by loaded
   * the plugins.
   * 
   * @param pluginManagerItf the plugin manager component to be used for loading
   *          plugins.
   * @param context the compilation context.
   * @return the collection of {@link CmdOption} objects that defined by loaded
   *         the plugins.
   * @throws ADLException if any problems occur while loading the plugins.
   */
  public static Collection<CmdOption> getCommandOptions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    if (cmdOptions == null) {
      initCmdOptions(pluginManagerItf, context);
    }
    return cmdOptions;
  }

  /**
   * Accessor method for getting the command option handler object which is
   * registered for the cmdOption.
   * 
   * @param cmdOption the option object which the handler is associated to.
   * @return reference to the handler object.
   */
  public static CommandOptionHandler getHandler(final CmdOption cmdOption) {
    return handlerMap.get(cmdOption);
  }

  public static final String SHORT_NAME     = "shortName";
  public static final String LONG_NAME      = "longName";
  public static final String DESCRIPTION    = "description";
  public static final String DEFAULT_VALUE  = "defaultValue";
  public static final String ALLOW_MULTIPLE = "allowMultiple";
  public static final String ARG_DESC       = "argDesc";
  public static final String HANDLER        = "handler";
  public static final String SEPARATOR      = "separator";
  public static final String ARG_NAME_DESC  = "argNameDesc";
  public static final String ARG_VALUE_DESC = "argValueDesc";

  public static final String FLAG           = "cmdFlag";
  public static final String PROPERTIES     = "cmdProperties";
  public static final String ARGUMENT       = "cmdArgument";
  public static final String APPEND_OPTION  = "cmdAppendOption";
  public static final String PATH_OPTION    = "cmdPathOption";

  // Parses the command-line extensions and initializes the option and handler
  // collections.
  // Supported elements are: cmdFlag, cmdProperties, cmdArgument,
  // cmdAppendOption and cmdPathOption.
  private static void initCmdOptions(final PluginManager pluginManagerItf,
      final Map<Object, Object> context) throws ADLException {
    cmdOptions = new ArrayList<CmdOption>();
    final Collection<Extension> extensions = pluginManagerItf.getExtensions(
        COMMAND_LINE_OPTIONS_EXTENSION, context);
    for (final Extension extension : extensions) {
      final NodeList nodes = getExtensionConfig(extension).getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        final Node node = nodes.item(i);
        if (node instanceof Element) {
          final Element element = (Element) node;
          CmdOption cmdOption = null;
          if (element.getNodeName().equals(FLAG)) {
            cmdOption = new CmdFlag(element.getAttribute(SHORT_NAME),
                element.getAttribute(LONG_NAME),
                element.getAttribute(DESCRIPTION));
          } else if (element.getNodeName().equals(PROPERTIES)) {
            cmdOption = new CmdProperties(element.getAttribute(SHORT_NAME),
                element.getAttribute(DESCRIPTION),
                element.getAttribute(ARG_NAME_DESC),
                element.getAttribute(ARG_VALUE_DESC));
          } else if (element.getNodeName().equals(ARGUMENT)) {
            final boolean allowMultiple = element.getAttribute(ALLOW_MULTIPLE) != null
                ? new Boolean(element.getAttribute(ALLOW_MULTIPLE))
                    .booleanValue() : false;
            cmdOption = new CmdArgument(element.getAttribute(SHORT_NAME),
                element.getAttribute(LONG_NAME),
                element.getAttribute(DESCRIPTION),
                element.getAttribute(ARG_DESC),
                element.getAttribute(DEFAULT_VALUE), allowMultiple);
          } else if (element.getNodeName().equals("cmdAppendOption")) {
            final String separator = element.getAttribute(SEPARATOR) != null
                ? element.getAttribute(SEPARATOR)
                : "";
            cmdOption = new CmdAppendOption(element.getAttribute(SHORT_NAME),
                element.getAttribute(LONG_NAME),
                element.getAttribute(DESCRIPTION),
                element.getAttribute(ARG_DESC),
                element.getAttribute(DEFAULT_VALUE), separator);
          } else if (element.getNodeName().equals("cmdPathOption")) {
            cmdOption = new CmdPathOption(element.getAttribute(SHORT_NAME),
                element.getAttribute(LONG_NAME),
                element.getAttribute(DESCRIPTION),
                element.getAttribute(ARG_DESC));
          } else {
            throw new CompilerError(GenericErrors.GENERIC_ERROR,
                "Unknown plugin element '" + element.getNodeName() + "'.");
          }
          cmdOptions.add(cmdOption);
          handlerMap.put(cmdOption, newHandler(element.getAttribute(HANDLER)));
        }
      }
    }
  }

  private static CommandOptionHandler newHandler(final String handlerClassName)
      throws ADLException {
    try {
      return CommandOptionHandler.class.getClassLoader()
          .loadClass(handlerClassName).asSubclass(CommandOptionHandler.class)
          .newInstance();
    } catch (final InstantiationException e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e, "Handler class '"
          + handlerClassName + "' cannot be instantiated.");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
          "Illegal access to the handler class '" + handlerClassName + "'.");
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e, "Handler class '"
          + handlerClassName + "' not found.");
    }
  }
}
