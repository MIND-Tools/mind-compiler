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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;

/**
 * Helper class for the manipulation of the {@link #INPUT_RESOURCES_DECORATION
 * input resources} decoration. This decoration allows to attach to a
 * {@link Node} a set that contains the resources the AST depends on.
 */
public final class InputResourcesHelper {
  private InputResourcesHelper() {
  }

  /**
   * The name of the "input resources" decoration.
   * 
   * @see #getInputResources(Definition)
   * @see #addInputResource(Definition, URL)
   * @see #addInputResources(Definition, Set)
   */
  public static final String INPUT_RESOURCES_DECORATION = "input-resources";

  /**
   * Returns the value of the {@link #INPUT_RESOURCES_DECORATION input
   * resources} decoration associated to the given node. May be
   * <code>null</code>.
   * 
   * @param node a node.
   * @return a set of name. May be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public static Set<InputResource> getInputResources(final Node node) {
    return (Set<InputResource>) node
        .astGetDecoration(INPUT_RESOURCES_DECORATION);
  }

  /**
   * Add the given resource to the "input resources" decoration associated to
   * the given node.
   * 
   * @param node a node.
   * @param resource a resource (ADL name, IDL file name, etc...).
   * @return <code>true</code> if the given resource has been actually added to
   *         the set of input resources (i.e. if the set of input resources did
   *         not contain the given resource).
   */
  public static boolean addInputResource(final Node node,
      final InputResource resource) {
    Set<InputResource> resources = getInputResources(node);
    if (resources == null) {
      resources = new HashSet<InputResource>();
      node.astSetDecoration(INPUT_RESOURCES_DECORATION, resources);
    }

    return resources.add(resource);
  }

  /**
   * Add the given set of input resources to the "input resources" decoration
   * associated to the given node.
   * 
   * @param node a node.
   * @param resources the resources to add. May be <code>null</code>.
   */
  public static void addInputResources(final Node node,
      final Set<InputResource> resources) {
    if (resources == null) return;

    final Set<InputResource> previousResources = getInputResources(node);
    if (previousResources == null) {
      node.astSetDecoration(INPUT_RESOURCES_DECORATION,
          new HashSet<InputResource>(resources));
    } else {
      previousResources.addAll(resources);
    }
  }

  /**
   * Get the time-stamp corresponding to the given {@link URL}.
   * 
   * @param resource a {@link URL}
   * @return the time-stamp of the file designated by the given {@link URL}.
   * @throws MalformedURLException is the given {@link URL} is invalid.
   * @see InputResourceLocator#findResource(InputResource, java.util.Map)
   */
  public static long getTimestamp(final URL resource)
      throws MalformedURLException {
    if ("file".equals(resource.getProtocol())) {
      try {
        return new File(resource.toURI()).lastModified();
      } catch (final URISyntaxException e) {
        final MalformedURLException mue = new MalformedURLException(
            "Invalid URL");
        mue.initCause(e);
        throw mue;
      }
    } else if ("jar".equals(resource.getProtocol())) {
      final String path = resource.getPath();
      final int i = path.indexOf('!');
      if (i == -1)
        throw new MalformedURLException("Invalid path of 'jar' URL: '" + path
            + "'");

      final URL jarFile = new URL(path.substring(0, i));
      return getTimestamp(jarFile);
    }
    return 0;
  }
}
