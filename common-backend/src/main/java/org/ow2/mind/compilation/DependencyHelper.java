/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.compilation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.ForceRegenContextHelper;

public final class DependencyHelper {

  protected static Logger depLogger = FractalADLLogManager.getLogger("dep");

  private DependencyHelper() {
  }

  public static boolean recompile(final File outputFile,
      final File dependencyFile, final Map<Object, Object> context,
      final File... additionalDependencies) {
    if (context != null && ForceRegenContextHelper.getForceRegen(context))
      return true;

    if (!outputFile.exists()) {
      if (depLogger.isLoggable(Level.FINE))
        depLogger.fine("Output file '" + outputFile
            + "' does not exist, compile it.");
      return true;
    }
    final long outputTimestamp = outputFile.lastModified();
    if (outputTimestamp == 0) {
      if (depLogger.isLoggable(Level.WARNING))
        depLogger.warning("Unable to determine timestamp of file '"
            + outputFile + "', recompile.");
      return true;
    }

    if (dependencyFile != null) {
      if (!dependencyFile.exists()) {
        if (depLogger.isLoggable(Level.FINE))
          depLogger.fine("Dependency file of '" + outputFile
              + "' does not exist, recompile.");
        return true;
      }

      final Map<File, List<File>> depMap = parseDepFile(dependencyFile);
      if (depMap == null) {
        if (depLogger.isLoggable(Level.FINE))
          depLogger.fine("Error in dependency file of '" + outputFile
              + "', recompile.");
        return true;
      }

      Collection<File> depFiles = depMap.get(outputFile);

      // if depFiles is null (i.e. the dependencyFile is invalid), recompile.
      if (depFiles == null) {
        // try with absolute path
        depFiles = depMap.get(outputFile.getAbsoluteFile());

        if (depFiles == null) {
          // try with single file name
          depFiles = depMap.get(new File(outputFile.getName()));

          if (depFiles == null) {
            if (depLogger.isLoggable(Level.WARNING))
              depLogger.warning("Invalid dependency file '" + dependencyFile
                  + "'. Can't find rule for target '" + outputFile
                  + "', recompile.");
            return true;
          }
        }

      }

      for (final File depfile : depFiles) {
        if (!depfile.exists()) {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Missing input file '" + depfile + "'.");
        } else if (depfile.lastModified() > outputTimestamp) {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Input file '" + depfile
                + "' is more recent than output file '" + outputFile
                + "', recompile.");
          return true;
        }
      }
    }

    if (additionalDependencies != null) {
      for (final File depfile : additionalDependencies) {
        if (!depfile.exists()) {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Missing input file '" + depfile + "'.");
        } else if (depfile.lastModified() > outputTimestamp) {
          if (depLogger.isLoggable(Level.FINE))
            depLogger.fine("Input file '" + depfile
                + "' is more recent than output file '" + outputFile
                + "', recompile.");
          return true;
        }
      }
    }

    if (depLogger.isLoggable(Level.FINE))
      depLogger.fine("Output file '" + outputFile
          + "' is up to date, do not recompile.");
    return false;
  }

  public static void writeDepFile(final File depFile,
      final Map<File, List<File>> deps) throws IOException {
    final PrintStream ps = new PrintStream(depFile);
    for (final Map.Entry<File, List<File>> dep : deps.entrySet()) {
      ps.printf("%s : ", dep.getKey().getPath());
      final Iterator<File> iter = dep.getValue().iterator();
      while (iter.hasNext()) {
        final File file = iter.next();
        ps.print(file.getPath());
        if (iter.hasNext()) {
          ps.print(" \\\n    ");
        } else {
          ps.print("\n\n");
        }
      }
    }
    ps.close();
  }

  /**
   * The old parseDepFile method parsed the make rule as one line, with entries
   * separated by the space character. This is the pure "make" rule, however
   * it's an issue with folders containing spaces, since the GCC dependency file
   * generation doesn't use quotation marks to differentiate the entries. The
   * new parseDepFile was developed to split the rule with '\' + newline, We
   * follow the convention as mentioned in GCC's documentation. @see
   * {@link https ://gcc.gnu.org/onlinedocs/gcc/Preprocessor-Options.html}.
   * Concerning the -M option: Unless specified explicitly (with -MT or -MQ),
   * the object file name consists of the name of the source file with any
   * suffix replaced with object file suffix and with any leading directory
   * parts removed. If there are many included files then the rule is split into
   * several lines using ‘\’-newline.
   * 
   * @param depfile
   * @return
   */
  public static Map<File, List<File>> parseDepFile(final File depfile) {
    final Map<File, List<File>> rules = new HashMap<File, List<File>>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(depfile));
      String line = null;
      List<String> ruleLines = new ArrayList<String>();
      while ((line = reader.readLine()) != null) {
        if (line.endsWith("\\")) {
          line = line.substring(0, line.length() - 1);
          if (line.length() > 0) ruleLines.add(line);
        } else {
          if (line.length() > 0) ruleLines.add(line);

          // end of rule, start a new one
          if (ruleLines.size() > 0) {
            parseRule(ruleLines, rules);
            ruleLines = new ArrayList<String>();
          }
        }
      }
      if (ruleLines.size() > 0) parseRule(ruleLines, rules);
    } catch (final IOException e) {
      depLogger.log(Level.WARNING, "An error occurs while reading \"" + depfile
          + "\".", e);
      return null;
    } finally {
      if (reader != null) try {
        reader.close();
      } catch (final IOException e) {
        // ignore
      }
    }
    return rules;
  }

  private static void parseRule(final List<String> ruleLines,
      final Map<File, List<File>> rules) {
    final String[] line0parts = ruleLines.get(0).split(":\\s+");
    if (line0parts.length > 2) {
      throw new IllegalArgumentException("Erroneous rule target format");
    }

    String target = line0parts[0].trim();

    ruleLines.remove(0);
    if (line0parts.length == 2) ruleLines.add(line0parts[1]);

    final List<File> dependencies = new ArrayList<File>();

    // Split dependency part.
    for (String dependency : ruleLines) {
      // un-escape dollar signs.
      dependency = DirectiveHelper.formatOptionString(dependency.replace("$$",
          "$").trim());
      final File depFile = new File(dependency);
      if (dependency.length() > 0 && depFile.exists())
        dependencies.add(depFile);
    }

    // un-escape dollar signs.
    target = target.replace("$$", "$");
    rules.put(new File(target), dependencies);
  }
}
