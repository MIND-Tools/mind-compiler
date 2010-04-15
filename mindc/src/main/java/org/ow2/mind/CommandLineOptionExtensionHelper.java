
package org.ow2.mind;

import static org.ow2.mind.plugin.ast.PluginASTHelper.getExtensionConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.AbstractLauncher.CmdFlag;
import org.ow2.mind.AbstractLauncher.CmdOption;
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

  public static Collection<CmdOption> getCommandOptions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    if (cmdOptions == null) {
      initCmdOptions(pluginManagerItf, context);
    }
    return cmdOptions;
  }

  public static CommandOptionHandler getHandler(final CmdOption cmdOption) {
    return handlerMap.get(cmdOption);
  }

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
          if (element.getNodeName().equals("cmdFlag")) {
            final CmdFlag flag = new CmdFlag(element.getAttribute("shortName"),
                element.getAttribute("longName"), element
                    .getAttribute("description"));
            cmdOptions.add(flag);
            handlerMap.put(flag, newHandler(element.getAttribute("handler")));
          }
        }
      }
    }
  }

  private static CommandOptionHandler newHandler(final String handlerClassName)
      throws ADLException {
    try {
      return CommandOptionHandler.class.getClassLoader().loadClass(
          handlerClassName).asSubclass(CommandOptionHandler.class)
          .newInstance();
    } catch (final InstantiationException e) {
      throw new ADLException(GenericErrors.GENERIC_ERROR, e, "Handler class '"
          + handlerClassName + "' cannot be instantiated.");
    } catch (final IllegalAccessException e) {
      throw new ADLException(GenericErrors.GENERIC_ERROR, e,
          "Illegal access to the handler class '" + handlerClassName + "'.");
    } catch (final ClassNotFoundException e) {
      throw new ADLException(GenericErrors.GENERIC_ERROR, e, "Handler class '"
          + handlerClassName + "' not found.");
    }
  }
}
