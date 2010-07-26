
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

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

  IDLLoader           loader;
  IDLVisitor          idlVisitor;
  Map<Object, Object> context;

  @Override
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();
    loader = IDLLoaderChainFactory.newLoader(errorManager).loader;
    idlVisitor = IDLBackendFactory.newIDLCompiler(loader);

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
    idlVisitor.visit(idl, context);
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
