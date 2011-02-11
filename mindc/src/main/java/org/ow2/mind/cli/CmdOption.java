
package org.ow2.mind.cli;

/**
 * Base class of command line options.
 */
public abstract class CmdOption {
  protected final String id;
  protected final String shortName;
  protected final String longName;
  protected final String description;

  /**
   * @param id the identifier of this option.
   * @param shortName the short name of the option. Must have one and only one
   *          character. May be <code>null</code>.
   * @param longName the long name of the option. Must have more than one
   *          character. May be <code>null</code>.
   * @param description the description of the option (used to generate help
   *          message).
   */
  public CmdOption(final String id, final String shortName,
      final String longName, final String description) {
    if (id == null)
      throw new IllegalArgumentException("Invalid id, cannot be null");
    if (shortName == null && longName == null)
      throw new IllegalArgumentException("Invalid option names");
    if (shortName != null && shortName.length() > 1)
      throw new IllegalArgumentException("Invalid shortName");
    if (longName != null && longName.length() <= 1)
      throw new IllegalArgumentException("Invalid longName");

    this.id = id;
    this.shortName = shortName;
    this.longName = longName;
    this.description = description;
  }

  /** @return the prototype of the options (used to generate help message). */
  public String getPrototype() {
    String desc;
    if (shortName != null) {
      desc = "-" + shortName;
      if (longName != null) {
        desc += ", --" + longName;
      }
    } else {
      desc = "--" + longName;
    }
    return desc;
  }

  /**
   * @return the identifier of this option.
   */
  public String getId() {
    return id;
  }

  /** @return the short name of the option. */
  public String getShortName() {
    return shortName;
  }

  /** @return the long name of the option. */
  public String getLongName() {
    return longName;
  }

  /** @return the description of the option. */
  public String getDescription() {
    return description;
  }

  /**
   * @param commandLine a command-line.
   * @return <code>true</code> if this option is present on the given
   *         command-line
   */
  public boolean isPresent(final CommandLine commandLine) {
    return commandLine.isOptionPresent(this);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;

    if (!(obj instanceof CmdOption)) return false;

    final CmdOption opt = (CmdOption) obj;
    if (shortName == null) {
      return opt.shortName == null && opt.longName.equals(longName);
    } else {
      return shortName.equals(opt.shortName);
    }
  }

  @Override
  public int hashCode() {
    if (shortName == null)
      return longName.hashCode();
    else
      return shortName.hashCode();
  }

  @Override
  public String toString() {
    return "CmdOption(id=" + id + ")";
  }
}