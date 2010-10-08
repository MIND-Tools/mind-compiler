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

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

public class IncrementalTest extends AbstractIncrementalTest {

  @Override
  protected void initPath() {
    initSourcePath(getDepsDir("fractal/api/Component.itf").getAbsolutePath(),
        "common", SRC_ROOT);
  }

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

    pause();
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
  public void incrementalTest16() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("GenericApplication<helloworld.HelloworldFactoryClient>");

    pause();
    final Map<String, Long> t2 = recompile("GenericApplication<helloworld.HelloworldFactoryClient>");
    assertChanged("GenericApplication.map", t1, t2);
    assertChanged("Factory.map", t1, t2);
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest17() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompileDefinition("asm.Assembly");

    pause();
    final Map<String, Long> t2 = recompileDefinition("asm.Assembly");
    assertUnchangedAll(".*", t1, t2);
  }

  @Test(groups = {"functional"})
  public void incrementalTest18() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompileDefinition("asm.Assembly");

    pause();
    touchFile("asm/asm.s");
    pause();
    final Map<String, Long> t2 = recompileDefinition("asm.Assembly");
    assertChanged("asm/Assembly_impl1.o", t1, t2);
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
  public void incrementalTest31() throws Exception {
    cleanBuildDir();
    final Map<String, Long> t1 = recompile("GenericApplication<helloworld.HelloworldGeneric>");

    pause();
    Map<String, Long> t2 = recompile("GenericApplication<helloworld.HelloworldGeneric>");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertChanged("helloworld/GenericComposite.map", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    pause();
    touchFile("helloworld/ClientType.adl");
    pause();
    t2 = recompile("GenericApplication<helloworld.HelloworldGeneric>");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);
    assertChanged("helloworld/HelloworldGeneric.def", t1Copy, t2Copy);
    assertChangedAll("helloworld/GenericComposite.*\\.def", t1Copy, t2Copy);
    assertChangedAll("GenericApplication.*\\.def", t1Copy, t2Copy);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertChanged("helloworld/GenericComposite.map", t1Copy, t2Copy);
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
    assertChangedAll("GenericApplication.*\\.def", t1Copy, t2Copy);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertUnchangedAll(
        "GenericApplication.*ClientInlined_modified_instances.c", t1Copy,
        t2Copy);
    assertChangedAll("GenericApplication.*ClientInlined_modified.*", t1Copy,
        t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);
  }

  @Test(groups = {"functional"})
  public void incrementalTest6() throws Exception {
    cleanBuildDir();

    copyFile("helloworld/HelloworldAnonymous.adl",
        "helloworld/HelloworldAnonymous_modified.adl", new String[]{
            "helloworld\\.HelloworldAnonymous",
            "helloworld.HelloworldAnonymous_modified"});
    pause();

    final Map<String, Long> t1 = recompile(
        "GenericApplication<helloworld.HelloworldAnonymous_modified>",
        "HelloworldAnonymous_modified");

    pause();
    Map<String, Long> t2 = recompile(
        "GenericApplication<helloworld.HelloworldAnonymous_modified>",
        "HelloworldAnonymous_modified");
    Map<String, Long> t1Copy = new HashMap<String, Long>(t1);
    Map<String, Long> t2Copy = new HashMap<String, Long>(t2);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertUnchangedAll(".*", t1Copy, t2Copy);

    copyFile("helloworld/HelloworldAnonymous.adl",
        "helloworld/HelloworldAnonymous_modified.adl", new String[]{
            "helloworld\\.HelloworldAnonymous",
            "helloworld.HelloworldAnonymous_modified"}, new String[]{
            "attribute int a", "attribute uint8_t a"});

    pause();
    t2 = recompile(
        "GenericApplication<helloworld.HelloworldAnonymous_modified>",
        "HelloworldAnonymous_modified");
    t1Copy = new HashMap<String, Long>(t1);
    t2Copy = new HashMap<String, Long>(t2);

    assertChanged("helloworld/HelloworldAnonymous_modified.def", t1Copy, t2Copy);
    assertChangedAll("helloworld/HelloworldAnonymous_modified_anon_0_impl0.*",
        t1Copy, t2Copy);
    assertUnchanged(
        "helloworld/HelloworldAnonymous_modified_anon_0_ctrl_impl.c", t1Copy,
        t2Copy);
    assertChangedAll(
        "helloworld/HelloworldAnonymous_modified_anon_0_ctrl_impl.*", t1Copy,
        t2Copy);
    assertUnchanged("helloworld/HelloworldAnonymous_modified_anon_0.inc",
        t1Copy, t2Copy);
    assertUnchanged("helloworld/HelloworldAnonymous_modified_anon_0.macro",
        t1Copy, t2Copy);
    assertChangedAll("helloworld/HelloworldAnonymous_modified_anon_0.*",
        t1Copy, t2Copy);
    assertChangedAll("GenericApplication.*\\.def", t1Copy, t2Copy);
    assertChanged("GenericApplication.map", t1Copy, t2Copy);
    assertUnchangedAll(
        "GenericApplication.*HelloworldAnonymous_modified_anon_0_instances.c",
        t1Copy, t2Copy);
    assertChangedAll(
        "GenericApplication.*HelloworldAnonymous_modified_anon_0.*", t1Copy,
        t2Copy);
    assertChangedAll("HelloworldAnonymous_modified(\\.exe)?", t1Copy, t2Copy);
  }
}
