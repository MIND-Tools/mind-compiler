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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.w3c.dom.Element;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAny;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDCardinal;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEmpty;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDParser;
import com.wutka.dtd.DTDSequence;

class ExtensionPointImpl implements ExtensionPoint {

  private static final String                    EXTENSION_ELEMENT_NAME = "extension";
  private static final String                    ID_ATTR_NAME           = "id";
  private static final String                    NAME_ATTR_NAME         = "name";
  private static final String                    DTD_ATTR_NAME          = "dtd";

  private final String                           id;
  private final String                           qualifiedId;
  private final String                           name;
  private final Plugin                           plugin;
  private final DTD                              dtd;
  private final List<Extension>                  extensions;
  private final WeakHashMap<DTDElement, Pattern> patternCache           = new WeakHashMap<DTDElement, Pattern>();

  ExtensionPointImpl(final PluginImpl plugin, final Element element) {
    this.plugin = plugin;

    id = element.getAttribute(ID_ATTR_NAME);
    if (id == null) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Invalid extenstion point, missing id in '" + element.getBaseURI()
              + "'.");
    }
    qualifiedId = plugin.getId() + "." + id;
    name = element.getAttribute(NAME_ATTR_NAME);

    extensions = new ArrayList<Extension>();

    final String dtd = element.getAttribute(DTD_ATTR_NAME);
    if (dtd != null && dtd.length() > 0) {
      final URL dtdURL;
      try {
        final URL pluginURL = plugin.getDescriptorURL();
        if (pluginURL.getProtocol().equals("jar")) {
          String path = pluginURL.getPath();
          path = path.substring(0, path.lastIndexOf('!'));
          dtdURL = new URL(pluginURL.getProtocol(), pluginURL.getHost(),
              pluginURL.getPort(), path + "!/" + dtd);
        } else {
          dtdURL = pluginURL.toURI().resolve(dtd).normalize().toURL();
        }
      } catch (final MalformedURLException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Can't find extension point DTD '" + dtd + "'.");
      } catch (final URISyntaxException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Can't find extension point DTD '" + dtd + "'.");
      }

      try {
        this.dtd = new DTDParser(new InputStreamReader(dtdURL.openStream()))
            .parse();
      } catch (final IOException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
            "Can't read extension point DTD '" + dtd + "'.");
      }
      if (this.dtd.elements.get(EXTENSION_ELEMENT_NAME) == null) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR, "Invalid DTD '"
            + dtd + "' missing definition of element '"
            + EXTENSION_ELEMENT_NAME + "'.");
      }

    } else {
      this.dtd = null;
    }
  }

  public String getId() {
    return id;
  }

  public String getQualifiedId() {
    return qualifiedId;
  }

  public String getName() {
    return name;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public Iterable<Extension> getExtensions() {
    return Iterables.unmodifiableIterable(extensions);
  }

  public Iterable<ConfigurationElement> getConfigurationElements() {
    return Iterables.unmodifiableIterable(Iterables.concat(Iterables.transform(
        extensions, new Function<Extension, Iterable<ConfigurationElement>>() {
          public Iterable<ConfigurationElement> apply(final Extension from) {
            return from.getConfigurationElements();
          }
        })));

  }

  public Iterable<ConfigurationElement> getConfigurationElements(
      final String name) {
    return Iterables.filter(getConfigurationElements(),
        new Predicate<ConfigurationElement>() {
          public boolean apply(final ConfigurationElement input) {
            return input.getName().equals(name);
          }
        });
  }

  protected void bindExtension(final Extension extension) {
    extensions.add(extension);

    if (dtd != null) {
      checkElementContent(extension.getConfigurationElements(),
          (DTDElement) dtd.elements.get(EXTENSION_ELEMENT_NAME));
    }
  }

  @SuppressWarnings("unchecked")
  void checkElement(final ConfigurationElement element) throws CompilerError {
    final String elementName = element.getName();
    final DTDElement dtdElement = (DTDElement) dtd.elements.get(elementName);
    if (dtdElement == null) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR,
          "Invalid element name '" + elementName + "' in extension.");
    }
    final Map<String, DTDAttribute> dtdAttributes = new HashMap<String, DTDAttribute>(
        dtdElement.attributes);
    final Map<String, String> attributes = element.getAttributes();
    for (final String attrName : attributes.keySet()) {
      final DTDAttribute dtdAttr = dtdAttributes.remove(attrName);
      if (dtdAttr == null) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Invalid attribute name '" + attrName + "' in extension.");
      }
    }
    // scan remaining dtdAttribute and check their contingency
    for (final DTDAttribute dtdAttr : dtdAttributes.values()) {
      if (dtdAttr.defaultValue != null) {
        ((ConfigurationElementImpl) element).setAttribute(dtdAttr.name,
            dtdAttr.defaultValue);
      } else if (dtdAttr.getDecl().equals(DTDDecl.REQUIRED)) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Missing attribute name '" + dtdAttr.name + "' in extension.");
      }
    }

    checkElementContent(element.getChildren(), dtdElement);
  }

  void checkElementContent(final Iterable<ConfigurationElement> children,
      final DTDElement dtdElement) throws CompilerError {
    if (!(dtdElement.content instanceof DTDEmpty || dtdElement.content instanceof DTDAny)) {
      final Pattern pattern = getPattern(dtdElement);
      if (!pattern.matcher(buildChildrenSequence(children)).matches()) {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "Invalid content of element '" + dtdElement.name
                + "' in extension.");
      }

      for (final ConfigurationElement child : children) {
        checkElement(child);
      }
    }
  }

  String buildChildrenSequence(final Iterable<ConfigurationElement> children) {
    final StringBuilder sb = new StringBuilder();
    for (final ConfigurationElement child : children) {
      sb.append(",").append(child.getName());
    }
    return sb.toString();
  }

  Pattern getPattern(final DTDElement element) {
    Pattern pattern = patternCache.get(element);
    if (pattern == null) {
      final StringBuilder sb = new StringBuilder();
      buildRegExp(element.content, sb);
      pattern = Pattern.compile(sb.toString());
      patternCache.put(element, pattern);
    }
    return pattern;
  }

  void buildRegExp(final DTDItem item, final StringBuilder sb) {
    if (item instanceof DTDName) {
      sb.append("(,").append(((DTDName) item).value).append(")");
    } else if (item instanceof DTDChoice) {
      sb.append("(");
      final DTDItem[] subItems = ((DTDChoice) item).getItem();
      for (int i = 0; i < subItems.length; i++) {
        buildRegExp(subItems[i], sb);
        if (i < subItems.length - 1) sb.append("|");
      }
      sb.append(")");
    } else if (item instanceof DTDSequence) {
      sb.append("(");
      final DTDItem[] subItems = ((DTDSequence) item).getItem();
      for (final DTDItem subItem : subItems) {
        buildRegExp(subItem, sb);
      }
      sb.append(")");
    } else {
      throw new CompilerError(GenericErrors.GENERIC_ERROR,
          "Invalid DTD in extension point '" + id + ".");
    }

    if (item.cardinal == DTDCardinal.ONEMANY) {
      sb.append("+");
    } else if (item.cardinal == DTDCardinal.ZEROMANY) {
      sb.append("*");
    } else if (item.cardinal == DTDCardinal.OPTIONAL) {
      sb.append("?");
    }
  }
}
