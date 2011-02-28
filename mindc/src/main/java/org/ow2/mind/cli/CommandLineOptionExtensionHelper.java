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

package org.ow2.mind.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public final class CommandLineOptionExtensionHelper {
  public static final String                                 COMMAND_LINE_OPTIONS_EXTENSION = "org.ow2.mind.mindc.command-line-options";

  protected static Map<String, CmdOption>                    cmdOptions;
  protected static Map<String, List<String>>                 preconditions;
  protected static Collection<Set<String>>                   exclusiveGroups;
  protected static Multimap<CmdOption, CommandOptionHandler> handlerMap;

  private CommandLineOptionExtensionHelper() {
  }

  /**
   * Returns the collection of {@link CmdOption} objects that defined by loaded
   * the plugins.
   * 
   * @param pluginManagerItf the plugin manager component to be used for loading
   *          plugins.
   * @return the collection of {@link CmdOption} objects that defined by loaded
   *         the plugins.
   */
  public static Collection<CmdOption> getCommandOptions(
      final PluginManager pluginManagerItf) {
    if (cmdOptions == null) {
      initRegistry(pluginManagerItf);
    }
    return Collections.unmodifiableCollection(cmdOptions.values());
  }

  /**
   * Accessor method for getting the command option handler objects which are
   * registered for the cmdOption.
   * 
   * @param pluginManagerItf the plugin manager component to be used for loading
   *          plugins.
   * @param cmdOption the option object which the handler is associated to.
   * @return Collection of the handler object. Returns an empty list if no
   *         handler is regirstered.
   */
  public static Collection<CommandOptionHandler> getHandler(
      final CmdOption cmdOption, final PluginManager pluginManagerItf) {
    if (handlerMap == null) initRegistry(pluginManagerItf);
    final Collection<CommandOptionHandler> handlers = handlerMap.get(cmdOption);
    return (handlers == null)
        ? Collections.<CommandOptionHandler> emptyList()
        : handlers;
  }

  /**
   * Returns the identifiers of the command options that must be processed
   * before the given option.
   * 
   * @param cmdOption an option.
   * @param pluginManagerItf the plugin manager component to be used for loading
   *          plugins.
   * @return the identifiers of the command options that must be processed
   *         before the given option.
   */
  public static List<String> getPrecedenceIds(final CmdOption cmdOption,
      final PluginManager pluginManagerItf) {
    if (preconditions == null) initRegistry(pluginManagerItf);
    final List<String> l = preconditions.get(cmdOption.getId());

    return (l == null) ? Collections.<String> emptyList() : Collections
        .unmodifiableList(l);
  }

  /**
   * Returns the exclusive groups
   * 
   * @param pluginManagerItf the plugin manager component to be used for loading
   *          plugins.
   * @return the exclusive groups
   */
  public static Collection<Set<String>> getExclusiveGroups(
      final PluginManager pluginManagerItf) {
    if (exclusiveGroups == null) initRegistry(pluginManagerItf);

    return Collections.unmodifiableCollection(exclusiveGroups);
  }

  private static final String ID              = "id";
  private static final String SHORT_NAME      = "shortName";
  private static final String LONG_NAME       = "longName";
  private static final String DESCRIPTION     = "description";
  private static final String DEFAULT_VALUE   = "defaultValue";
  private static final String ALLOW_MULTIPLE  = "allowMultiple";
  private static final String ARG_DESC        = "argDesc";
  private static final String HANDLER         = "handler";
  private static final String SEPARATOR       = "separator";
  private static final String ARG_NAME_DESC   = "argNameDesc";
  private static final String ARG_VALUE_DESC  = "argValueDesc";

  private static final String FLAG            = "cmdFlag";
  private static final String PROPERTIES      = "cmdProperties";
  private static final String ARGUMENT        = "cmdArgument";
  private static final String APPEND_OPTION   = "cmdAppendOption";
  private static final String PATH_OPTION     = "cmdPathOption";

  private static final String PROCESS_AFTER   = "processAfter";
  private static final String PROCESS_BEFORE  = "processBefore";

  private static final String EXCLUSIVE_GROUP = "exclusiveGroup";

  // Parses the command-line extensions and initializes the option and handler
  // collections.
  // Supported elements are: cmdFlag, cmdProperties, cmdArgument,
  // cmdAppendOption and cmdPathOption.
  private static void initRegistry(final PluginManager pluginManagerItf) {
    cmdOptions = new LinkedHashMap<String, CmdOption>();
    handlerMap = ArrayListMultimap.create();

    for (final ConfigurationElement element : pluginManagerItf
        .getConfigurationElements(COMMAND_LINE_OPTIONS_EXTENSION)) {
      if (element.getName().equals(EXCLUSIVE_GROUP)) continue;

      CmdOption cmdOption;
      if (element.getName().equals(FLAG)) {
        cmdOption = new CmdFlag(element.getAttribute(ID),
            element.getAttribute(SHORT_NAME), element.getAttribute(LONG_NAME),
            element.getAttribute(DESCRIPTION));

      } else if (element.getName().equals(PROPERTIES)) {
        cmdOption = new CmdProperties(element.getAttribute(ID),
            element.getAttribute(SHORT_NAME),
            element.getAttribute(DESCRIPTION),
            element.getAttribute(ARG_NAME_DESC),
            element.getAttribute(ARG_VALUE_DESC));

      } else if (element.getName().equals(ARGUMENT)) {
        final boolean allowMultiple = element.getAttribute(ALLOW_MULTIPLE) != null
            ? new Boolean(element.getAttribute(ALLOW_MULTIPLE)).booleanValue()
            : false;
        cmdOption = new CmdArgument(element.getAttribute(ID),
            element.getAttribute(SHORT_NAME), element.getAttribute(LONG_NAME),
            element.getAttribute(DESCRIPTION), element.getAttribute(ARG_DESC),
            element.getAttribute(DEFAULT_VALUE), allowMultiple);

      } else if (element.getName().equals(APPEND_OPTION)) {
        final String separator = element.getAttribute(SEPARATOR) != null
            ? element.getAttribute(SEPARATOR)
            : " ";
        cmdOption = new CmdAppendOption(element.getAttribute(ID),
            element.getAttribute(SHORT_NAME), element.getAttribute(LONG_NAME),
            element.getAttribute(DESCRIPTION), element.getAttribute(ARG_DESC),
            element.getAttribute(DEFAULT_VALUE), separator);

      } else if (element.getName().equals(PATH_OPTION)) {
        cmdOption = new CmdPathOption(element.getAttribute(ID),
            element.getAttribute(SHORT_NAME), element.getAttribute(LONG_NAME),
            element.getAttribute(DESCRIPTION), element.getAttribute(ARG_DESC));

      } else {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Unknown plugin element '" + element.getName() + "'.");
      }
      final CmdOption prevOpt = cmdOptions.put(cmdOption.getId(), cmdOption);
      if (prevOpt != null) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Invalid command line option Id: '" + cmdOption.getId()
                + "' is already used.");
      }

      final CommandOptionHandler handler = element.createInstance(HANDLER,
          CommandOptionHandler.class);
      if (handler != null) {
        handlerMap.put(cmdOption, handler);
      }

    }

    // initialize preconditions.
    preconditions = new HashMap<String, List<String>>();
    for (final ConfigurationElement element : pluginManagerItf
        .getConfigurationElements(COMMAND_LINE_OPTIONS_EXTENSION)) {
      final String id = element.getAttribute(ID);

      List<String> precondition = preconditions.get(id);
      if (precondition == null) {
        precondition = new ArrayList<String>();
        preconditions.put(id, precondition);
      }
      for (final ConfigurationElement processAfter : element
          .getChildren(PROCESS_AFTER)) {
        final String afterId = processAfter.getAttribute(ID);
        if (!cmdOptions.containsKey(afterId)) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR,
              "Unknown option Id: '" + afterId + "'.");
        }
        precondition.add(afterId);
      }

      for (final ConfigurationElement processBefore : element
          .getChildren(PROCESS_BEFORE)) {
        final String beforeId = processBefore.getAttribute(ID);
        if (!cmdOptions.containsKey(beforeId)) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR,
              "Unknown option Id: '" + beforeId + "'.");
        }
        List<String> l = preconditions.get(beforeId);
        if (l == null) {
          l = new ArrayList<String>();
          preconditions.put(beforeId, l);
        }
        l.add(id);
      }
    }

    // initialize exclusiveGroups
    exclusiveGroups = new ArrayList<Set<String>>();
    for (final ConfigurationElement element : pluginManagerItf
        .getConfigurationElements(COMMAND_LINE_OPTIONS_EXTENSION,
            EXCLUSIVE_GROUP)) {
      final Set<String> exclusiveGroup = new HashSet<String>();
      for (final ConfigurationElement child : element.getChildren()) {
        exclusiveGroup.add(child.getAttribute(ID));
      }
      exclusiveGroups.add(Collections.unmodifiableSet(exclusiveGroup));
    }
  }
}
