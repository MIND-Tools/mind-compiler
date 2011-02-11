
package org.ow2.mind.mindc.test.cli;

import java.util.Map;

import org.ow2.mind.cli.CmdOption;
import org.ow2.mind.cli.CommandLine;
import org.ow2.mind.cli.CommandOptionHandler;
import org.ow2.mind.cli.InvalidCommandLineException;

public class CmdArgumentHandler implements CommandOptionHandler {

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    context.put("cmdArgument", cmdOption);
  }

}
