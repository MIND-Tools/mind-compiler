
package org.ow2.mind.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;

/** Set of available command-line options. */
public class Options {
  protected String                           usageDescription;
  protected final Map<String, CmdOption>     optionsById        = new HashMap<String, CmdOption>();
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
    CmdOption prevOpt = optionsById.put(option.getId(), option);
    if (prevOpt != null) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, "id '" + option.id
          + "' already used");
    }
    if (option instanceof CmdProperties) {
      prevOpt = optionsByPrefix.put(option.shortName, (CmdProperties) option);
      if (prevOpt != null || optionsByShortName.containsKey(option.shortName)) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR, "short name '"
            + option.shortName + "' already used");
      }
    } else {
      if (option.shortName != null) {
        prevOpt = optionsByShortName.put(option.shortName, option);
        if (prevOpt != null || optionsByPrefix.containsKey(option.shortName)) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR, "short name '"
              + option.shortName + "' already used");
        }
      }
      if (option.longName != null) {
        prevOpt = optionsByLongName.put(option.longName, option);
        if (prevOpt != null) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR, "long name '"
              + option.longName + "' already used");
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

  /**
   * Add a set of options
   * 
   * @param options the options to add.
   */
  public void addOptions(final Iterable<CmdOption> options) {
    for (final CmdOption option : options) {
      addOption(option);
    }
  }

  /** @return the available options. */
  public Collection<CmdOption> getOptions() {
    return optionSet;
  }

  /**
   * Returns the option with the given identifier.
   * 
   * @param id an option identifier.
   * @return the option with the given identifier or <code>null</code>.
   */
  public CmdOption getById(final String id) {
    return optionsById.get(id);
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

  /**
   * @return the usageDescription
   */
  public String getUsageDescription() {
    return usageDescription;
  }

  /**
   * @param usageDescription the usageDescription to set
   */
  public void setUsageDescription(final String usageDescription) {
    this.usageDescription = usageDescription;
  }

}