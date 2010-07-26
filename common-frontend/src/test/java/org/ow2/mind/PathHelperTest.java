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

import static org.ow2.mind.PathHelper.fullyQualifiedNameToAbsolute;
import static org.ow2.mind.PathHelper.fullyQualifiedNameToDirName;
import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;
import static org.ow2.mind.PathHelper.getExtension;
import static org.ow2.mind.PathHelper.getParent;
import static org.ow2.mind.PathHelper.isRelative;
import static org.ow2.mind.PathHelper.isValid;
import static org.ow2.mind.PathHelper.packageNameToAbsolute;
import static org.ow2.mind.PathHelper.packageNameToDirName;
import static org.ow2.mind.PathHelper.removeExtension;
import static org.ow2.mind.PathHelper.replaceExtension;
import static org.ow2.mind.PathHelper.toAbsolute;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.ow2.mind.PathHelper.InvalidRelativPathException;
import org.testng.annotations.Test;

public class PathHelperTest {

  /**
   * Test method for {@link PathHelper#isValid(String)}.
   */
  @Test(groups = {"functional"})
  public void testIsValid() {
    assertTrue(isValid("bar.txt"));
    assertTrue(isValid("foo/bar.txt"));
    assertTrue(isValid("./foo/bar.txt"));
    assertTrue(isValid("./../foo/bar.txt"));
    assertTrue(isValid("/foo/bar.txt"));

    assertFalse(isValid("foo//bar.txt"));
    assertFalse(isValid("/../foo/bar.txt"));
  }

  /**
   * Test method for {@link PathHelper#isRelative(String)}.
   */
  @Test(groups = {"functional"})
  public void testIsRelative() {
    assertTrue(isRelative("bar.txt"));
    assertTrue(isRelative("./bar.txt"));
    assertTrue(isRelative("foo/bar.txt"));
    assertTrue(isRelative("./foo/bar.txt"));
    assertTrue(isRelative("./../foo/bar.txt"));
    assertFalse(isRelative("/foo/bar.txt"));
  }

  /**
   * Test method for {@link PathHelper#getParent(String)}.
   */
  @Test(groups = {"functional"})
  public void testGetParent() throws Exception {
    assertEquals("foo", getParent("foo/bar.txt"));
    assertEquals("foo/toto", getParent("foo/toto/bar.txt"));
    assertEquals("", getParent("bar.txt"));
  }

  /**
   * Test method for {@link PathHelper#getExtension(String)}.
   */
  @Test(groups = {"functional"})
  public void testGetExtension() {
    assertEquals("txt", getExtension("bar.txt"));
    assertEquals("txt", getExtension("./bar.txt"));
    assertEquals("txt", getExtension("foo/bar.txt"));
    assertEquals("txt", getExtension("./foo/bar.txt"));
    assertEquals("txt", getExtension("./../foo/bar.txt"));

    assertNull(getExtension("bar"));
    assertNull(getExtension("foo.1/bar"));
  }

  /**
   * Test method for {@link PathHelper#removeExtension(String)}.
   */
  @Test(groups = {"functional"})
  public void testRemoveExtension() {
    assertEquals("bar", removeExtension("bar.txt"));
    assertEquals("./bar", removeExtension("./bar.txt"));
    assertEquals("foo/bar", removeExtension("foo/bar.txt"));
    assertEquals("./foo/bar", removeExtension("./foo/bar.txt"));
    assertEquals("./../foo/bar", removeExtension("./../foo/bar.txt"));

    assertEquals("bar", removeExtension("bar"));
    assertEquals("./bar", removeExtension("./bar"));
    assertEquals("foo.1/bar", removeExtension("foo.1/bar"));
  }

  /**
   * Test method for {@link PathHelper#replaceExtension(String, String)} .
   */
  @Test(groups = {"functional"})
  public void testReplaceExtension() {
    assertEquals("bar.c", replaceExtension("bar.txt", "c"));
    assertEquals("./bar.c", replaceExtension("./bar.txt", "c"));
    assertEquals("foo/bar.c", replaceExtension("foo/bar.txt", "c"));
    assertEquals("./foo/bar.c", replaceExtension("./foo/bar.txt", "c"));
    assertEquals("./../foo/bar.c", replaceExtension("./../foo/bar.txt", "c"));

    assertEquals("bar.c", replaceExtension("bar", "c"));
    assertEquals("./bar.c", replaceExtension("./bar", "c"));
    assertEquals("foo.1/bar.c", replaceExtension("foo.1/bar", "c"));

    assertEquals("bar.c", replaceExtension("bar.txt", ".c"));
  }

