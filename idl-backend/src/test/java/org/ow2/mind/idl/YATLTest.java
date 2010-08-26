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

package org.ow2.mind.idl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.PathHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.io.BasicOutputFileLocator;

public class YATLTest extends TestCase {

  IDLLoader           loader;
  IDL2C               idl2c;
  Map<Object, Object> context;

  @Override
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();
    loader = IDLLoaderChainFactory.newLoader(errorManager).loader;
    idl2c = new IDL2C();

    context = new HashMap<Object, Object>();
    context.put(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY, new File(
        "target/build"));
  }

  public void test1() throws Exception {
    processAndCompare("test1");
  }

  public void test2() throws Exception {
    processAndCompare("test2");
  }

  public void testFoo2() throws Exception {
    processAndCompare("/foo/test2.idt");
  }

  private void processAndCompare(final String testName) throws Exception {
    final File actualFile = process(testName);
    String expectedFileName;
    if (testName.startsWith("/")) {
      expectedFileName = PathHelper.replaceExtension(testName,
          ".expected.idt.h");
    } else {
      expectedFileName = PathHelper.fullyQualifiedNameToPath(testName,
          ".expected.itf.h");
    }
    final URL expected = getClass().getClassLoader().getResource(
        expectedFileName.substring(1, expectedFileName.length()));
    assertNotNull("Unable to find expected result " + expectedFileName,
        expected);
    final File expectedFile = new File(expected.toURI());
    compare(actualFile, expectedFile);
  }

  private File process(final String testName) throws ADLException {
    final IDL idl = loader.load(testName, context);
    idl2c.idlFile(idl);
    if (testName.startsWith("/")) {
      return new File("target/build" + testName + ".h");
    } else {
      return new File("target/build"
          + PathHelper.fullyQualifiedNameToPath(idl.getName(), "itf.h"));
    }
  }

  private void compare(final File actualFile, final File expectedFile)
      throws IOException {
    LineNumberReader actualReader = null;
    LineNumberReader expectedReader = null;
    try {
      actualReader = new LineNumberReader(new FileReader(actualFile));
      expectedReader = new LineNumberReader(new FileReader(expectedFile));

      String actualLine = actualReader.readLine();
      String expectedLine = expectedReader.readLine();
      while (actualLine != null && expectedLine != null) {
        assertEquals(actualFile.getPath() + ":" + actualReader.getLineNumber()
            + " unexpected line", expectedLine, actualLine);
        actualLine = actualReader.readLine();
        expectedLine = expectedReader.readLine();
      }
      if (expectedLine != null) {
        fail(actualFile.getPath() + ":" + actualReader.getLineNumber()
            + " Too many line in file");
      }
      if (actualLine != null) {
        fail(actualFile.getPath() + ":" + actualReader.getLineNumber()
            + " not enough line in file");
      }

    } finally {
      if (actualReader != null) actualReader.close();
      if (expectedReader != null) expectedReader.close();
    }

  }

}
