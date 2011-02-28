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

import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;

import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.InputResource;

import com.google.common.collect.Lists;

/**
 * Basic implementation of the {@link ADLLocator} interface for STCF ADL.
 */
public class BasicADLLocator implements ADLLocator {

  public static final String SOURCE_EXTENSION = ".adl";
  public static final String BINARY_EXTENSION = ".def";

  // ---------------------------------------------------------------------------
  // public static methods
  // ---------------------------------------------------------------------------

  public Iterable<String> getResourceKind() {
    return Lists.newArrayList(ADLLocator.ADL_RESOURCE_KIND);
  };

  public static String getADLSourceName(final Definition definition) {
    return getADLSourceName(definition.getName());
  }

  public static String getADLSourceName(String name) {
    int i = name.indexOf('$');
    if (i > 0) {
      name = name.substring(0, i);
    }
    i = name.indexOf('<');
    if (i > 0) {
      name = name.substring(0, i);
    }
    return fullyQualifiedNameToPath(name, SOURCE_EXTENSION);
  }

  public static String getADLBinaryName(final Definition definition) {
    final String name = definition.getName();
    return getADLBinaryName(name);
  }

  public static String getADLBinaryName(final String name) {
    return fullyQualifiedNameToPath(name, BINARY_EXTENSION);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLocator interface
  // ---------------------------------------------------------------------------

  public URL findBinaryADL(final String name, final Map<Object, Object> context) {
    return ClassLoaderHelper.getClassLoader(this, context).getResource(
        getADLBinaryName(name).substring(1));
  }

  public URL findSourceADL(final String name, final Map<Object, Object> context) {
    return ClassLoaderHelper.getClassLoader(this, context).getResource(
        getADLSourceName(name).substring(1));
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return findSourceADL(name, context);
  }

  public InputResource toInputResource(final String name) {
    return new InputResource(ADL_RESOURCE_KIND, name);
  }
}
