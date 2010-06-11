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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

  public static Map<File, List<File>> parseDepFile(final File depfile) {
    final Map<File, List<File>> rules = new HashMap<File, List<File>>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(depfile));
      String line = null, rule = "";
      while ((line = reader.readLine()) != null) {
        if (line.endsWith("\\")) {
          rule += line.substring(0, line.length() - 1);
        } else {
          rule += line.trim();

          // end of rule, start a new one
          if (rule.length() > 0) {
            parseRule(rule, rules);
            rule = "";
          }
        }
      }
      if (rule.length() > 0) parseRule(rule, rules);
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

  private static void parseRule(final String rule,
      final Map<File, List<File>> rules) {
    final String[] parts = rule.split(":\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Can't find rule target");
    }
    // Split target part.
    final List<String> targets = DirectiveHelper.splitOptionString(parts[0]
        .trim());
    final List<File> dependencies = new ArrayList<File>();
    // Split dependency part.
    for (String dependency : DirectiveHelper.splitOptionString(parts[1])) {
      // un-escape dollar signs.
      dependency = dependency.replace("$$", "$");
      if (dependency.length() > 0) dependencies.add(new File(dependency));
    }
    for (final String target : targets) {
      rules.put(new File(target), dependencies);
    }
  }
}
