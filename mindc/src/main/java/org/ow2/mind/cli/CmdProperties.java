
package org.ow2.mind.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * An option that associate name to value.
 */
public class CmdProperties extends CmdOption {
  protected final String argNameDesc;
  protected final String argValueDesc;

  /**
   * @param id the identifier of this option.
   * @param shortName the short name of the option. Must have one and only one
   *          character.
   * @param description the description of the option (used to generate help
   *          message).
   * @param argNameDesc the description of the argument name (used to generate
   *          help message).
   * @param argValueDesc the description of the argument value (used to generate
   *          help message).
   */
  public CmdProperties(final String id, final String shortName,
      final String description, final String argNameDesc,
      final String argValueDesc) {
    super(id, shortName, null, description
        + ". This option may be specified several times.");
    this.argNameDesc = argNameDesc;
    this.argValueDesc = argValueDesc;
  }

  @Override
  public String getPrototype() {
    return "-" + shortName + argNameDesc + "=" + argValueDesc;
  }

  @SuppressWarnings("unchecked")
  void setValue(final CommandLine commandLine, final String name,
      final String value) throws InvalidCommandLineException {
    if (name == null || value == null) return;
    Map<String, String> values = (Map<String, String>) commandLine
        .getOptionValue(this);
    if (values == null) {
      values = new HashMap<String, String>();
      commandLine.setOptionValue(this, values);
    }
    values.put(name, value);
  }

  /**
   * Returns the value of this option in the given command-line.
   * 
   * @param commandLine a command-line.
   * @return A map associating name to value, or <code>null</code> if this
   *         option is not specified on the given command line.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> getValue(final CommandLine commandLine) {
    return (Map<String, String>) commandLine.getOptionValue(this);
  }
}