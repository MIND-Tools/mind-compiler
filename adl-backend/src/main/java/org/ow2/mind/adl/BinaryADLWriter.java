
package org.ow2.mind.adl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.NodeOutputStream;
import org.ow2.mind.io.IOErrors;

public class BinaryADLWriter extends AbstractSourceGenerator
    implements
      DefinitionSourceGenerator {

  protected static Logger logger = FractalADLLogManager.getLogger("io");

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  /**
   * Public constructor.
   */
  public BinaryADLWriter() {
    super(null);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionSourceGenerator interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final File outputFile = outputFileLocatorItf.getMetadataOutputFile(
        BasicADLLocator.getADLBinaryName(definition), context);

    if (regenerate(outputFile, definition, context)) {

      NodeOutputStream nos = null;
      try {
        if (logger.isLoggable(Level.FINE))
          logger.log(Level.FINE, "Write binary ADL to " + outputFile);
        nos = new NodeOutputStream(new FileOutputStream(outputFile));
        nos.writeNode(definition);
      } catch (final IOException e) {
        throw new CompilerError(IOErrors.WRITE_ERROR, e,
            "Can't write binary ADL to file " + outputFile);
      } finally {
        if (nos != null)
          try {
            nos.close();
          } catch (final IOException e) {
            if (logger.isLoggable(Level.WARNING))
              logger
                  .warning("Unable to close stream use to write binary ADL \""
                      + outputFile + "\" : " + e.getMessage());
          }
      }
    }
  }

}
