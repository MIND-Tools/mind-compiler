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

  // The io logger
  private static Logger       ioLogger  = FractalADLLogManager.getLogger("io");

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
   * @return <code>true</code> if the file has been actually written.
   * @throws IOException if an error occurs.
   */
  public static boolean writeToFile(final File outputFile, final String content)
      throws IOException {
    if (!outputFile.exists()) {
      doWrite(outputFile, content);
      return true;
    }

    boolean rewrite = false;
    FileReader fr = null;
    try {
      fr = new FileReader(outputFile);
      final char[] cs = content.toCharArray();
      int inPos = 0;

      final char[] fc = new char[READ_SIZE];

      int r;
      do {
        r = fr.read(fc);
        for (int i = 0; i < r && inPos < cs.length; i++, inPos++) {
          if (cs[inPos] != fc[i]) {
            break;
          }
        }
      } while (r != -1);
      if (inPos != cs.length) {
        rewrite = true;
      }

    } catch (final IOException e) {
      // if an exception happen while comparing file content, ignore it an
      // overwrite the file.
      rewrite = true;
    } finally {
      if (fr != null) fr.close();
    }

    if (rewrite) {
      doWrite(outputFile, content);
      return true;
    } else {
      if (ioLogger.isLoggable(Level.FINE))
        ioLogger.fine("Generated source file '" + outputFile
            + "' is unchanged.");
      return false;
    }
  }

  private static void doWrite(final File outputFile, final String content)
      throws IOException {
    if (ioLogger.isLoggable(Level.FINE))
      ioLogger.fine("Write generated source file '" + outputFile + "'.");
    final FileWriter fw = new FileWriter(outputFile);
    try {
      fw.write(content);
    } finally {
      fw.close();
    }
  }
}
