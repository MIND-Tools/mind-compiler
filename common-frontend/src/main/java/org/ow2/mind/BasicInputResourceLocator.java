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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

public class BasicInputResourceLocator
    implements
      InputResourceLocator,
      BindingController {

  protected static Logger                          logger                                   = FractalADLLogManager
                                                                                                .getLogger("loader.InputResourceLocator");

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String                       GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME = "resource-locator";

  public final Map<String, GenericResourceLocator> genericResourceLocators                  = new HashMap<String, GenericResourceLocator>();

  // ---------------------------------------------------------------------------
  // Implementation of the InputResourceLocator interface
  // ---------------------------------------------------------------------------

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    final ClassLoader cl = ClassLoaderHelper.getClassLoader(this, context);
    if (cl instanceof URLClassLoader) {
      return ((URLClassLoader) cl).getURLs();
    }
    return null;
  }

  public URL findResource(final InputResource resource,
      final Map<Object, Object> context) {
    if (resource.getLocation() != null) return resource.getLocation();

    final GenericResourceLocator genericResourceLocator = genericResourceLocators
        .get(resource.getKind());
    if (genericResourceLocator != null) {
      final URL location = genericResourceLocator.findResource(resource
          .getName(), context);
      if (location != null) resource.setLocation(location);
      return location;
    }
    return null;
  }

  public boolean isUpToDate(final long timestamp,
      final Collection<InputResource> inputs, final Map<Object, Object> context) {
    if (ForceRegenContextHelper.getForceRegen(context)) return false;

    for (final InputResource dependency : inputs) {
      try {
        if (getTimestamp(dependency, context) >= timestamp) {
          // a dependency is newer than given timestamp.
          return false;
        }
      } catch (final MalformedURLException e) {
        if (logger.isLoggable(Level.FINE))
          logger.log(Level.FINE, "Can't determine file timestamps of "
              + dependency);
        // if one dependency is missing consider the file as out of date.
        return false;
      }
    }
    return true;
  }

  public boolean isUpToDate(final File file,
      final Collection<InputResource> inputs, final Map<Object, Object> context) {
    return isUpToDate(file.lastModified(), inputs, context);
  }

  public boolean isUpToDate(final URL url,
      final Collection<InputResource> inputs, final Map<Object, Object> context)
      throws MalformedURLException {
    return isUpToDate(InputResourcesHelper.getTimestamp(url), inputs, context);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected long getTimestamp(final InputResource resource,
      final Map<Object, Object> context) throws MalformedURLException {
    long timestamp = resource.getTimestamp();
    if (timestamp == -1) {
      final URL url = findResource(resource, context);
      if (url != null)
        timestamp = InputResourcesHelper.getTimestamp(url);
      else
        timestamp = Long.MAX_VALUE;
      resource.setTimestamp(timestamp);
    }
    return timestamp;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String clientItfName, final Object serverItf)
      throws NoSuchInterfaceException, IllegalBindingException,
      IllegalLifeCycleException {
    checkItfName(clientItfName);

    if (clientItfName.startsWith(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME)) {
      final String resourceKind = clientItfName
          .substring(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME.length());
      genericResourceLocators.put(resourceKind,
          (GenericResourceLocator) serverItf);
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + clientItfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    final Set<String> kinds = genericResourceLocators.keySet();
    final String[] itfNames = new String[kinds.size()];
    int i = 0;
    for (final String kind : kinds) {
      itfNames[i] = GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME + kind;
      i++;
    }
    return itfNames;
  }

  public Object lookupFc(final String clientItfName)
      throws NoSuchInterfaceException {
    checkItfName(clientItfName);

    if (clientItfName.startsWith(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME)) {
      final String resourceKind = clientItfName
          .substring(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME.length());
      final GenericResourceLocator itf = genericResourceLocators
          .get(resourceKind);
      if (itf != null) return itf;
    }
    throw new NoSuchInterfaceException("No client interface named '"
        + clientItfName + "' for binding the interface");
  }

  public void unbindFc(final String clientItfName)
      throws NoSuchInterfaceException, IllegalBindingException,
      IllegalLifeCycleException {
    if (clientItfName.startsWith(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME)) {
      final String resourceKind = clientItfName
          .substring(GENERIC_RESOURCE_LOCATOR_PREFIX_ITF_NAME.length());
      final GenericResourceLocator itf = genericResourceLocators
          .remove(resourceKind);
      if (itf != null) return;
    }
    throw new NoSuchInterfaceException("No client interface named '"
        + clientItfName + "' for binding the interface");
  }
}
