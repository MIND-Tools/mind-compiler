/**
 * Copyright (C) 2009 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind;

import java.io.File;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract compiler Launcher.
 */
public abstract class AbstractLauncher {

  protected static final String PROGRAM_NAME_PROPERTY_NAME = "cecilia.launcher.name";

  protected final CmdPathOption srcPathOpt                 = new CmdPathOption(
                                                               "S",
                                                               "src-path",
                                                               "the search path of ADL,IDL and implementation files (list of path separated by '"
                                                                   + File.pathSeparator
                                                                   + "')",
                                                               "<path list>");

  protected final CmdArgument   outDirOpt                  = new CmdArgument(
                                                               "o",
                                                               "out-path",
                                                               "the path where generated files will be put",
                                                               "<output path>",
                                                               ".", false);

  protected final CmdFlag       helpOpt                    = new CmdFlag("h",
                                                               "help",
                                                               "Print this help and exit");

  protected final CmdFlag       versionOpt                 = new CmdFlag("v",
                                                               "version",
                                                               "Print version number and exit");

  protected final Options       options                    = new Options();
  {
    options.addOptions(helpOpt, versionOpt, srcPathOpt, outDirOpt);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected ClassLoader getSourceClassLoader(final List<String> srcPath) {
    final List<String> validatedPaths = new ArrayList<String>(srcPath.size());

    // check source paths
    for (final String path : srcPath) {
      final File f = new File(path);
      if (!f.exists()) {
        System.out.println("Warning '" + f.getAbsolutePath()
            + "' source path can't be found ");
      } else if (!f.isDirectory()) {
        System.out.println("Warning: \"" + path
            + "\" is not a directory, path ignored.");
      } else {
        validatedPaths.add(path);
      }
    }

    // build URL array of source path
    final URL[] urls = new URL[validatedPaths.size()];
    for (int i = 0; i < urls.length; i++) {
      final String path = validatedPaths.get(i);
      final File f = new File(path);
      try {
        urls[i] = f.toURI().toURL();
      } catch (final MalformedURLException e) {
        // never append
        throw new Error(e);
      }
    }

    return new URLClassLoader(urls, getClass().getClassLoader());
  }

  protected ClassLoader getSourceClassLoader(final CommandLine cmdLine) {
    List<String> srcPath = srcPathOpt.getPathValue(cmdLine);
    if (srcPath == null) {
      srcPath = new ArrayList<String>(1);
      srcPath.add(".");
    }

    return getSourceClassLoader(srcPath);
  }

  protected static File newBuildDir(final File outDir, final String dirName)
      throws InvalidCommandLineException {
    final File d = new File(outDir, dirName);
    checkDir(d);
    return d;
  }

  protected static File newBuildDir(final String name)
      throws InvalidCommandLineException {
    final File d = new File(name);
    checkDir(d);
    return d;
  }

  protected static void checkDir(final File d)
      throws InvalidCommandLineException {
    if (d.exists() && !d.isDirectory())
      throw new InvalidCommandLineException("Invalid build directory '"
          + d.getAbsolutePath() + "' not a directory", 6);
  }

  protected static List<String> parsePathList(String paths) {
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

  protected static Map<String, List<String>> argsToMap(final String... args)
      throws InvalidCommandLineException {
    final Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    List<String> nonOptList = null;

    for (final String arg : args) {
      if (arg.startsWith("-")) {

        final String argName;
        final String argValue;

        final int startIndex;
        if (arg.startsWith("--"))
          startIndex = 2;
        else
          startIndex = 1;

        final int index = arg.indexOf('=');
        if (index == -1) {
          argName = arg.substring(startIndex);
          argValue = null;
        } else {
          if (index < startIndex + 1) {
            throw new InvalidCommandLineException("Invalid option '" + arg
                + "'", 1);
          }
          argName = arg.substring(startIndex, index);
          argValue = arg.substring(index + 1);
        }

        List<String> l = map.get(argName);
        if (l == null) {
          l = new ArrayList<String>();
          map.put(argName, l);
        }
        l.add(argValue);

      } else {
        if (nonOptList == null) {
          nonOptList = new ArrayList<String>();
          map.put(null, nonOptList);
        }
        nonOptList.add(arg);
      }
    }

    return map;
  }

  protected String getVersion() {
    final String pkgVersion = this.getClass().getPackage()
        .getImplementationVersion();
    return (pkgVersion == null) ? "unknown" : pkgVersion;
  }

  protected String getProgramName() {
    return System.getProperty(PROGRAM_NAME_PROPERTY_NAME, getClass().getName());
  }

  protected abstract void printUsage(PrintStream ps);

  protected void printVersion(final PrintStream ps) {
    ps.println(getProgramName() + " version " + getVersion());
  }

  protected void printHelp(final PrintStream ps) {
    printUsage(ps);
    ps.println();
    ps.println("Available options are :");
    int maxCol = 0;

    for (final CmdOption opt : options.getOptions()) {
      final int col = 2 + opt.getPrototype().length();
      if (col > maxCol) maxCol = col;
    }
    for (final CmdOption opt : options.getOptions()) {
      final StringBuffer sb = new StringBuffer("  ");
      sb.append(opt.getPrototype());
      while (sb.length() < maxCol)
        sb.append(' ');
      sb.append("  ").append(opt.getDescription());
      ps.println(sb);
    }
  }

  // ---------------------------------------------------------------------------
  // Internal classes
  // ---------------------------------------------------------------------------

  /**
   * Exception thrown when an error on the command line has been detected.
   */
  public static class InvalidCommandLineException extends Exception {

    protected final int exitValue;

    /**
     * @param message detail message.
     * @param exitValue exit value.
     */
    public InvalidCommandLineException(final String message, final int exitValue) {
      super(message);
      this.exitValue = exitValue;
    }

    /**
     * @return the exit value.
     */
    public int getExitValue() {
      return exitValue;
    }
  }

  /** Set of available command-line options. */
  public static class Options {
    protected final Set<CmdOption>             optionSet          = new LinkedHashSet<CmdOption>();
    protected final Map<String, CmdOption>     optionsByShortName = new HashMap<String, CmdOption>();
    protected final Map<String, CmdOption>     optionsByLongName  = new HashMap<String, CmdOption>();
    protected final Map<String, CmdProperties> optionsByPrefix    = new HashMap<String, CmdProperties>();

    /**
     * Add an option
     * 
     * @param option an option to add.
     */
    public void addOption(final CmdOption option) {
      if (option instanceof CmdProperties) {
        final CmdOption prevOpt = optionsByPrefix.put(option.shortName,
            (CmdProperties) option);
        if (prevOpt != null || optionsByShortName.containsKey(option.shortName)) {
          throw new IllegalArgumentException("short name '" + option.shortName
              + "' already used");
        }
      } else {
        if (option.shortName != null) {
          final CmdOption prevOpt = optionsByShortName.put(option.shortName,
              option);
          if (prevOpt != null || optionsByPrefix.containsKey(option.shortName)) {
            throw new IllegalArgumentException("short name '"
                + option.shortName + "' already used");
          }
        }
        if (option.longName != null) {
          final CmdOption prevOpt = optionsByLongName.put(option.longName,
              option);
          if (prevOpt != null) {
            throw new IllegalArgumentException("long name '" + option.longName
                + "' already used");
          }
        }
      }

      optionSet.add(option);
    }

    /**
     * Add a set of options
     * 
     * @param options the options to add.
     */
    public void addOptions(final CmdOption... options) {
      for (final CmdOption option : options) {
        addOption(option);
      }
    }

    /** @return the available options. */
    public Collection<CmdOption> getOptions() {
      return optionSet;
    }

    CmdOption getByShortName(final String shortName) {
      return optionsByShortName.get(shortName);
    }

    CmdOption getByLongName(final String longName) {
      return optionsByLongName.get(longName);
    }

    CmdOption getByName(final String name) {
      final String prefix = name.substring(0, 1);

      CmdOption option = optionsByPrefix.get(prefix);
      if (option != null) return option;

      option = optionsByShortName.get(name);
      if (option != null) return option;

      return optionsByLongName.get(name);
    }
  }

  /**
   * A command line is the result of parsing a list of string arguments with a
   * set of options.
   */
  public static class CommandLine {
    protected final Options                options;
    protected final Map<CmdOption, Object> optionValues = new LinkedHashMap<CmdOption, Object>();
    protected final List<String>           arguments    = new ArrayList<String>();

    /**
     * Parse the given arguments to a CommandLine.
     * 
     * @param options the available options.
     * @param allowUnknownOption if true, unrecognized options will be added to
     *          list of arguments.
     * @param args the list of argument to parse.
     * @return a CommandLine object.
     * @throws InvalidCommandLineException if the list of argument is invalid.
     */
    public static CommandLine parseArgs(final Options options,
        final boolean allowUnknownOption, final String... args)
        throws InvalidCommandLineException {
      final CommandLine cmdLine = new CommandLine(options);

      for (final String arg : args) {
        if (arg.startsWith("-")) {

          final String argName;
          final String argValue;

          boolean longName;
          final int startIndex;
          if (arg.startsWith("--")) {
            startIndex = 2;
            longName = true;
          } else {
            startIndex = 1;
            longName = false;
          }

          final int index = arg.indexOf('=');
          if (index == -1) {
            argName = arg.substring(startIndex);
            argValue = null;
          } else {
            if (index < startIndex + 1) {
              throw new InvalidCommandLineException("Invalid option '" + arg
                  + "'", 1);
            }
            argName = arg.substring(startIndex, index);
            argValue = arg.substring(index + 1);
          }

          final CmdOption opt;
          if (longName)
            opt = cmdLine.options.getByLongName(argName);
          else
            opt = cmdLine.options.getByName(argName);

          if (opt == null) {
            if (allowUnknownOption) {
              cmdLine.arguments.add(arg);
            } else {
              throw new InvalidCommandLineException("Unknown option '"
                  + argName + "'", 1);
            }
          } else {
            if (opt instanceof CmdFlag) {
              if (argValue != null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' do not accept value", 1);
              }

              ((CmdFlag) opt).setPresent(cmdLine);

            } else if (opt instanceof CmdProperties) {
              if (argValue == null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' expects a value", 1);
              }
              ((CmdProperties) opt).setValue(cmdLine, argName.substring(1),
                  argValue);
            } else { // opt instanceof CmdArgument
              if (argValue == null) {
                throw new InvalidCommandLineException("Invalid option '"
                    + argName + "' expects a value", 1);
              }
              ((CmdArgument) opt).setValue(cmdLine, argValue);
            }
          }
        } else {
          cmdLine.arguments.add(arg);
        }
      }
      return cmdLine;
    }

    protected CommandLine(final Options options) {
      this.options = options;
    }

    protected Object setOptionValue(final CmdOption option, final Object value) {
      return optionValues.put(option, value);
    }

    /** @return the list of arguments. */
    public List<String> getArguments() {
      return arguments;
    }

    /**
     * @param option an option.
     * @return <code>true</code> is the given option is present on this command
     *         line.
     */
    public boolean isOptionPresent(final CmdOption option) {
      return optionValues.containsKey(option);
    }

    Object getOptionValue(final CmdOption option) {
      return optionValues.get(option);
    }
  }

  /**
   * Base class of command line options.
   */
  public abstract static class CmdOption {
    protected final String shortName;
    protected final String longName;
    protected final String description;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     */
    public CmdOption(final String shortName, final String longName,
        final String description) {
      if (shortName == null && longName == null)
        throw new IllegalArgumentException("Invalid option names");
      if (shortName != null && shortName.length() > 1)
        throw new IllegalArgumentException("Invalid shortName");
      if (longName != null && longName.length() <= 1)
        throw new IllegalArgumentException("Invalid longName");

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
  }

  /**
   * An option that may be present or not on a command line.
   */
  public static class CmdFlag extends CmdOption {

    /** @see CmdOption#CmdOption(String, String, String) */
    public CmdFlag(final String shortName, final String longName,
        final String description) {
      super(shortName, longName, description);
    }

    void setPresent(final CommandLine commandLine) {
      commandLine.setOptionValue(this, "");
    }
  }

  /**
   * A command line option that have a value.
   */
  public static class CmdArgument extends CmdOption {
    protected final String  argDesc;
    protected final String  defaultValue;
    protected final boolean allowMultiple;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     * @param defaultValue the default value of this option. May be
     *          <code>null</code>.
     * @param allowMultiple if <code>true</code>, this option can be specified
     *          several time on a command-line. In that case, the last
     *          occurrence is used.
     */
    public CmdArgument(final String shortName, final String longName,
        final String description, final String argDesc,
        final String defaultValue, final boolean allowMultiple) {
      super(shortName, longName, (defaultValue == null)
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
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdArgument(final String shortName, final String longName,
        final String description, final String argDesc) {
      this(shortName, longName, description, argDesc, null, false);
    }

    void setValue(final CommandLine commandLine, final String value)
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

    @Override
    public String getDescription() {
      if (allowMultiple)
        return super.getDescription()
            + ". This option may be specified several times.";
      else
        return super.getDescription();
    }
  }

  /**
   * An option that associate name to value.
   */
  public static class CmdProperties extends CmdOption {
    protected final String argNameDesc;
    protected final String argValueDesc;

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argNameDesc the description of the argument name (used to generate
     *          help message).
     * @param argValueDesc the description of the argument name (used to
     *          generate help message).
     */
    public CmdProperties(final String shortName, final String description,
        final String argNameDesc, final String argValueDesc) {
      super(shortName, null, description
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

  /**
   * An option that have a value and that may be specified several time on a
   * command-line. The resulting option value is the concatenation of the values
   * of each occurrence of this option.
   */
  public static class CmdAppendOption extends CmdArgument {

    protected final String separator;

    /**
     * Constructor for CmdAppendOption that has no default value and that use
     * <code>" "</code> as separator.
     * 
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdAppendOption(final String shortName, final String longName,
        final String description, final String argDesc) {
      this(shortName, longName, description, argDesc, null, " ");
    }

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     * @param defaultValue the default value of this option. May be
     *          <code>null</code>.
     * @param separator the string used to separate individual value.
     */
    public CmdAppendOption(final String shortName, final String longName,
        final String description, final String argDesc,
        final String defaultValue, final String separator) {
      super(shortName, longName, description, argDesc, defaultValue, true);
      this.separator = separator;
    }

    @Override
    void setValue(final CommandLine commandLine, final String value)
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

  /**
   * An option that have a value and that may be specified several time on a
   * command-line. The resulting option value is the concatenation of the values
   * of each occurrence of this option separated by {@link File#pathSeparator}.
   */
  public static class CmdPathOption extends CmdAppendOption {

    /**
     * @param shortName the short name of the option. Must have one and only one
     *          character. May be <code>null</code>.
     * @param longName the long name of the option. Must have more than one
     *          character. May be <code>null</code>.
     * @param description the description of the option (used to generate help
     *          message).
     * @param argDesc the description of the argument value (used to generate
     *          help message).
     */
    public CmdPathOption(final String shortName, final String longName,
        final String description, final String argDesc) {
      super(shortName, longName, description, argDesc, null, File.pathSeparator);
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
  }
}
