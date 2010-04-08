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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.VoidVisitor;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DefinitionVisitorExtensionHelper {
  private DefinitionVisitorExtensionHelper() {
  }

  public static final String                    DEFINITION_VISITOR_EXTENSION = "org.ow2.mind.adl.definition-visitors";

  protected static Collection<VisitorExtension> visitorExtensions            = null;

  public static Collection<VisitorExtension> getVisitorExtensions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    if (visitorExtensions == null) {
      initVisitorExtensions(pluginManagerItf, context);
    }
    return visitorExtensions;
  }

  protected static void initVisitorExtensions(
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    visitorExtensions = new ArrayList<VisitorExtension>();

    final Collection<Extension> extensions = pluginManagerItf.getExtensions(
        DEFINITION_VISITOR_EXTENSION, context);
    for (final Extension extension : extensions) {
      final VisitorExtension visitorExtension = new VisitorExtension();
      visitorExtensions.add(visitorExtension);
      final NodeList nodes = ((Element) extension
          .astGetDecoration("xml-element")).getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        final Node node = nodes.item(i);
        if (node instanceof Element) {
          final Element element = (Element) node;
          if (element.getNodeName().equals("visitor")) {
            visitorExtension.setVisitor(element.getAttribute("class"));
            visitorExtension.setVisitorName(element.getAttribute("name"));
          } else if (element.getNodeName().equals("requires")) {
            final NodeList requiresNodes = element.getChildNodes();
            for (int k = 0; k < requiresNodes.getLength(); k++) {
              final Node requiresNode = requiresNodes.item(k);
              if (requiresNode instanceof Element) {
                final Element requiresElement = (Element) requiresNode;
                if (requiresElement.getNodeName().equals("interface")) {
                  visitorExtension.addRequiredInterface(requiresElement
                      .getAttribute("name"));
                }
              }
            }
          }
        }
      }
    }
  }

  protected static final class VisitorExtension {
    private VoidVisitor<Definition>  visitor            = null;
    private String                   visitorName        = null;
    final private Collection<String> requiredInterfaces = new ArrayList<String>(); ;

    public VoidVisitor<Definition> getVisitor() {
      return visitor;
    }

    public void setVisitor(final String visitorClass) throws ADLException {
      try {
        visitor = VisitorExtension.class.getClassLoader().loadClass(
            visitorClass).asSubclass(VoidVisitor.class).newInstance();
      } catch (final InstantiationException e) {
        throw new ADLException(GenericErrors.GENERIC_ERROR, e,
            "Extension class '" + visitorClass + "' cannot be instantiated.");
      } catch (final IllegalAccessException e) {
        throw new ADLException(GenericErrors.GENERIC_ERROR, e,
            "Illegal access to the extension class '" + visitorClass + "'.");
      } catch (final ClassNotFoundException e) {
        throw new ADLException(GenericErrors.GENERIC_ERROR, e,
            "Extension class '" + visitorClass + "' not found.");
      }
    }

    public String getVisitorName() {
      return visitorName;
    }

    public Collection<String> getRequiredInterfaces() {
      return requiredInterfaces;
    }

    public void setVisitorName(final String visitorName) {
      this.visitorName = visitorName;
    }

    public void addRequiredInterface(final String itf) {
      requiredInterfaces.add(itf);
    }

  }
}
