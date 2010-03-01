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

package org.ow2.mind.io;

import static org.ow2.mind.PathHelper.isRelative;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.ForceRegenContextHelper;

public class BasicOutputFileLocator implements OutputFileLocator {

  public static final String OUTPUT_DIR_CONTEXT_KEY           = "outputdir";
  public static final String DEFAULT_OUTPUT_DIR               = "build";
  public static final String TEMPORARY_OUTPUT_DIR_CONTEXT_KEY = "temporaryOutputDir";

  public File getCSourceOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);
    return mkdirs(new File(outDir, path));
  }

  public File getCSourceOutputDir(final Map<Object, Object> context)
      throws ADLException {
    return getOutputDir(context);
  }

  public File getCSourceTemporaryOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (ForceRegenContextHelper.getKeepTemp(context))
      return getCSourceOutputFile(path, context);
    return getTemporaryOutputFile(path, context);
  }

  public File getCSourceTemporaryOutputDir(final Map<Object, Object> context)
      throws ADLException {
    return getTemporaryOutputDir(context);
  }

  public File getCCompiledOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);
    return mkdirs(new File(outDir, path));
  }

  public File getCExecutableOutputFile(String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);

    // ensure that executable path on Windows ends with ".exe".
    if (System.getProperty("os.name").contains("Windows")
        && !path.endsWith(".exe")) {
      path = path + ".exe";
    }

    return mkdirs(new File(outDir, path));
  }

  public File getCCompiledOutputDir(final Map<Object, Object> context)
      throws ADLException {
    return getOutputDir(context);
  }

  public File getCCompiledTemporaryOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (ForceRegenContextHelper.getKeepTemp(context))
      return getCCompiledOutputFile(path, context);
    return getTemporaryOutputFile(path, context);
  }

  public File getCCompiledTemporaryOutputDir(final Map<Object, Object> context)
      throws ADLException {
    return getTemporaryOutputDir(context);
  }

  public File getMetadataOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);
    return mkdirs(new File(outDir, path));
  }

  protected File getTemporaryOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getTemporaryOutputDir(context);
    return mkdirs(new File(outDir, path));
  }

  protected File mkdirs(final File outputFile) throws ADLException {
    final File parent = outputFile.getParentFile();
    if (parent.exists()) {
      if (!parent.isDirectory()) {
        throw new ADLException(GenericErrors.GENERIC_ERROR,
            "IO Error: cannot create directory \"" + parent.getPath()
                + "\". File exists but is not a directory.");
      }
    } else {
      parent.mkdirs();
    }
    return outputFile;
  }

  protected File getOutputDir(final Map<Object, Object> context)
      throws ADLException {
    File outDir = (File) context.get(OUTPUT_DIR_CONTEXT_KEY);
    if (outDir == null) outDir = new File(DEFAULT_OUTPUT_DIR);
    if (outDir.exists()) {
      if (!outDir.isDirectory()) {
        throw new ADLException(IOErrors.INVALID_OUTPUT_DIR, outDir);
      }
    } else {
      outDir.mkdirs();
    }
    return outDir;
  }

  protected File getTemporaryOutputDir(final Map<Object, Object> context) {
    File tempOutDir = (File) context.get(TEMPORARY_OUTPUT_DIR_CONTEXT_KEY);
    if (tempOutDir == null) {
      for (int i = 0; i < 10; i++) {
        File tempFile;
        try {
          tempFile = File.createTempFile("mindc", null);
        } catch (final IOException e) {
          // fail to create temp file, retry.
          continue;
        }
        if (!tempFile.delete()) {
          // fail to delete temp file, retry
          continue;
        }
        if (!tempFile.mkdir()) {
          // fail to create directory, retry
          continue;
        }

        // succesfully create temp directory.
        tempOutDir = tempFile;
        break;
      }

      if (tempOutDir == null) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "IO Error: fail to create temporary directory.");
      }
      context.put(TEMPORARY_OUTPUT_DIR_CONTEXT_KEY, tempOutDir);

      // Add a shutdown hook to delete temporary directory.
      final File temporaryOutputDir = tempOutDir;
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          delete(temporaryOutputDir);
        }

        public void delete(final File f) {
          if (f.isDirectory()) {
            for (final File subFile : f.listFiles())
              delete(subFile);
          }
          f.delete();
        }
      });

    }
    return tempOutDir;
  }
}
