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

package org.ow2.mind.adl;

import static org.ow2.mind.plugin.ast.PluginASTHelper.getExtensionConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.VoidVisitor;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VisitorExtensionHelper {

  public static final String                                 DEFINITION_SOURCE_GENERATOR_EXTENSION = "org.ow2.mind.adl.definition-source-generators";
  public static final String                                 INSTANCE_SOURCE_GENERATOR             = "org.ow2.mind.adl.instance-source-generators";

  public static final String[]                               extensionPoints                       = {
      DEFINITION_SOURCE_GENERATOR_EXTENSION, INSTANCE_SOURCE_GENERATOR                             };

  protected static Map<String, Collection<VisitorExtension>> visitorExtensions                     = null;

  public static Collection<VisitorExtension> getVisitorExtensions(
      final String extensionPoint, final PluginManager pluginManagerItf,
      final Map<Object, Object> context) throws ADLException {
    if (visitorExtensions == null) {
      initVisitorExtensions(pluginManagerItf, context);
    }
    return visitorExtensions.get(extensionPoint);
  }

  protected static void initVisitorExtensions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    visitorExtensions = new HashMap<String, Collection<VisitorExtension>>();
    for (final String extensionPoint : extensionPoints) {
      final Collection<VisitorExtension> extPointExtensions = new ArrayList<VisitorExtension>();
      visitorExtensions.put(extensionPoint, extPointExtensions);
      final Collection<Extension> extensions = pluginManagerItf.getExtensions(
          extensionPoint, context);
      for (final Extension extension : extensions) {
        final VisitorExtension visitorExtension = new VisitorExtension();
        extPointExtensions.add(visitorExtension);
        final NodeList nodes = getExtensionConfig(extension).getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
          final Node node = nodes.item(i);
          if (node instanceof Element) {
            final Element element = (Element) node;
            if (element.getNodeName().equals("visitor")) {
              visitorExtension.setVisitor(element.getAttribute("class"));
              visitorExtension.setVisitorName(element.getAttribute("name"));
            }
          }
        }
      }
    }
  }

  protected static final class VisitorExtension {
    private VoidVisitor<Definition> visitor     = null;
    private String                  visitorName = null;

    public VoidVisitor<?> getVisitor() {
      return visitor;
    }

    public void setVisitor(final String visitorClass) throws ADLException {
      try {
        visitor = VisitorExtension.class.getClassLoader()
            .loadClass(visitorClass).asSubclass(VoidVisitor.class)
            .newInstance();
      } catch (final InstantiationException e) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
            "Extension class '" + visitorClass + "' cannot be instantiated.");
      } catch (final IllegalAccessException e) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
            "Illegal access to the extension class '" + visitorClass + "'.");
      } catch (final ClassNotFoundException e) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
            "Extension class '" + visitorClass + "' not found.");
      }
    }

    public String getVisitorName() {
      return visitorName;
    }

    public void setVisitorName(final String visitorName) {
      this.visitorName = visitorName;
    }

  }
}
