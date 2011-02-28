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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Basic implementation of the {@link PluginManager} interface.
 */
@Singleton
public class BasicPluginManager implements PluginManager {

  public final static String  PLUGIN_CLASS_LOADER          = "plugin-class-loader";

  private static final String EXTENSION_POINT_ELEMENT_NAME = "extension-point";
  private static final String EXTENSION_ELEMENT_NAME       = "extension";
  private static final String PLUGIN_NAME_ATTR_NAME        = "name";
  private static final String PLUGIN_ID_ATTR_NAME          = "id";

  public static final String  PLUGIN_XML                   = "mind-plugin.xml";

  protected PluginRegistry    pluginRegistry;

  protected DocumentBuilder   builder                      = null;

  @Inject
  @Named(PLUGIN_CLASS_LOADER)
  protected ClassLoader       classLoader;

  @Inject
  protected Injector          injector;

  protected static Logger     pluginLogger                 = FractalADLLogManager
                                                               .getLogger("plugin");

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

  public ExtensionPoint getExtensionPoint(final String extensionPoint) {
    return getRegistry().extensionPoints.get(extensionPoint);
  }

  public Iterable<Extension> getExtensions(final String extensionPoint) {
    final ExtensionPoint point = getExtensionPoint(extensionPoint);
    if (point == null) return null;
    return point.getExtensions();
  }

  public Iterable<ConfigurationElement> getConfigurationElements(
      final String extensionPoint) {
    final ExtensionPoint point = getExtensionPoint(extensionPoint);
    if (point == null) return null;
    return point.getConfigurationElements();
  }

  public Iterable<ConfigurationElement> getConfigurationElements(
      final String extensionPoint, final String name) {
    final ExtensionPoint point = getExtensionPoint(extensionPoint);
    if (point == null) return null;
    return point.getConfigurationElements(name);
  }

  public Iterable<String> getExtensionPointNames() {
    return Iterables.unmodifiableIterable(getRegistry().extensionPoints
        .keySet());
  }

  public <T> T getInstance(final Class<T> clazz) {
    return injector.getInstance(clazz);
  }

  // ---------------------------------------------------------------------------
  // Utility classes and methods
  // ---------------------------------------------------------------------------

  protected PluginRegistry initRegistry() {
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
      initPlugin(classLoader, url, registry);
    }

    // bind extensions to extension points :
    for (final Plugin plugin : registry.plugins.values()) {
      if (pluginLogger.isLoggable(Level.FINE)) {
        pluginLogger.fine("Initializing the plugin " + plugin.getId());
      }
      for (final Extension extension : plugin.getExtensions()) {
        final ExtensionPointImpl point = registry.extensionPoints.get(extension
            .getExtensionPointID());
        if (point == null) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Unknown Extenstion point ID \""
                  + extension.getExtensionPointID()
                  + "\" referenced by plugin \"" + plugin.getId() + "\".");
        }
        point.bindExtension(extension);
      }
    }

    return registry;
  }

  protected void initPlugin(final ClassLoader classLoader,
      final URL pluginDesc, final PluginRegistry registry) {

    final Document document;
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
    final String id = root.getAttribute(PLUGIN_ID_ATTR_NAME);
    final String name = root.getAttribute(PLUGIN_NAME_ATTR_NAME);

    // create plugin object and add it in the registry
    final PluginImpl plugin = new PluginImpl(this, pluginDesc, id, name,
        classLoader);
    final Plugin prevPlugin = registry.plugins.put(plugin.getId(), plugin);
    if (prevPlugin != null) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Invalid plugin ID \"" + plugin.getId()
              + "\" A plugin with the same name is already defined.");
    }

    // add extension and extension points to plugin and registry
    final NodeList nodes = root.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      final Node node = nodes.item(i);
      if (!(node instanceof Element)) continue;
      final Element element = (Element) node;
      if (element.getNodeName().equals(EXTENSION_ELEMENT_NAME)) {
        plugin.addExtension(new ExtensionImpl(plugin, element));
      } else if (element.getNodeName().equals(EXTENSION_POINT_ELEMENT_NAME)) {
        final ExtensionPointImpl point = new ExtensionPointImpl(plugin, element);
        final ExtensionPoint prevPoint = registry.extensionPoints.put(
            point.getQualifiedId(), point);
        if (prevPoint != null) {
          throw new CompilerError(
              GenericErrors.INTERNAL_ERROR,
              "Invalid Extenstion point ID \""
                  + id
                  + "\" An extension point with the same name is already defined.");
        }
        plugin.addExtensionPoint(point);
      }
    }

  }

  protected synchronized PluginRegistry getRegistry() {
    if (pluginRegistry == null) {
      pluginRegistry = initRegistry();
    }
    return pluginRegistry;
  }

  protected static final class PluginRegistry {
    final Map<String, PluginImpl>         plugins         = new HashMap<String, PluginImpl>();
    final Map<String, ExtensionPointImpl> extensionPoints = new HashMap<String, ExtensionPointImpl>();
  }
}
