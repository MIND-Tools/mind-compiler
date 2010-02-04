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
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.GenericErrors;

public class BasicOutputFileLocator implements OutputFileLocator {

  public static final String OUTPUT_DIR_CONTEXT_KEY = "outputdir";
  public static final String DEFAULT_OUTPUT_DIR     = "build";

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

  public File getCCompiledOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);
    return mkdirs(new File(outDir, path));
  }

  public File getCCompiledOutputDir(final Map<Object, Object> context)
      throws ADLException {
    return getOutputDir(context);
  }

  public File getMetadataOutputFile(final String path,
      final Map<Object, Object> context) throws ADLException {
    if (isRelative(path))
      throw new IllegalArgumentException("path must be absolute");
    final File outDir = getOutputDir(context);
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

}
