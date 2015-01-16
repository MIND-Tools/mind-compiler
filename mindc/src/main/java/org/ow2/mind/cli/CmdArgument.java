
package org.ow2.mind.cli;

/**
 * A command line option that have a value.
 */
public class CmdArgument extends CmdOption {
  protected final String  argDesc;
  protected final String  defaultValue;
  protected final boolean allowMultiple;

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
   * @param allowMultiple if <code>true</code>, this option can be specified
   *          several time on a command-line. In that case, the last occurrence
   *          is used.
   */
  public CmdArgument(final String id, final String shortName,
      final String longName, final String description, final String argDesc,
      final String defaultValue, final boolean allowMultiple) {
    super(id, shortName, longName, (defaultValue == null)
        ? description
        : description + " (default is '" + defaultValue + "')");
    this.argDesc = argDesc;
    this.defaultValue = defaultValue;
    this.allowMultiple = allowMultiple;
  }

  /**
   * Constructor for CmdArgument that has no default value and that does not
   * allow multiple occurrences.
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
  public CmdArgument(final String id, final String shortName,
      final String longName, final String description, final String argDesc) {
    this(id, shortName, longName, description, argDesc, null, false);
  }

  public void setValue(final CommandLine commandLine, final String value)
      throws InvalidCommandLineException {
    if (value == null) return;
    final Object prevValue = commandLine.setOptionValue(this, value);
    if (!allowMultiple && prevValue != null) {
      throw new InvalidCommandLineException("'" + longName
          + "' can't be specified several times.", 1);
    }
  }

  /**
   * Return the value of this option in the given command-line.
   * 
   * @param commandLine a command line.
   * @return the value of this option in the given command-line, or the
   *         {@link #getDefaultValue() default value}, or <code>null</code> if
   *         the given command line does not contains this option and this
   *         option has no default value.
   */
  public String getValue(final CommandLine commandLine) {
    final String optionValue = (String) commandLine.getOptionValue(this);
    return optionValue == null ? defaultValue : optionValue;
  }

  /** @return the default value. */
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public String getPrototype() {
    String desc;
    if (shortName != null) {
      desc = "-" + shortName + "=" + argDesc;
      if (longName != null) {
        desc += ", --" + longName;
      }
    } else {
      desc = "--" + longName + "=" + argDesc;
    }
    return desc;
  }

  public String getArgDescription() {
    return argDesc;
  }

  @Override
  public String getDescription() {
    if (allowMultiple)
      return super.getDescription()
          + ". This option may be specified several times.";
    else
      return super.getDescription();
  }
}