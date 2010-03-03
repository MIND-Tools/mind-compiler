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
import org.testng.annotations.Test;

public class IncrementalTest extends AbstractFunctionalTest {

  static final String SRC_ROOT = "incremental";
  static File         buildDir = new File("target/build/incremental-test");

  @Test(groups = {"functional"})
  public void incrementalTest1() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompileDefinition("helloworld.Client");

    pause();
    final Map<String, Long> t2 = recompileDefinition("helloworld.Client");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest11() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    pause();
    final Map<String, Long> t2 = recompile("helloworld.HelloworldApplication");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest12() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    pause();
    final Map<String, Long> t2 = recompileDefinition("helloworld.Helloworld");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest13() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    initContext(true);
    runner.compile("helloworld.HelloworldApplication");
    final Map<String, Long> t2 = getBuildTimestamps();
    assertChangedAll(".*\\.o", t1, t2);
    assertChangedAll(".*\\.d", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest14() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("GenericApplication<helloworld.HelloworldMultiClient>");

    pause();
    final Map<String, Long> t2 = recompile("GenericApplication<helloworld.HelloworldMultiClient>");
    assertChanged("GenericApplication.map", t1, t2);
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest15() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("GenericApplication<helloworld.HelloworldControlled>");

    pause();
    final Map<String, Long> t2 = recompile("GenericApplication<helloworld.HelloworldControlled>");
    assertChanged("GenericApplication.map", t1, t2);
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest2() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    pause();
    Map<String, Long> t2 = recompile("helloworld.HelloworldApplication");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    touchFile("helloworld/Client.adl");
    pause();
    t2 = recompile("helloworld.HelloworldApplication");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertChanged("helloworld/Client.def", t1Copy, t2Copy);
    assertChanged("helloworld/Helloworld.def", t1Copy, t2Copy);
    assertChanged("helloworld/HelloworldApplication.def", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
  }

  @Test(groups = {"functional"})
  public void incrementalTest21() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    pause();
    Map<String, Long> t2 = recompile("helloworld.HelloworldApplication");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    touchFile("helloworld/client.c");
    pause();
    t2 = recompile("helloworld.HelloworldApplication");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll("helloworld/HelloworldApplication.*Client_instances.c",
        t1Copy, t2Copy);
    assertChangedAll("helloworld/HelloworldApplication.*Client.*", t1Copy,
        t2Copy);
    assertChangedAll("helloworld/HelloworldApplication(\\.exe)?", t1Copy,
        t2Copy);
    assertUnchangedAll("helloworld/Helloworld.*", t1Copy, t2Copy);
    assertChangedAll("helloworld/Client_impl0.*", t1Copy, t2Copy);
    assertUnchanged("helloworld/Client_ctrl_impl.c", t1Copy, t2Copy);
    assertChangedAll("helloworld/Client_ctrl_impl.*", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    final Map<String, Long> t3 = recompile("helloworld.HelloworldApplication");
    t2Copy = new HashMap<String, Long>(t2);
    final Map<String, Long> t3Copy = new HashMap<String, Long>(t3);
    assertUnchangedAll(".*", t2Copy, t3Copy);
  }

  @Test(groups = {"functional"})
  public void incrementalTest22() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("helloworld.HelloworldApplication");

    pause();
    Map<String, Long> t2 = recompile("helloworld.HelloworldApplication");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    touchFile("helloworld/Service.itf");
    pause();
    t2 = recompile("helloworld.HelloworldApplication");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertChanged("helloworld/Service.itfdef", t1Copy, t2Copy);
    assertChanged("helloworld/Client.def", t1Copy, t2Copy);
    assertChanged("helloworld/Server.def", t1Copy, t2Copy);
    assertChanged("helloworld/Helloworld.def", t1Copy, t2Copy);
    assertChanged("helloworld/HelloworldApplication.def", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    final Map<String, Long> t3 = recompile("helloworld.HelloworldApplication");
    t2Copy = new HashMap<String, Long>(t2);
    final Map<String, Long> t3Copy = new HashMap<String, Long>(t3);
    assertUnchangedAll(".*", t2Copy, t3Copy);
  }

  @Test(groups = {"functional"})
  public void incrementalTest3() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompileDefinition("helloworld.ClientInlined");

    Map<String, Long> t2 = recompileDefinition("helloworld.ClientInlined");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    touchFile("helloworld/ClientInlined.adl");
    pause();
    t2 = recompileDefinition("helloworld.ClientInlined");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertChanged("helloworld/ClientInlined.def", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
  }

  @Test(groups = {"functional"})
  public void incrementalTest4() throws Exception {
    cleanBuildDir();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"});
    pause();

    final Map<String, Long> t1 = recompileDefinition("helloworld.ClientInlined_modified");

    pause();
    Map<String, Long> t2 = recompileDefinition("helloworld.ClientInlined_modified");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"},
        new String[]{"hello world", "Hello World !"});
    pause();
    t2 = recompileDefinition("helloworld.ClientInlined_modified");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertChangedAll("helloworld/ClientInlined_modified_impl0.*", t1Copy,
        t2Copy);
    assertUnchanged("helloworld/ClientInlined_modified_ctrl_impl.c", t1Copy,
        t2Copy);
    assertChangedAll("helloworld/ClientInlined_modified_ctrl_impl.*", t1Copy,
        t2Copy);
    assertChanged("helloworld/ClientInlined_modified.def", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
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
    pause();

    final Map<String, Long> t1 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");

    pause();
    copyFile("helloworld/client.c", "helloworld/client_modified.c",
        new String[]{"hello world", "Hello World !"});
    pause();
    final Map<String, Long> t2 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");
    final Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    final Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertUnchangedAll("helloworld/Helloworld.*", t1Copy, t2Copy);
    assertChangedAll("helloworld/Client_modified_impl0.*", t1Copy, t2Copy);
    assertUnchanged("helloworld/Client_modified_ctrl_impl.c", t1Copy, t2Copy);
    assertChangedAll("helloworld/Client_modified_ctrl_impl.*", t1Copy, t2Copy);
    assertChangedAll("Helloworld_modified(\\.exe)?", t1Copy, t2Copy);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertUnchangedAll("GenericApplication.*Client_modified_instances.c",
        t1Copy, t2Copy);
    assertChangedAll("GenericApplication.*Client_modified.*", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
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
    pause();

    final Map<String, Long> t1 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");

    pause();
    copyFile("helloworld/ClientInlined.adl",
        "helloworld/ClientInlined_modified.adl", new String[]{
            "helloworld\\.ClientInlined", "helloworld.ClientInlined_modified"},
        new String[]{"hello world", "Hello World !"});
    pause();
    final Map<String, Long> t2 = recompile(
        "GenericApplication<helloworld.Helloworld_modified>",
        "Helloworld_modified");
    final Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    final Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertChanged("helloworld/ClientInlined_modified.def", t1Copy, t2Copy);
    assertChanged("helloworld/Helloworld_modified.def", t1Copy, t2Copy);
    assertUnchangedAll("helloworld/Helloworld.*", t1Copy, t2Copy);
    assertChangedAll("helloworld/ClientInlined_modified_impl0.*", t1Copy,
        t2Copy);
    assertUnchanged("helloworld/ClientInlined_modified_ctrl_impl.c", t1Copy,
        t2Copy);
    assertChangedAll("helloworld/ClientInlined_modified_ctrl_impl.*", t1Copy,
        t2Copy);
    assertChangedAll("Helloworld_modified(\\.exe)?", t1Copy, t2Copy);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertUnchangedAll(
        "GenericApplication.*ClientInlined_modified_instances.c", t1Copy,
        t2Copy);
    assertChangedAll("GenericApplication.*ClientInlined_modified.*", t1Copy,
        t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
  }

  private void pause() throws InterruptedException {
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
    timestamps1.remove(path);
    timestamps2.remove(path);
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

  protected void initContext(final boolean force) {
    // delete previous temporary directory.
    if (runner.context != null) {
      final File tempDir = (File) runner.context
          .get(BasicOutputFileLocator.TEMPORARY_OUTPUT_DIR_CONTEXT_KEY);
      if (tempDir != null) {
        deleteDir(tempDir);
      }
    }

    runner.initContext();
    initSourcePath(SRC_ROOT);

    if (!buildDir.exists()) {
      buildDir.mkdirs();
    }
    runner.context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, buildDir);
    ForceRegenContextHelper.setForceRegen(runner.context, force);
    ForceRegenContextHelper.setKeepTemp(runner.context, false);
  }

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
