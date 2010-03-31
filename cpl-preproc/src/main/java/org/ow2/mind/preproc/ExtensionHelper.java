
package org.ow2.mind.preproc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenStream;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.ast.Extension;
import org.ow2.mind.preproc.parser.CPLLexer;
import org.ow2.mind.preproc.parser.CPLParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ExtensionHelper {
  public static final String CPL_EXTENSION = "org.ow2.mind.preproc.cpl-parser";

  public static Lexer getLexer(final PluginManager pluginManagerItf,
      final String inputPath, final Map<Object, Object> context)
      throws ADLException, IOException {
    final Collection<Extension> extensions = pluginManagerItf.getExtensions(
        CPL_EXTENSION, context);
    if (extensions.size() == 0) {
      // Return the default lexer
      return new CPLLexer(new ANTLRFileStream(inputPath));
    }
    if (extensions.size() > 1) {
      throw new ADLException(GenericErrors.GENERIC_ERROR,
          "There are more than one extensions for the extension-point '"
              + CPL_EXTENSION + "'. This is illegal.");
    }
    // Get the single extension element
    final Extension extension = extensions.iterator().next();
    final String lexerClassName = getExtensionClassName(extension, "lexer");
    // Get the parser class
    try {
      @SuppressWarnings("unchecked")
      final Class<Lexer> lexerClass = (Class<Lexer>) ExtensionHelper.class
          .getClassLoader().loadClass(lexerClassName);
      final Class[] parameters = {CharStream.class};
      return lexerClass.getConstructor(parameters).newInstance(
          new ANTLRFileStream(inputPath));
    } catch (final InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  public static Parser getParser(final PluginManager pluginManagerItf,
      final TokenStream tokens, final Map<Object, Object> context)
      throws ADLException {
    final Collection<Extension> extensions = pluginManagerItf.getExtensions(
        CPL_EXTENSION, context);
    if (extensions.size() == 0) {
      // Return the default lexer
      return new CPLParser(tokens);
    }
    if (extensions.size() > 1) {
      throw new ADLException(GenericErrors.GENERIC_ERROR,
          "There are more than one extensions for the extension-point '"
              + CPL_EXTENSION + "'. This is illegal.");
    }
    // Get the single extension element
    final Extension extension = extensions.iterator().next();
    final String parserClassName = getExtensionClassName(extension, "parser");
    // Get the parser class
    try {
      @SuppressWarnings("unchecked")
      final Class<Parser> parserClass = (Class<Parser>) ExtensionHelper.class
          .getClassLoader().loadClass(parserClassName);
      final Class[] parameters = {TokenStream.class};
      return parserClass.getConstructor(parameters).newInstance(tokens);
    } catch (final InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  public static String getExtensionClassName(final Extension extension,
      final String extensionName) throws ADLException {
    final NodeList nodes = ((Element) extension.astGetDecoration("xml-element"))
        .getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      final Node node = nodes.item(i);
      if (node instanceof Element) {
        final Element element = (Element) node;
        if (element.getNodeName().equals(extensionName)) {
          return element.getAttribute("class");
        }
      }
    }
    throw new ADLException(GenericErrors.GENERIC_ERROR, "No extension class '"
        + extensionName + "' found.");
  }
}
