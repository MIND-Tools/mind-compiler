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

package org.ow2.mind.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.mind.plugin.util.Assert;

/**
 * This Handler parses the "src-path" option, creates a corresponding
 * classloader and register it in the context.
 */
public class SrcPathOptionHandler implements CommandOptionHandler {

  /** The ID of the "src-path" option. */
  public static final String  SRC_PATH_ID             = "org.ow2.mind.mindc.SrcPath";

  private static final String CLASSLOADER_CONTEXT_KEY = "classloader";

  /**
   * Returns the source class loader that has been registered in the given
   * context.
   * 
   * @param context the current context.
   * @return the source class loader
   */
  public static ClassLoader getSourceClassLoader(
      final Map<Object, Object> context) {
    return (ClassLoader) context.get(CLASSLOADER_CONTEXT_KEY);
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), SRC_PATH_ID);
    final CmdPathOption srcPathOpt = Assert.assertInstanceof(cmdOption,
        CmdPathOption.class);
    List<String> srcPath = srcPathOpt.getPathValue(cmdLine);
    if (srcPath == null) {
      srcPath = new ArrayList<String>(1);
      srcPath.add(".");
    }

    context.put(CLASSLOADER_CONTEXT_KEY, getSourceClassLoader(srcPath));
  }

  protected ClassLoader getSourceClassLoader(final List<String> srcPath) {
    final List<String> validatedPaths = new ArrayList<String>(srcPath.size());

    // check source paths
    for (final String path : srcPath) {
      final File f = new File(path);
      if (!f.exists()) {
        System.out.println("Warning '" + f.getAbsolutePath()
            + "' source path can't be found ");
      } else if (!f.isDirectory()) {
        System.out.println("Warning: \"" + path
            + "\" is not a directory, path ignored.");
      } else {
        validatedPaths.add(path);
      }
    }

    // build URL array of source path
    final URL[] urls = new URL[validatedPaths.size()];
    for (int i = 0; i < urls.length; i++) {
      final String path = validatedPaths.get(i);
      final File f = new File(path);
      try {
        urls[i] = f.toURI().toURL();
      } catch (final MalformedURLException e) {
        // never append
        throw new Error(e);
      }
    }

    return new URLClassLoader(urls, getClass().getClassLoader());
  }
}
