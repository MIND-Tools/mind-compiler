
package org.ow2.mind.cli;

/**
 * An option that have a value and that may be specified several time on a
 * command-line. The resulting option value is the concatenation of the values
 * of each occurrence of this option.
 */
public class CmdAppendOption extends CmdArgument {

  protected final String separator;

  /**
   * Constructor for CmdAppendOption that has no default value and that use
   * <code>" "</code> as separator.
   * 
   * @param id the identifier of this option.
   * @param shortName the short name of the option. Must have one and only one
   *          character. May be <code>null</code>.
   * @param longName the long name of the option. Must have more than one
   *          character. May be <code>null</code>.
   * @param description the description of the option (used to generate help
   *          message).
   * @param argDesc the description of the argument value (used to generate help
   *          message).
   */
  public CmdAppendOption(final String id, final String shortName,
      final String longName, final String description, final String argDesc) {
    this(id, shortName, longName, description, argDesc, null, " ");
  }

  /**
   * @param id the identifier of this option.
   * @param shortName the short name of the option. Must have one and only one
   *          character. May be <code>null</code>.
   * @param longName the long name of the option. Must have more than one
   *          character. May be <code>null</code>.
   * @param description the description of the option (used to generate help
   *          message).
   * @param argDesc the description of the argument value (used to generate help
   *          message).
   * @param defaultValue the default value of this option. May be
   *          <code>null</code>.
   * @param separator the string used to separate individual value.
   */
  public CmdAppendOption(final String id, final String shortName,
      final String longName, final String description, final String argDesc,
      final String defaultValue, final String separator) {
    super(id, shortName, longName, description, argDesc, defaultValue, true);
    this.separator = separator;
  }

  @Override
  public void setValue(final CommandLine commandLine, final String value)
      throws InvalidCommandLineException {
    if (value == null) return;
    final String prevValue = (String) commandLine.getOptionValue(this);

    if (prevValue == null) {
      commandLine.setOptionValue(this, value);
    } else {
      commandLine.setOptionValue(this, prevValue + separator + value);
    }
  }
}