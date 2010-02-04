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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.unit;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class UnitTestDataProvider {
  private UnitTestDataProvider() {
  }

  public static final String INCLUDE_TEST_PROPERTY_NAME = "includeTest";

  public static Object[][] listADLs(final String... rootDirs) throws Exception {
    final String includeTest = System.getProperty(INCLUDE_TEST_PROPERTY_NAME);
    final Pattern regexp;
    if (includeTest != null)
      regexp = Pattern.compile(includeTest);
    else
      regexp = null;

    final Map<String, List<String>> adls = new HashMap<String, List<String>>(
        rootDirs.length);
    int nbADL = 0;
    for (final String rootDir : rootDirs) {
      final URL root = UnitTestDataProvider.class.getClassLoader().getResource(
          rootDir);
      if (root == null) {
        System.err
            .println("Warning : can't find root dir \"" + rootDir + "\".");
        continue;
      }
      final File testDir = new File(root.toURI());
      assert testDir.isDirectory();
      final List<String> adlNameList = new ArrayList<String>();
      listADLs(testDir, "", regexp, adlNameList);
      adls.put(rootDir, adlNameList);
      nbADL += adlNameList.size();
    }

    final Object[][] result = new Object[nbADL][2];
    for (final Map.Entry<String, List<String>> entry : adls.entrySet()) {
      for (int i = 0; i < entry.getValue().size(); i++) {
        result[i][0] = entry.getKey();
        result[i][1] = entry.getValue().get(i);
      }
    }
    return result;
  }

  // Recursively looks in sub directories of 'dir' and returns all files with
  // .adl extension.
  private static void listADLs(final File dir, final String prefix,
      final Pattern regexp, final List<String> list) throws Exception {
    assert dir.isDirectory();
    final String[] adlNames = dir.list(new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return name.endsWith(".adl");
      }
    });

    if (adlNames != null) {
      for (final String adlName : adlNames) {
        final String name = prefix + adlName.substring(0, adlName.length() - 4);
        if (regexp != null && !regexp.matcher(name).matches()) continue;
        list.add(name);
      }
    }
    final File[] subDirs = dir.listFiles(new FilenameFilter() {
      public boolean accept(final File dir, final String name) {
        return (new File(dir, name)).isDirectory();
      }
    });
    if (subDirs != null) {
      for (final File subDir : subDirs) {
        listADLs(subDir, prefix + subDir.getName() + ".", regexp, list);

      }
    }
  }
}
