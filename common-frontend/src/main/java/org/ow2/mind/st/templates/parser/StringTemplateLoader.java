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

package org.ow2.mind.st.templates.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.st.templates.ast.TemplateComponent;
import org.ow2.mind.st.templates.jtb.ParseException;
import org.ow2.mind.st.templates.jtb.Parser;
import org.ow2.mind.st.templates.jtb.syntaxtree.TemplateComponentDefinition;

import com.google.inject.Inject;

public class StringTemplateLoader implements Loader {

  protected static final String DTD                = "classpath://org/ow2/mind/st/xml/template-component.dtd";

  public static final String    TEMPLATE_EXTENSION = ".stc";

  @Inject
  public XMLNodeFactory         nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final URL templateFile = locateTemplateComponent(name, context);

    TemplateComponent tc;
    try {
      tc = readTemplate(templateFile);
    } catch (final IOException e) {
      throw new CompilerError(ADLErrors.IO_ERROR, e, templateFile.getPath());
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(
          templateFile.getPath(), e.currentToken.beginLine,
          e.currentToken.beginColumn);
      throw new CompilerError(ADLErrors.PARSE_ERROR, locator, e.getMessage());
    }

    if (!tc.getName().equals(name)) {
      throw new CompilerError(ADLErrors.WRONG_DEFINITION_NAME,
          new NodeErrorLocator(tc), name, tc.getName());
    }

    String content = tc.getContent();
    content = content.substring(content.indexOf("$${") + 3);
    content = content.substring(0, content.indexOf("}$$"));
    final String groupLine = "group "
        + tc.getName().substring(tc.getName().lastIndexOf('.') + 1) + ";\n";
    tc.setContent(groupLine + content);
    return tc;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected URL locateTemplateComponent(final String name,
      final Map<Object, Object> context) {
    final String templateFileName = name.replace('.', '/') + TEMPLATE_EXTENSION;
    final URL templateURL = ClassLoaderHelper.getClassLoader(this, context)
        .getResource(templateFileName);
    if (templateURL == null) {
      throw new CompilerError(ADLErrors.IO_ERROR, templateFileName);
    }
    return templateURL;

  }

  protected TemplateComponent readTemplate(final URL srcFile)
      throws IOException, ParseException {
    final InputStream is = srcFile.openStream();
    final Parser parser = new Parser(is);
    final JTBProcessor processor = new JTBProcessor(nodeFactoryItf, DTD,
        srcFile.getPath());
    final TemplateComponentDefinition content = parser
        .TemplateComponentDefinition();
    return processor.toTemplateComponent(content);
  }
}
