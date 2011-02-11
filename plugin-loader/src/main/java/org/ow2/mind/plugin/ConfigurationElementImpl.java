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
 * Contributors: 
 */

package org.ow2.mind.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

class ConfigurationElementImpl implements ConfigurationElement {

  private final PluginImpl                 plugin;
  private final Object                     parent;
  private final String                     name;
  private final Map<String, String>        attributes;
  private final List<ConfigurationElement> children;

  ConfigurationElementImpl(final PluginImpl plugin, final Object parent,
      final Element element) {
    this.plugin = plugin;
    this.parent = parent;
    name = element.getNodeName();
    final NamedNodeMap attrs = element.getAttributes();
    attributes = new HashMap<String, String>(attrs.getLength());
    for (int i = 0; i < attrs.getLength(); i++) {
      final Attr attr = (Attr) attrs.item(i);
      attributes.put(attr.getName(), attr.getValue());
    }

    final NodeList childNodes = element.getChildNodes();
    children = new ArrayList<ConfigurationElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node node = childNodes.item(i);
      if (!(node instanceof Element)) continue;
      children.add(new ConfigurationElementImpl(plugin, this, (Element) node));
    }
  }

  public String getName() {
    return name;
  }

  public String getAttribute(final String name) {
    return attributes.get(name);
  }

  public String getAttribute(final String name, final String defaultValue) {
    return attributes.containsKey(name) ? attributes.get(name) : defaultValue;
  }

  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public <T> T createInstance(final String attrName, final Class<T> expectedType) {
    final String className = getAttribute(attrName);
    if (className == null) return null;
    Class<?> uncastedClass;
    try {
      uncastedClass = plugin.getClassLoader().loadClass(className);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
          "Can't load class '" + className + "'");
    }
    Class<? extends T> castedClass;
    try {
      castedClass = uncastedClass.asSubclass(expectedType);
    } catch (final ClassCastException e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e, "Class '"
          + className + "' is not a sub-class of '" + expectedType.getName()
          + "'.");
    }
    return plugin.getPluginManager().getInstance(castedClass);
  }

  public Iterable<ConfigurationElement> getChildren() {
    return Iterables.unmodifiableIterable(children);
  }

  public ConfigurationElement getChild() {
    if (children.isEmpty())
      return null;
    else
      return children.get(0);
  }

  public Iterable<ConfigurationElement> getChildren(final String name) {
    return Iterables.filter(children, new Predicate<ConfigurationElement>() {

      public boolean apply(final ConfigurationElement input) {
        return input.getName().equals(name);
      }
    });
  }

  public ConfigurationElement getChild(final String name) {
    final Iterator<ConfigurationElement> children = getChildren(name)
        .iterator();
    return children.hasNext() ? children.next() : null;
  }

  public Object getParent() {
    return parent;
  }

  void setAttribute(final String name, final String value) {
    attributes.put(name, value);
  }

}
