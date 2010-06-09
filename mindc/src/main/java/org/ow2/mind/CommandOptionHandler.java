
package org.ow2.mind;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.AbstractLauncher.CmdOption;
import org.ow2.mind.AbstractLauncher.CommandLine;

public interface CommandOptionHandler {
  void processCommandOption(CmdOption cmdOption, CommandLine cmdLine,
      Map<Object, Object> context) throws ADLException;
}
