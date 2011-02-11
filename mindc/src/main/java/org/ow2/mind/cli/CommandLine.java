
package org.ow2.mind.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command line is the result of parsing a list of string arguments with a set
 * of options.
 */
public class CommandLine {
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
            throw new InvalidCommandLineException("Unknown option '" + argName
                + "'", 1);
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

  /**
   * @param optionId an option identifier.
   * @return <code>true</code> is the given option is present on this command
   *         line.
   */
  public boolean isOptionPresent(final String optionId) {
    final CmdOption option = options.getById(optionId);
    if (option == null) return false;
    return optionValues.containsKey(option);
  }

  /**
   * Returns the available options that can be used on the command line.
   * 
   * @return the available options that can be used on the command line.
   */
  public Options getOptions() {
    return options;
  }

  Object getOptionValue(final CmdOption option) {
    return optionValues.get(option);
  }
}