  /**
   * Test method for {@link PathHelper#toAbsolute(String, String)}.
   * 
   * @throws Exception
   */
  @Test(groups = {"functional"})
  public void testToAbsolute() throws Exception {
    assertEquals("foo/bar.txt", toAbsolute("foo", "./bar.txt"));
    assertEquals("foo/bar.txt", toAbsolute("foo", "bar.txt"));
    assertEquals("foo/bar.txt", toAbsolute("foo/", "./bar.txt"));
    assertEquals("foo/toto/bar.txt", toAbsolute("foo", "toto/bar.txt"));
    assertEquals("foo/toto/bar.txt", toAbsolute("foo", "./toto/bar.txt"));
    assertEquals("foo/toto/bar.txt", toAbsolute("foo/toto", "bar.txt"));
    assertEquals("foo/toto/bar.txt", toAbsolute("foo/toto", "./bar.txt"));
    assertEquals("foo/titi/bar.txt", toAbsolute("foo/toto", "../titi/bar.txt"));

    assertEquals("bar.txt", toAbsolute("foo/", "../bar.txt"));
    assertEquals("bar.txt", toAbsolute("foo/", "./../bar.txt"));

    assertEquals("toto/bar.txt", toAbsolute("foo/", "../toto/bar.txt"));
    assertEquals("toto/bar.txt", toAbsolute("foo/", "./../toto/bar.txt"));

    assertEquals("toto/bar.txt", toAbsolute("foo/titi", "../../toto/bar.txt"));

    testFaultyToAbsolute("foo", "../../bar.txt");
    testFaultyToAbsolute("foo", "./../../bar.txt");
    testFaultyToAbsolute("/", "../bar.txt");
    testFaultyToAbsolute("", "../bar.txt");

  }

  private void testFaultyToAbsolute(final String dirName, final String path) {
    try {
      toAbsolute(dirName, path);
      fail();
    } catch (final InvalidRelativPathException e) {
      // OK
    }
  }

  /**
   * Test method for {@link PathHelper#fullyQualifiedNameToDirName(String)} .
   */
  @Test(groups = {"functional"})
  public void testFullyQualifiedNameToDirName() {
    assertEquals("/foo", fullyQualifiedNameToDirName("foo.bar"));
    assertEquals("/foo/toto", fullyQualifiedNameToDirName("foo.toto.bar"));
    assertEquals("/", fullyQualifiedNameToDirName("bar"));
  }

  /**
   * Test method for {@link PathHelper#packageNameToDirName(String)} .
   */
  @Test(groups = {"functional"})
  public void testPackageNameToDirName() {
    assertEquals("/foo/bar", packageNameToDirName("foo.bar"));
    assertEquals("/foo/toto/bar", packageNameToDirName("foo.toto.bar"));
    assertEquals("/bar", packageNameToDirName("bar"));
  }

  /**
   * Test method for
   * {@link PathHelper#fullyQualifiedNameToAbsolute(String, String)} .
   * 
   * @throws Exception
   */
  @Test(groups = {"functional"})
  public void testFullyQualifiedNameToAbsolute() throws Exception {
    assertEquals("/foo/bar.txt",
        fullyQualifiedNameToAbsolute("foo.toto", "./bar.txt"));
    assertEquals("/foo/titi/bar.txt",
        fullyQualifiedNameToAbsolute("foo.toto", "./titi/bar.txt"));
    assertEquals("/bar.txt",
        fullyQualifiedNameToAbsolute("foo.toto", "../bar.txt"));
    assertEquals(
        "/hello/cli_src/client.c",
        fullyQualifiedNameToAbsolute("hello.client.Client",
            "../cli_src/client.c"));
  }

  /**
   * Test method for {@link PathHelper#packageNameToAbsolute(String, String)} .
   * 
   * @throws Exception
   */
  @Test(groups = {"functional"})
  public void testPackageNameToAbsolute() throws Exception {
    assertEquals("/foo/toto/bar.txt",
        packageNameToAbsolute("foo.toto", "./bar.txt"));
    assertEquals("/foo/toto/titi/bar.txt",
        packageNameToAbsolute("foo.toto", "./titi/bar.txt"));
    assertEquals("/bar.txt", packageNameToAbsolute("foo", "../bar.txt"));
  }

  /**
   * Test method for {@link PathHelper#fullyQualifiedNameToPath(String, String)}
   * .
   */
  @Test(groups = {"functional"})
  public void testFullyQualifiedNameToPath() {
    assertEquals("/foo/bar.adl", fullyQualifiedNameToPath("foo.bar", "adl"));
    assertEquals("/foo/bar.adl", fullyQualifiedNameToPath("foo.bar", ".adl"));
    assertEquals("/foo/bar", fullyQualifiedNameToPath("foo.bar", null));
  }

}
