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

package org.ow2.mind.adl.implementation;

import static org.objectweb.fractal.adl.util.ClassLoaderHelper.getClassLoader;
import static org.ow2.mind.PathHelper.isRelative;
import static org.ow2.mind.PathHelper.isValid;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import org.ow2.mind.InputResource;

import com.google.common.collect.Lists;

public class BasicImplementationLocator implements ImplementationLocator {

  // ---------------------------------------------------------------------------
  // Implementation of the ImplementationLocator interface
  // ---------------------------------------------------------------------------

  public Iterable<String> getResourceKind() {
    return Lists
        .newArrayList(ImplementationLocator.IMPLEMENTATION_RESOURCE_KIND);
  }

  public URL findSource(final String path, final Map<Object, Object> context) {
    if (!isValid(path))
      throw new IllegalArgumentException("\"" + path + "\" is not a valid path");
    if (isRelative(path))
      throw new IllegalArgumentException("\"" + path
          + "\" is not an absolute path");
    try {
      final Enumeration<URL> urls = getClassLoader(this, context).getResources(
          path.substring(1));
      while (urls.hasMoreElements()) {
        final URL url = urls.nextElement();
        if (url.getProtocol().equals("file")) return url;
      }
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;

  }

  public InputResource toInputResource(final String path) {
    return new InputResource(
        ImplementationLocator.IMPLEMENTATION_RESOURCE_KIND, path);
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return findSource(name, context);
  }

}
