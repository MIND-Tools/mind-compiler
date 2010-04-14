
package org.ow2.mind;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.AbstractLauncher.CmdOption;

public interface CommandOptionHandler {
  void processCommandOption(CmdOption cmdOption, Map<Object, Object> context)
      throws ADLException;
}
