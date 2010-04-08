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
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl.parser;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.ByteArrayInputStream;
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
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.jtb.ParseException;
import org.ow2.mind.adl.jtb.Parser;
import org.ow2.mind.adl.jtb.syntaxtree.ADLFile;

/**
 * Parser the ADL source file located by the {@link #adlLocatorItf}.
 */
public class ADLParser implements Loader, BindingController {

  protected static final String DTD = "classpath://org/ow2/mind/adl/mind_v1.dtd";

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /**
   * Client interface bound to the {@link XMLNodeFactory node factory}
   * component.
   */
  public XMLNodeFactory         nodeFactoryItf;

  /**
   * The {@link ADLLocator} client interface used to locate ADL source files to
   * parse.
   */
  public ADLLocator             adlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final String path;
    final InputStream is;

    final Object registeredADL = ADLParserContextHelper.getRegisteredADL(name,
        context);
    if (registeredADL != null) {
      if (registeredADL instanceof Definition) {
        return (Definition) registeredADL;
      } else if (registeredADL instanceof String) {
        is = new ByteArrayInputStream(((String) registeredADL).getBytes());
        path = "<generated>";
      } else {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR,
            "Unexpected type for registered ADL");
      }
    } else {
      final URL adlFile = locateADL(name, context);
      path = adlFile.getPath();
      try {
        is = adlFile.openStream();
      } catch (final IOException e) {
        throw new ADLException(ADLErrors.IO_ERROR, e, path);
      }
    }

    Definition d;
    try {
      d = readADL(is, path);
    } catch (final IOException e) {
      throw new ADLException(ADLErrors.IO_ERROR, e, path);
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(path,
          e.currentToken.next.beginLine, e.currentToken.next.endLine,
          e.currentToken.next.beginColumn, e.currentToken.next.endColumn);
      throw new ADLException(ADLErrors.PARSE_ERROR, locator, e.getMessage());
    }

    InputResourcesHelper.addInputResource(d, adlLocatorItf
        .toInputResource(name));

    return d;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected URL locateADL(final String name, final Map<Object, Object> context)
      throws ADLException {
    final URL srcFile = adlLocatorItf.findSourceADL(name, context);
    if (srcFile == null) {
      throw new ADLException(ADLErrors.ADL_NOT_FOUND, name);
    }
    return srcFile;
  }

  protected Definition readADL(final InputStream is, final String fileName)
      throws IOException, ParseException, ADLException {
    final Parser parser = new Parser(is);
    final JTBProcessor processor = new JTBProcessor(nodeFactoryItf, DTD,
        fileName);
    final ADLFile content = parser.ADLFile();
    return processor.toDefinition(content);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      this.nodeFactoryItf = (XMLNodeFactory) value;
    } else if (itfName.equals(ADLLocator.ITF_NAME)) {
      this.adlLocatorItf = (ADLLocator) value;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(XMLNodeFactory.ITF_NAME, ADLLocator.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      return this.nodeFactoryItf;
    } else if (itfName.equals(ADLLocator.ITF_NAME)) {
      return this.adlLocatorItf;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      this.nodeFactoryItf = null;
    } else if (itfName.equals(ADLLocator.ITF_NAME)) {
      this.adlLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }
}
