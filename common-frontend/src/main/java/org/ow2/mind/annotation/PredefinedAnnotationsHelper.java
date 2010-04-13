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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.annotation;

import static org.ow2.mind.plugin.ast.PluginASTHelper.getExtensionConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class PredefinedAnnotationsHelper {

  public static final String ANNOTATION_EXTENSIONS = "org.ow2.mind.predefined-annotations";
  public static final String ANNOTATION            = "annotation";
  public static final String PACKAGE               = "package";

  private PredefinedAnnotationsHelper() {
  }

  public static String[] getPredefinedAnnotations(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    final Collection<Extension> extensions = pluginManagerItf.getExtensions(
        ANNOTATION_EXTENSIONS, context);
    final List<String> annotationPackages = new ArrayList<String>();
    for (final Extension extension : extensions) {
      annotationPackages.add(getAnnotationPackage(extension));
    }
    return annotationPackages.toArray(new String[0]);
  }

  private static String getAnnotationPackage(final Extension extension)
      throws ADLException {
    final NodeList nodes = getExtensionConfig(extension).getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      final Node node = nodes.item(i);
      if (node instanceof Element) {
        final Element element = (Element) node;
        if (element.getNodeName().equals(ANNOTATION)) {
          return element.getAttribute(PACKAGE);
        }
      }
    }
    throw new ADLException(GenericErrors.GENERIC_ERROR,
        "Annotation package element not found.");
  }
}
