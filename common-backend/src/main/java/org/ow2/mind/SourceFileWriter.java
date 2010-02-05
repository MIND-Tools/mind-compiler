/***
 * Cecilia ADL Compiler
 * Copyright (C) 2008 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: fractal@objectweb.org
 *
 * Author:Matthieu Leclercq
 */

package org.ow2.mind;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.util.FractalADLLogManager;

/**
 * Helper class that provides methods to write a string into a generated source
 * file. If the output file already exist, its content is compared with the
 * string to be written to avoid useless timestamp modification.
 */
public final class SourceFileWriter {
  private SourceFileWriter() {
  }

  // The dep logger
  private static Logger    depLogger = FractalADLLogManager.getLogger("dep");

  // The io logger
  private static Logger    ioLogger  = FractalADLLogManager.getLogger("io");

  private static final int    READ_SIZE = 512;

  private static final Object LOCK      = new Object();

  /**
   * Creates the given output directory. The creation is protected by a global
   * lock to avoid concurrency issues.
   * 
   * @param outputDir the directory to create.
   * @throws IOException the the given file exists and is not a directory.
   */
  public static void createOutputDir(final File outputDir) throws IOException {
    synchronized (LOCK) {
      if (outputDir.exists()) {
        if (!outputDir.isDirectory()) {
          throw new IOException("Invalid file '" + outputDir
              + "': not a directory");
        }
      } else {
        outputDir.mkdirs();
      }
    }
  }


  /**
   * Write the given content in the given output file. If the output file
   * already exist, its content is compared with the string to be written to
   * avoid useless timestamp modification.
   * 
   * @param outputFile the output file into which the content will be written.
   * @param content the content to write into the file
   * @throws IOException if an error occurs.
   */
  public static void writeToFile(final File outputFile, final String content)
      throws IOException {
    if (!outputFile.exists()) doWrite(outputFile, content);

    boolean rewrite = false;
    try {
      final FileReader fr = new FileReader(outputFile);
      final char[] cs = content.toCharArray();
      int inPos = 0;

      final char[] fc = new char[READ_SIZE];

      int r;
      do {
        r = fr.read(fc);
        for (int i = 0; i < r; i++, inPos++) {
          if (cs[inPos] != fc[i]) {
            break;
          }
        }
      } while (r != -1);
      if (inPos != cs.length) {
        rewrite = true;
      }

      fr.close();
    } catch (final IOException e) {
      // if an exception happen while comparing file content, ignore it an
      // overwrite the file.
      rewrite = true;
    }

    if (rewrite) {
      if (ioLogger.isLoggable(Level.FINE))
        ioLogger.fine("Write generated source file '" + outputFile + "'.");
      doWrite(outputFile, content);
    } else {
      if (depLogger.isLoggable(Level.FINE))
        depLogger.fine("Generated source file '" + outputFile
            + "' is unchanged.");
    }
  }

  private static void doWrite(final File outputFile, final String content)
      throws IOException {
    final FileWriter fw = new FileWriter(outputFile);
    fw.write(content);
    fw.close();
  }
}
