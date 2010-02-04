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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.InputResource;

public class BasicImplementationLocator implements ImplementationLocator {

  // ---------------------------------------------------------------------------
  // Implementation of the ImplementationLocator interface
  // ---------------------------------------------------------------------------

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    final ClassLoader cl = ClassLoaderHelper.getClassLoader(this, context);
    if (cl instanceof URLClassLoader) {
      return ((URLClassLoader) cl).getURLs();
    }
    return null;
  }

  public URL findSource(final String path, final Map<Object, Object> context) {
    if (!isValid(path))
      throw new IllegalArgumentException("\"" + path + "\" is not a valid path");
    if (isRelative(path))
      throw new IllegalArgumentException("\"" + path
          + "\" is not an absolute path");

    return getClassLoader(this, context).getResource(path.substring(1));
  }

  public InputResource toInputResource(final String path) {
    return new InputResource(
        ImplementationLocator.IMPLEMENTATION_RESOURCE_KIND, path);
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return findSource(name, context);
  }

}
