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
import java.util.Map;
import java.util.regex.Pattern;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.testng.annotations.Test;

public class IncrementalTest extends AbstractFunctionalTest {

  static final String SRC_ROOT = "incremental";
  static File         buildDir = new File("target/build/incremental-test");

  @Test(groups = {"functional"})
  public void incrementalTest1() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    final Map<String, Long> t2 = recompile("helloworld.HelloworldApplication");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest11() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    initContext(true);
    runner.compile("helloworld.HelloworldApplication");
    final Map<String, Long> t2 = getBuildTimestamps();
    assertChangedAll(".*\\.o", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest2() throws Exception {
    cleanBuildDir();
    initContext(true);
    runner.compile("helloworld.HelloworldApplication");
    final Map<String, Long> t1 = getBuildTimestamps();

    pause();
    Map<String, Long> t2 = recompileDefinition("helloworld.Helloworld");
    assertUnchangedAll(".*", t1, t2);

    pause();
    touchFile("helloworld/Client.adl");
    t2 = recompileDefinition("helloworld.Helloworld");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest21() throws Exception {
    cleanBuildDir();
    initContext(true);
    runner.compile("helloworld.HelloworldApplication");
    final Map<String, Long> t1 = getBuildTimestamps();

    pause();
    Map<String, Long> t2 = recompileDefinition("helloworld.Helloworld");
    assertUnchangedAll("helloworld/Helloworld_ctrl_impl.*", t1, t2);

    pause();
    touchFile("helloworld/client.c");
    t2 = recompileDefinition("helloworld.Client");
    assertUnchangedAll("helloworld/Client_ctrl.*", t1, t2);
    assertChangedAll("helloworld/Client_impl0.*", t1, t2);

    pause();
    final Map<String, Long> t3 = recompileDefinition("helloworld.Client");
    assertUnchangedAll("helloworld/Helloworld_ctrl_impl.*", t2, t3);
  }

  @Test(groups = {"functional"})
  public void incrementalTest3() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompileDefinition("helloworld.ClientInlined");

    Map<String, Long> t2 = recompileDefinition("helloworld.ClientInlined");
    assertUnchangedAll("helloworld/ClientInlined.*", t1, t2);

    touchFile("helloworld/ClientInlined.adl");
    t2 = recompileDefinition("helloworld.ClientInlined");
    assertUnchangedAll("helloworld/ClientInlined.*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest4() throws Exception {
    cleanBuildDir();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"});

    final Map<String, Long> t1 = recompileDefinition("helloworld.ClientInlined_modified");

    pause();
    Map<String, Long> t2 = recompileDefinition("helloworld.ClientInlined_modified");
    assertUnchangedAll("helloworld/ClientInlined_modified.*", t1, t2);

    pause();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"},
        new String[]{"hello world", "Hello World !"});
    t2 = recompileDefinition("helloworld.ClientInlined_modified");
    assertChangedAll("helloworld/ClientInlined_modified_impl0.*", t1, t2);
    assertUnchangedAll("helloworld/ClientInlined_modified_ctrl.*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest5() throws Exception {
    cleanBuildDir();
    copyFile("helloworld/Client.adl", "helloworld/Client_modified.adl",
        new String[]{"helloworld\\.Client", "helloworld.Client_modified"},
        new String[]{"source client\\.c", "source client_modified.c"});
    copyFile("helloworld/client.c", "helloworld/client_modified.c");

    copyFile("helloworld/Helloworld.adl", "helloworld/Helloworld_modified.adl",
        new String[]{"helloworld\\.Helloworld",
            "helloworld.Helloworld_modified"}, new String[]{"contains Client",
            "contains Client_modified"});

    final Map<String, Long> t1 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");

    pause();
    copyFile("helloworld/client.c", "helloworld/client_modified.c",
        new String[]{"hello world", "Hello World !"});
    final Map<String, Long> t2 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");
    assertUnchangedAll("helloworld/Helloworld.*", t1, t2);
    assertChangedAll("helloworld/Client_modified_impl0.*", t1, t2);
    assertUnchangedAll("helloworld/Client_modified_ctrl.*", t1, t2);
    assertUnchangedAll("GenericApplication_.*", t1, t2);
    assertChangedAll("Helloworld_modified(\\.exe)?", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest51() throws Exception {
    cleanBuildDir();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"});

    copyFile("helloworld/Helloworld.adl", "helloworld/Helloworld_modified.adl",
        new String[]{"helloworld\\.Helloworld",
            "helloworld.Helloworld_modified"}, new String[]{"contains Client",
            "contains ClientInlined_modified"});

    final Map<String, Long> t1 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");

    pause();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"},
        new String[]{"hello world", "Hello World !"});
    final Map<String, Long> t2 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");
    assertUnchangedAll("helloworld/Helloworld.*", t1, t2);
    assertChangedAll("helloworld/ClientInlined_modified_impl0.*", t1, t2);
    assertUnchangedAll("helloworld/ClientInlined_modified_ctrl.*", t1, t2);
    assertUnchangedAll("GenericApplication_.*", t1, t2);
    assertChangedAll("Helloworld_modified(\\.exe)?", t1, t2);
  }

  private void pause() throws InterruptedException {
    synchronized (this) {
      // wait half a second to be sure that timestamp are actually modified
      this.wait(1500);
    }
  }

  private Map<String, Long> recompileDefinition(final String adlName)
      throws ADLException, InterruptedException {
    initContext(false);
    runner.compileDefinition(adlName);
    return getBuildTimestamps();
  }

  private Map<String, Long> recompile(final String adlName)
      throws ADLException, InterruptedException {
    return recompile(adlName, null);
  }

  private Map<String, Long> recompile(final String adlName,
      final String execName) throws ADLException, InterruptedException {
    initContext(false);
    runner.compile(adlName, execName);
    return getBuildTimestamps();
  }

  protected static void assertUnchanged(final String path,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Long time1 = timestamps1.get(path);
    assertNotNull(time1, "missing timestamp of \"" + path + "\" in first map");
    final Long time2 = timestamps2.get(path);
    assertNotNull(time2, "missing timestamp of \"" + path + "\" in second map");
    assertTrue(time1.equals(time2), "Timestamp of \"" + path + "\" has changed");
  }

  protected static void assertUnchangedAll(final String pattern,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Pattern p = Pattern.compile(pattern);
    boolean foundMatches = false;
    for (final Map.Entry<String, Long> t1 : timestamps1.entrySet()) {
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
  }

  protected static void assertChangedAll(final String pattern,
      final Map<String, Long> timestamps1, final Map<String, Long> timestamps2) {
    final Pattern p = Pattern.compile(pattern);
    boolean foundMatches = false;
    for (final Map.Entry<String, Long> t1 : timestamps1.entrySet()) {
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
    final File newFile = new File(srcFile.getPath().replace(path, newPath));

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

  protected void initContext(final boolean force) {
    runner.initContext();
    initSourcePath(SRC_ROOT);

    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    runner.context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);
    ForceRegenContextHelper.setForceRegen(runner.context, force);
  }

  protected void cleanBuildDir() {
    if (buildDir.exists()) deleteDir(buildDir);
  }

  protected Map<String, Long> getBuildTimestamps() {
    final Map<String, Long> timestamps = new HashMap<String, Long>();
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
    }
    timestamps.put(name, f.lastModified());
  }
}
