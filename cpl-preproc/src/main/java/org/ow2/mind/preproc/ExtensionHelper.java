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

package org.ow2.mind.preproc;

import static org.ow2.mind.plugin.ast.PluginASTHelper.getExtensionConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.TokenStream;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.ow2.mind.preproc.parser.AbstractCPLParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ExtensionHelper {
  public static final String                         CPL_EXTENSION   = "org.ow2.mind.preproc.cpl-parser";

  private static final DefaultCPLPreprocessorFactory defaultFactory  = new DefaultCPLPreprocessorFactory();

  private static CPLPreprocessorFactory              ppFactory       = null;

  private static boolean                             extensionLoaded = false;

  private static void loadExtension(final PluginManager pluginManagerItf,
      final Map<Object, Object> context) throws ADLException {
    if (!extensionLoaded) {
      final Collection<Extension> extensions = pluginManagerItf.getExtensions(
          CPL_EXTENSION, context);
      if (extensions.size() == 0) {
        ppFactory = defaultFactory;
      } else if (extensions.size() == 1) {
        ppFactory = getExtensionFactory(extensions.iterator().next(),
            "factory", CPLPreprocessorFactory.class);
      } else {
        throw new CompilerError(GenericErrors.GENERIC_ERROR,
            "There are more than one extensions for the extension-point '"
                + CPL_EXTENSION + "'. This is illegal.");
      }
      extensionLoaded = true;
    }
  }

  public static Lexer getLexer(final PluginManager pluginManagerItf,
      final String inputPath, final Map<Object, Object> context)
      throws ADLException, IOException {
    if (!extensionLoaded) {
      loadExtension(pluginManagerItf, context);
    }
    return ppFactory.getLexer(new ANTLRFileStream(inputPath));
  }

  public static AbstractCPLParser getParser(
      final PluginManager pluginManagerItf, final TokenStream tokens,
      final Map<Object, Object> context) throws ADLException {
    if (!extensionLoaded) {
      loadExtension(pluginManagerItf, context);
    }
    return ppFactory.getParser(tokens);
  }

  public static <T> T getExtensionFactory(final Extension extension,
      final String extensionName, final Class<? extends T> expectedType)
      throws ADLException {
    String className;
    final NodeList nodes = getExtensionConfig(extension).getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      final Node node = nodes.item(i);
      if (node instanceof Element) {
        final Element element = (Element) node;
        if (element.getNodeName().equals(extensionName)) {
          className = element.getAttribute("class");
          try {
            final Class<? extends T> extensionClass = ExtensionHelper.class
                .getClassLoader().loadClass(className).asSubclass(expectedType);
            return extensionClass.newInstance();
          } catch (final ClassNotFoundException e) {
            throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
                "Extension class '" + extensionName + "' not found.");
          } catch (final InstantiationException e) {
            throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
                "Extension class '" + extensionName
                    + "' cannot be instantiated.");
          } catch (final IllegalAccessException e) {
            throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
                "Illegal access to the extension class '" + extensionName
                    + "'.");
          }
        }
      }
    }
    throw new CompilerError(GenericErrors.GENERIC_ERROR, "No extension class '"
        + extensionName + "' found.");
  }
}
