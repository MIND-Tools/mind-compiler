
package org.ow2.mind.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An option that have a value and that may be specified several time on a
 * command-line. The resulting option value is the concatenation of the values
 * of each occurrence of this option separated by {@link File#pathSeparator}.
 */
public class CmdPathOption extends CmdAppendOption {

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
   */
  public CmdPathOption(final String id, final String shortName,
      final String longName, final String description, final String argDesc) {
    super(id, shortName, longName, description, argDesc, null,
        File.pathSeparator);
  }

  /**
   * @param commandLine a command-line
   * @return the value of this option on the given command-line as a list of
   *         String, or <code>null</code>.
   */
  public List<String> getPathValue(final CommandLine commandLine) {
    final String value = getValue(commandLine);
    if (value == null)
      return null;
    else
      return parsePathList(value);
  }

  protected List<String> parsePathList(String paths) {
    final List<String> l = new ArrayList<String>();
    int index = paths.indexOf(File.pathSeparatorChar);
    while (index != -1) {
      l.add(paths.substring(0, index));
      paths = paths.substring(index + 1);
      index = paths.indexOf(File.pathSeparatorChar);
    }
    l.add(paths);
    return l;
  }
}