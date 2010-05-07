
package org.ow2.mind.mindc.test.cli;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.CommandLineOptionExtensionHelper;
import org.ow2.mind.CommandOptionHandler;
import org.ow2.mind.AbstractLauncher.CmdOption;
import org.ow2.mind.AbstractLauncher.CommandLine;

public class CmdPathOptionHandler implements CommandOptionHandler {

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws ADLException {
    context.put(CommandLineOptionExtensionHelper.PATH_OPTION, cmdOption);
  }

}
