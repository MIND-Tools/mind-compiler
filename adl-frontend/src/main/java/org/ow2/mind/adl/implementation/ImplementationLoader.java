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

import static org.ow2.mind.PathHelper.fullyQualifiedNameToAbsolute;
import static org.ow2.mind.PathHelper.isRelative;
import static org.ow2.mind.PathHelper.isValid;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.PathHelper.InvalidRelativPathException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

public class ImplementationLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager          errorManagerItf;

  @Inject
  protected ImplementationLocator implementationLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition def = clientLoader.load(name, context);
    if (def instanceof ImplementationContainer) {
      processImplementation(def, (ImplementationContainer) def, context);
    }
    return def;
  }

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected void processImplementation(final Definition def,
      final ImplementationContainer container, final Map<Object, Object> context)
      throws ADLException {
    final Data data = container.getData();
    if (data != null) {
      processData(def, data, context);
    }

    for (final Source src : container.getSources()) {
      processSrc(def, src, context);
    }
  }

  protected void processData(final Definition def, final Data data,
      final Map<Object, Object> context) throws ADLException {
    String path = data.getPath();
    if (path != null) {
      if (!isValid(path)) {
        errorManagerItf.logError(ADLErrors.INVALID_PATH, data, path);
        return;
      }

      if (isRelative(path)) {
        try {
          path = fullyQualifiedNameToAbsolute(def.getName(), path);
        } catch (final InvalidRelativPathException e) {
          errorManagerItf.logError(ADLErrors.INVALID_PATH, data, path);
        }
        data.setPath(path);
      }

      if (implementationLocatorItf.findSource(path, context) == null) {
        errorManagerItf.logError(ADLErrors.SOURCE_NOT_FOUND, data, path);
      }
    }
  }

  protected void processSrc(final Definition def, final Source src,
      final Map<Object, Object> context) throws ADLException {
    String path = src.getPath();
    if (path != null) {
      if (!isValid(path)) {
        errorManagerItf.logError(ADLErrors.INVALID_PATH, src, path);
        return;
      }

      if (isRelative(path)) {
        try {
          path = fullyQualifiedNameToAbsolute(def.getName(), path);
        } catch (final InvalidRelativPathException e) {
          errorManagerItf.logError(ADLErrors.INVALID_PATH, src, path);
        }
        src.setPath(path);
      }

      if (implementationLocatorItf.findSource(path, context) == null) {
        errorManagerItf.logError(ADLErrors.SOURCE_NOT_FOUND, src, path);
      }
    }
  }
}
