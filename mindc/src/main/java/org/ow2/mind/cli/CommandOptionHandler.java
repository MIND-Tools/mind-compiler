
package org.ow2.mind.cli;

import java.util.Map;

public interface CommandOptionHandler {
  void processCommandOption(CmdOption cmdOption, CommandLine cmdLine,
      Map<Object, Object> context) throws InvalidCommandLineException;
}
