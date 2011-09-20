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

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.objectweb.fractal.adl.util.FractalADLLogManager;

import com.google.inject.Inject;

public class BasicInputResourceLocator implements InputResourceLocator {

  protected static Logger                       logger = FractalADLLogManager
                                                           .getLogger("loader.InputResourceLocator");

  protected Map<String, GenericResourceLocator> genericResourceLocators;

  @Inject
  protected void setGenericResourceLocators(
      final Set<GenericResourceLocator> locators) {
    genericResourceLocators = new HashMap<String, GenericResourceLocator>(
        locators.size());
    for (final GenericResourceLocator locator : locators) {
      for (final String resourceKing : locator.getResourceKind()) {
        genericResourceLocators.put(resourceKing, locator);
      }
    }
  }

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
    if (genericResourceLocator == null) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Missing GenericResourceLocator for input resources of kind \""
              + resource.getKind() + "\".");
    }
    final URL location = genericResourceLocator.findResource(
        resource.getName(), context);
    if (location != null) resource.setLocation(location);
    return location;
  }

  public boolean isUpToDate(final long timestamp,
      final Collection<InputResource> inputs, final Map<Object, Object> context) {
    if (ForceRegenContextHelper.getForceRegen(context)) {
      if (logger.isLoggable(Level.FINEST))
        logger
            .log(Level.FINEST, "Forced mode, isUpToDate method return false.");
      return false;
    }

    if (inputs == null) {
      // this happens when there is no real file attached to a component.
      return false;
    }

    for (final InputResource dependency : inputs) {
      try {
        if (getTimestamp(dependency, context) >= timestamp) {
          // a dependency is newer than given timestamp.
          if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, "Dependency \"" + dependency
                + "\" is more recent (location=\"" + dependency.getLocation()
                + "\"), isUpToDate method return false.");
          return false;
        }
      } catch (final MalformedURLException e) {
        if (logger.isLoggable(Level.WARNING))
          logger.log(Level.WARNING, "Can't determine file timestamps of "
              + dependency);
        // if one dependency is missing consider the file as out of date.
        return false;
      }
    }
    if (logger.isLoggable(Level.FINEST))
      logger.log(Level.FINEST,
          "Dependencies are up-to-dates, isUpToDate method return true.");
    return true;
  }

  public boolean isUpToDate(final File file,
      final Collection<InputResource> inputs, final Map<Object, Object> context) {
    if (logger.isLoggable(Level.FINEST))
      logger.log(Level.FINEST, "Checks if file \"" + file + "\" is up-to-date");
    return isUpToDate(file.lastModified(), inputs, context);
  }

  public boolean isUpToDate(final URL url,
      final Collection<InputResource> inputs, final Map<Object, Object> context)
      throws MalformedURLException {
    if (logger.isLoggable(Level.FINEST))
      logger.log(Level.FINEST, "Checks if URL \"" + url + "\" is up-to-date");
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
        timestamp = 0L;
      resource.setTimestamp(timestamp);
    }
    return timestamp;
  }
}
