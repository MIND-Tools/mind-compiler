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

package org.ow2.mind;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.io.BasicOutputFileLocator;

public abstract class AbstractIncrementalTest extends AbstractFunctionalTest {

  protected static final String SRC_ROOT = "incremental";
  protected static File         buildDir = new File(
                                             "target/build/incremental-test");

  protected static void assertUnchanged(final String path,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Long time1 = timestamps1.get(path);
    assertNotNull(time1, "missing timestamp of \"" + path + "\" in first map");
    final Long time2 = timestamps2.get(path);
    assertNotNull(time2, "missing timestamp of \"" + path + "\" in second map");
    assertTrue(time1.equals(time2), "Timestamp of \"" + path + "\" has changed");
    timestamps1.remove(path);
    timestamps2.remove(path);
  }

  protected void pause() throws InterruptedException {
    // wait a second and half to be sure that timestamps are actually modified
    synchronized (this) {
      final long begin = System.currentTimeMillis();
      long t = System.currentTimeMillis();
      while (t - begin < 1500) {
        this.wait(begin - t + 1500);
        t = System.currentTimeMillis();
      }
    }
  }

  protected Map<String, Long> recompileDefinition(final String adlName)
      throws ADLException, InterruptedException {
    initContext(false);
    runner.compileDefinition(adlName);
    return getBuildTimestamps();
  }

  protected Map<String, Long> recompile(final String adlName)
      throws ADLException, InterruptedException {
    return recompile(adlName, null);
  }

  protected Map<String, Long> recompile(final String adlName,
      final String execName) throws ADLException, InterruptedException {
    initContext(false);
    runner.compile(adlName, execName);
    return getBuildTimestamps();
  }

  protected static void assertUnchangedAll(final String pattern,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Pattern p = Pattern.compile(pattern);
    boolean foundMatches = false;
    for (final Map.Entry<String, Long> t1 : new HashSet<Map.Entry<String, Long>>(
        timestamps1.entrySet())) {
      if (p.matcher(t1.getKey()).matches()) {
        final Long t2 = timestamps2.get(t1.getKey());
        if (t2 != null) {
          foundMatches = true;
          assertUnchanged(t1.getKey(), timestamps1, timestamps2);
        }
      }
    }
    assertTrue(foundMatches, "No match found for pattern \"" + pattern + "\"");
  }

  protected static void assertChanged(final String path,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Long time1 = timestamps1.get(path);
    assertNotNull(time1, "missing timestamp of \"" + path + "\" in first map");
    final Long time2 = timestamps2.get(path);
    assertNotNull(time2, "missing timestamp of \"" + path + "\" in second map");
    assertTrue(time1 < time2, "Timestamp of \"" + path + "\" is unchanged");
    timestamps1.remove(path);
    timestamps2.remove(path);
  }

  protected static void assertChangedAll(final String pattern,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Pattern p = Pattern.compile(pattern);
    boolean foundMatches = false;
    for (final Map.Entry<String, Long> t1 : new HashSet<Map.Entry<String, Long>>(
        timestamps1.entrySet())) {
      if (p.matcher(t1.getKey()).matches()) {
        final Long t2 = timestamps2.get(t1.getKey());
        if (t2 != null) {
          foundMatches = true;
          assertChanged(t1.getKey(), timestamps1, timestamps2);
        }
      }
    }
    assertTrue(foundMatches, "No match found for pattern \"" + pattern + "\"");
  }

  protected void touchFile(final String path) throws Exception {
    final URL resource = getClass().getClassLoader().getResource(
        SRC_ROOT + "/" + path);
    assertNotNull(resource);
    final File file = new File(resource.toURI());
    final long t1 = file.lastModified();
    file.setLastModified(System.currentTimeMillis());
    assertTrue(file.lastModified() > t1, "Touch of file \"" + path
        + "\" failed.");
  }

  protected void copyFile(final String path, final String newPath,
      final String[]... replacements) throws Exception {
    final URL resource = getClass().getClassLoader().getResource(
        SRC_ROOT + "/" + path);
    assertNotNull(resource);
    final File srcFile = new File(resource.toURI());
    final File newFile = new File(srcFile.getPath().replace(
        path.replace('/', File.separatorChar), newPath));

    LineNumberReader reader = null;
    PrintWriter writer = null;
    try {
      reader = new LineNumberReader(new FileReader(srcFile));
      writer = new PrintWriter(newFile);
      String line = reader.readLine();
      while (line != null) {
        if (replacements != null) {
          for (final String[] replacement : replacements) {
            assert replacement.length == 2;
            line = line.replaceAll(replacement[0], replacement[1]);
          }
        }
        writer.println(line);
        line = reader.readLine();
      }
    } finally {
      if (reader != null) reader.close();
      if (writer != null) writer.close();
    }
  }

  protected void initContext(final boolean force) throws ADLException {
    // delete previous temporary directory.
    if (runner.context != null) {
      final File tempDir = (File) runner.context
          .get(BasicOutputFileLocator.TEMPORARY_OUTPUT_DIR_CONTEXT_KEY);
      if (tempDir != null) {
        deleteDir(tempDir);
      }
    }

    runner.initContext();
    initPath();

    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    runner.context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);
    ForceRegenContextHelper.setForceRegen(runner.context, force);
    ForceRegenContextHelper.setKeepTemp(runner.context, false);
    ForceRegenContextHelper.setNoBinaryAST(runner.context, false);
  }

  protected abstract void initPath();

  protected void cleanBuildDir() {
    if (buildDir.exists()) deleteDir(buildDir);
  }

  protected HashMap<String, Long> getBuildTimestamps() {
    final HashMap<String, Long> timestamps = new HashMap<String, Long>();
    for (final File subFile : buildDir.listFiles()) {
      getTimestamps(subFile, "", timestamps);
    }
    return timestamps;
  }

  protected void deleteDir(final File f) {
    if (f.isDirectory()) {
      for (final File subFile : f.listFiles())
        deleteDir(subFile);
    }
    // f.delete();
    assertTrue(f.delete(), "Can't delete \"" + f + "\".");
  }

  protected void getTimestamps(final File f, final String path,
      final Map<String, Long> timestamps) {
    final String name = path + f.getName();
    if (f.isDirectory()) {
      final String subPath = name + "/";
      for (final File subFile : f.listFiles()) {
        getTimestamps(subFile, subPath, timestamps);
      }
    } else {
      timestamps.put(name, f.lastModified());
    }
  }

}