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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;

public final class DepsHelper {

  private static final String    TEST_DEPS_DIR = "target/test-deps";

  private static final String    JAR_PROTOCOL  = "jar";
  private static final String    FILE_PROTOCOL = "file";

  private static Map<File, File> unpackedJars  = new HashMap<File, File>();

  private DepsHelper() {
  }

  public static File unpackDeps(final String resource, final ClassLoader cl)
      throws ZipException, IOException, URISyntaxException {
    final URL url = cl.getResource(resource);
    if (url == null)
      throw new IllegalArgumentException("Can't find " + resource
          + " in classpath.");
    final String protocol = url.getProtocol();
    if (protocol.equals(JAR_PROTOCOL)) {
      String path = url.getPath();
      final int i = path.indexOf('!');
      if (i <= 0) {
        throw new IllegalArgumentException("Illegal URL " + url
            + " Can't find jar file URL section.");
      }
      path = path.substring(0, i);
      final URL jarURL = new URL(path);
      if (!jarURL.getProtocol().equals(FILE_PROTOCOL)) {
        throw new IllegalArgumentException("Illegal URL " + url
            + " Invalid URL protocol for jar file");
      }
      final File jarFile = new File(jarURL.toURI().getPath());
      if (!jarFile.exists())
        throw new IllegalArgumentException("Illegal URL path " + jarFile);
      File unpackedJar = unpackedJars.get(jarFile);
      if (unpackedJar == null) {
        unpackedJar = unpackJar(jarFile);
        unpackedJars.put(jarFile, unpackedJar);
      }
      return unpackedJar;
    } else if (protocol.equals(FILE_PROTOCOL)) {
      File f = new File(url.toURI());
      String s = resource;
      int i = s.lastIndexOf('/');
      while (i > 0) {
        f = f.getParentFile();
        s = s.substring(0, i);
        i = s.lastIndexOf('/');
      }
      if (s.length() > 0) {
        f = f.getParentFile();
      }
      return f;
    } else {
      throw new IllegalArgumentException("Invalid URL protocol " + url);
    }
  }

  private static File unpackJar(final File jarFile) throws ZipException,
      IOException {
    String jarName = jarFile.getName();
    final int i = jarName.lastIndexOf('.');
    if (i > 0) jarName = jarName.substring(0, i);
    final File toDir = new File(TEST_DEPS_DIR, jarName);
    if (toDir.lastModified() > jarFile.lastModified()) {
      System.out.println("Package " + jarFile + " is uptodate in " + toDir);
      return toDir;
    }

    if (toDir.exists()) {
      delete(toDir);
    }
    System.out.println("Unpacking " + jarFile + " to " + toDir);
    final ZipFile f = new ZipFile(jarFile);
    final Enumeration<? extends ZipEntry> entries = f.entries();
    final byte[] b = new byte[1024];
    while (entries.hasMoreElements()) {
      final ZipEntry entry = entries.nextElement();
      final File of = new File(toDir, entry.getName());
      if (entry.isDirectory()) {
        of.mkdirs();
      } else {
        final InputStream is = new BufferedInputStream(f.getInputStream(entry));
        of.getParentFile().mkdirs();
        final OutputStream os = new BufferedOutputStream(new FileOutputStream(
            of));
        while (true) {
          final int nbBytes = is.read(b);
          if (nbBytes == -1) break;
          os.write(b, 0, nbBytes);
        }
        os.flush();
        os.close();
        is.close();
      }
    }
    return toDir;
  }

  private static File getTempDir(final String name) {
    File tempOutDir = null;
    for (int i = 0; i < 10; i++) {
      File tempFile;
      try {
        tempFile = File.createTempFile(name, null);
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

    // Add a shutdown hook to delete temporary directory.
    final File temporaryOutputDir = tempOutDir;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        delete(temporaryOutputDir);
      }

    });

    return temporaryOutputDir;
  }

  private static void delete(final File f) {
    if (f.isDirectory()) {
      for (final File subFile : f.listFiles())
        delete(subFile);
    }
    f.delete();
  }
}
