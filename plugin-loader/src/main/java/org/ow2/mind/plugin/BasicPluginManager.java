/**
 * Copyright (C) 2010 STMicroelectronics
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
 * Contributors: Ali Erdem Ozcan
 */

package org.ow2.mind.plugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.plugin.ast.Extension;
import org.ow2.mind.plugin.ast.ExtensionPoint;
import org.ow2.mind.plugin.ast.Plugin;
import org.ow2.mind.plugin.ast.PluginASTHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BasicPluginManager implements PluginManager {

  public static final String PLUGIN_XML   = "mind-plugin.xml";

  protected PluginRegistry   pluginRegistry;
  protected ClassLoader      classLoader;
  protected DocumentBuilder  builder      = null;

  protected static Logger    pluginLogger = FractalADLLogManager
                                              .getLogger("plugin");

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** Client interface for node factory. */
  public NodeFactory         nodeFactoryItf;

  // public XMLNodeFactory xmlNodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  public BasicPluginManager() {
    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory
        .newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (final ParserConfigurationException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Can't initialize document builder");
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the PluginManager interface
  // ---------------------------------------------------------------------------

  public void setClassLoader(final ClassLoader cl) {
    classLoader = cl;
    // invalidate plugin registry
    pluginRegistry = null;
  };

  public Collection<Extension> getExtensions(final String extensionPoint,
      final Map<Object, Object> context) throws ADLException {
    Collection<Extension> oo = getRegistry(context).extensions
        .get(extensionPoint);

    if (oo != null)
      return Collections.unmodifiableCollection(oo);
    else
      return Collections.emptySet();
  }

  // ---------------------------------------------------------------------------
  // Utility classes and methods
  // ---------------------------------------------------------------------------

  protected PluginRegistry initRegistry(final Map<Object, Object> context)
      throws ADLException {
    final PluginRegistry registry = new PluginRegistry();

    final ClassLoader classLoader = (this.classLoader != null)
        ? this.classLoader
        : this.getClass().getClassLoader();
    Enumeration<URL> plugins = null;
    try {
      plugins = classLoader.getResources(PLUGIN_XML);
    } catch (final IOException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Can't find plugin descriptors");
    }

    while (plugins.hasMoreElements()) {
      final URL url = plugins.nextElement();
      initPlugin(url, registry, context);
    }

    // bind extensions to extension points :
    for (final Plugin plugin : registry.plugins.values()) {
      if (pluginLogger.isLoggable(Level.FINE)) {
        pluginLogger.fine("Initializing the plugin " + plugin.getId());
      }
      for (final Extension extension : plugin.getExtensions()) {
        final ExtensionPoint point = registry.extensionPoints.get(extension
            .getPoint());
        if (point == null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Unknown Extenstion point ID \"" + extension.getPoint()
                  + "\" referenced by plugin \"" + plugin.getId() + "\".");
        }
        registry.extensions.get(extension.getPoint()).add(extension);
      }
    }

    return registry;
  }

  protected void initPlugin(final URL pluginDesc,
      final PluginRegistry registry, final Map<Object, Object> context)
      throws ADLException {

    final Plugin plugin = loadPlugin(pluginDesc);
    final Plugin prevPlugin = registry.plugins.put(plugin.getId(), plugin);
    if (prevPlugin != null) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Invalid plugin ID \"" + plugin.getId()
              + "\" A plugin with the same name is already defined.");
    }

    for (final ExtensionPoint point : plugin.getExtensionPoints()) {
      final ExtensionPoint prevPoint = registry.extensionPoints.put(
          getQualifiedExtensionName(plugin, point), point);
      if (prevPoint != null) {
        throw new CompilerError(
            GenericErrors.INTERNAL_ERROR,
            "Invalid Extenstion point ID \""
                + plugin.getId()
                + "\" An extension point with the same name is already defined.");
      }
      registry.extensions.put(getQualifiedExtensionName(plugin, point),
          new ArrayList<Extension>());
    }
  }

  protected String getQualifiedExtensionName(Plugin plugin, ExtensionPoint point) {
    return plugin.getId() + "." + point.getId();
  }

  protected PluginRegistry getRegistry(final Map<Object, Object> context)
      throws ADLException {
    if (pluginRegistry == null) {
      pluginRegistry = initRegistry(context);
    }
    return pluginRegistry;
  }

  protected Plugin loadPlugin(final URL pluginDesc) throws ADLException {
    Document document = null;
    try {
      document = builder.parse(pluginDesc.openStream());
    } catch (final FileNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to find the plugin descriptor '" + pluginDesc + "'.");
    } catch (final IOException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to access the plugin descriptor '" + pluginDesc + "'.");
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to parse the XML plugin descriptor '" + pluginDesc + "'.");
    }
    final Element root = document.getDocumentElement();
    // Setting the base information
    final Plugin plugin = newPluginNode(root.getAttribute("id"), root
        .getAttribute("name"));

    // Setting the extension points
    final NodeList nodes = root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      final org.w3c.dom.Node node = nodes.item(i);
      if (node instanceof Element) {
        final Element element = (Element) node;
        if (element.getNodeName().equals("extension")) {
          final Extension extension = newExtensionNode(element
              .getAttribute("point"));
          PluginASTHelper.setExtensionConfig(extension, element);
          plugin.addExtension(extension);
        } else if (element.getNodeName().equals("extension-point")) {
          final ExtensionPoint extensionPoint = newExtensionPointNode(element
              .getAttribute("id"), element.getAttribute("dtd"));
          plugin.addExtensionPoint(extensionPoint);
        }
      }
    }
    return plugin;
  }

  // protected Node loadASTFromDom(final String dtd, final Element element) {
  // XMLNode node;
  // try {
  // node = xmlNodeFactoryItf.newXMLNode(dtd, element.getLocalName());
  // } catch (final SAXException e) {
  // throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
  // "Invalid extension point DTD \"" + dtd + "\".");
  // }
  //
  // // set attributes
  // final NamedNodeMap attributes = element.getAttributes();
  // final Map<String, String> attributeMap = new HashMap<String, String>(
  // attributes.getLength());
  // for (int i = 0; i < attributes.getLength(); i++) {
  // final Attr attr = (Attr) attributes.item(i);
  // attributeMap.put(attr.getName(), attr.getValue());
  // }
  // node.astSetAttributes(attributeMap);
  //
  // // add sub nodes
  // final NodeList childNodes = element.getChildNodes();
  // for (int i = 0; i < childNodes.getLength(); i++) {
  // final org.w3c.dom.Node child = childNodes.item(i);
  // if (child instanceof Element) {
  // node.astAddNode(loadASTFromDom(dtd, (Element) child));
  // }
  // }
  //
  // return node;
  // }

  protected Plugin newPluginNode(final String id, final String name) {
    final Plugin plugin = newNode(nodeFactoryItf, "plugin", Plugin.class);
    plugin.setId(id);
    plugin.setName(name);
    return plugin;
  }

  protected ExtensionPoint newExtensionPointNode(final String id,
      final String dtd) {
    final ExtensionPoint extensionPoint = newNode(nodeFactoryItf,
        "extensionPoint", ExtensionPoint.class);
    extensionPoint.setId(id);
    extensionPoint.setDtd(dtd);
    return extensionPoint;
  }

  protected Extension newExtensionNode(final String point) {
    final Extension extension = newNode(nodeFactoryItf, "extension",
        Extension.class);
    extension.setPoint(point);
    return extension;
  }

  protected static final class PluginRegistry {
    final Map<String, Plugin>                plugins         = new HashMap<String, Plugin>();
    final Map<String, ExtensionPoint>        extensionPoints = new HashMap<String, ExtensionPoint>();
    final Map<String, Collection<Extension>> extensions      = new HashMap<String, Collection<Extension>>();
  }

  protected static <T extends Node> T newNode(final NodeFactory nodeFactory,
      final String nodeType, final Class<T> nodeItf, final Class<?>... itfs) {
    final String[] itfNames = new String[itfs.length + 1];
    itfNames[0] = nodeItf.getName();
    for (int i = 0; i < itfs.length; i++) {
      itfNames[i + 1] = itfs[i].getName();
    }
    try {
      return nodeItf.cast(nodeFactory.newNode(nodeType, itfNames));
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unexpected error.");
    }
  }

}
