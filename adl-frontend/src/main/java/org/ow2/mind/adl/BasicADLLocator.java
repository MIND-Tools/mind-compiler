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

package org.ow2.mind.adl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.InputResource;

/**
 * Basic implementation of the {@link ADLLocator} interface for STCF ADL.
 */
public class BasicADLLocator implements ADLLocator {

  static final String SOURCE_EXTENSION = ".adl";
  static final String BINARY_EXTENSION = ".def";

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLocator interface
  // ---------------------------------------------------------------------------

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    final ClassLoader cl = ClassLoaderHelper.getClassLoader(this, context);
    if (cl instanceof URLClassLoader) {
      return ((URLClassLoader) cl).getURLs();
    }
    return null;
  }

  public URL findBinaryADL(final String name, final Map<Object, Object> context) {
    return ClassLoaderHelper.getClassLoader(this, context).getResource(
        (name.replace('.', '/') + BINARY_EXTENSION));
  }

  public URL findSourceADL(final String name, final Map<Object, Object> context) {
    return ClassLoaderHelper.getClassLoader(this, context).getResource(
        (name.replace('.', '/') + SOURCE_EXTENSION));
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return findSourceADL(name, context);
  }

  public InputResource toInputResource(final String name) {
    return new InputResource(ADL_RESOURCE_KIND, name);
  }
}
