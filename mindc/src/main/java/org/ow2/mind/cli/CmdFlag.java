
package org.ow2.mind.cli;

/**
 * An option that may be present or not on a command line.
 */
public class CmdFlag extends CmdOption {

  /** @see CmdOption#CmdOption(String, String, String, String) */
  public CmdFlag(final String id, final String shortName,
      final String longName, final String description) {
    super(id, shortName, longName, description);
  }

  void setPresent(final CommandLine commandLine) {
    commandLine.setOptionValue(this, "");
  }
}