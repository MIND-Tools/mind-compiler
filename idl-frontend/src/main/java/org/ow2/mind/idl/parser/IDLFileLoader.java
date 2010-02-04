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

package org.ow2.mind.idl.parser;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.idl.IDLErrors;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.SharedTypeDefinition;
import org.ow2.mind.idl.jtb.ParseException;
import org.ow2.mind.idl.jtb.Parser;
import org.ow2.mind.idl.jtb.syntaxtree.IDTFile;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;

public class IDLFileLoader implements IDLLoader, BindingController {

  protected static final String DTD = "classpath://org/ow2/mind/idl/mind_v1.dtd";

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /**
   * Client interface bound to the {@link XMLNodeFactory node factory}
   * component.
   */
  public XMLNodeFactory         nodeFactoryItf;

  /**
   * The {@link IDLLocator} client interface used to locate IDL source files to
   * parse.
   */
  public IDLLocator             idlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    if (name.startsWith("/"))
      return loadSharedTypeDefinition(name, context);
    else
      return loadInterfaceDefinition(name, context);
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected InterfaceDefinition loadInterfaceDefinition(final String name,
      final Map<Object, Object> context) throws ADLException {
    final URL idlFile = locateItf(name, context);

    InterfaceDefinition itf;
    try {
      itf = readItf(idlFile);
    } catch (final IOException e) {
      throw new ADLException(IDLErrors.IO_ERROR, e, idlFile.getPath());
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(idlFile.getPath(),
          e.currentToken.beginLine, e.currentToken.beginColumn);
      throw new ADLException(IDLErrors.PARSE_ERROR, locator, e.getMessage());
    }

    if (!name.equals(itf.getName())) {
      throw new ADLException(IDLErrors.UNEXPECTED_ITF_NAME, itf, itf.getName(),
          name);
    }

    InputResourcesHelper.addInputResource(itf, idlLocatorItf
        .toInterfaceInputResource(name));

    return itf;
  }

  protected URL locateItf(final String name, final Map<Object, Object> context)
      throws ADLException {
    final URL srcFile = idlLocatorItf.findSourceItf(name, context);
    if (srcFile == null) {
      throw new ADLException(IDLErrors.IDL_NOT_FOUND, name);
    }
    return srcFile;
  }

  protected InterfaceDefinition readItf(final URL srcFile) throws IOException,
      ParseException {
    final InputStream is = srcFile.openStream();
    final Parser parser = new Parser(is);
    final JTBProcessor processor = new JTBProcessor(nodeFactoryItf, DTD,
        srcFile.getPath());
    final ITFFile content = parser.ITFFile();
    return processor.toInterfaceDefinition(content);
  }

  protected SharedTypeDefinition loadSharedTypeDefinition(final String name,
      final Map<Object, Object> context) throws ADLException {
    final URL idtFile = locateIdt(name, context);

    SharedTypeDefinition idt;

    try {
      idt = readIdt(idtFile);
    } catch (final IOException e) {
      throw new ADLException(IDLErrors.IO_ERROR, e, idtFile.getPath());
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(idtFile.getPath(),
          e.currentToken.beginLine, e.currentToken.beginColumn);
      throw new ADLException(IDLErrors.PARSE_ERROR, locator, e.getMessage());
    }

    idt.setName(name);

    InputResourcesHelper.addInputResource(idt, idlLocatorItf
        .toSharedTypeInputResource(name));

    return idt;

  }

  protected URL locateIdt(final String name, final Map<Object, Object> context)
      throws ADLException {
    final URL srcFile = idlLocatorItf.findHeader(name, context);
    if (srcFile == null) {
      throw new ADLException(IDLErrors.IDL_NOT_FOUND, name);
    }
    return srcFile;
  }

  protected SharedTypeDefinition readIdt(final URL srcFile) throws IOException,
      ParseException {
    final InputStream is = srcFile.openStream();
    final Parser parser = new Parser(is);
    final JTBProcessor processor = new JTBProcessor(nodeFactoryItf, DTD,
        srcFile.getPath());
    final IDTFile content = parser.IDTFile();
    return processor.toSharedTypeDefinition(content);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      this.nodeFactoryItf = (XMLNodeFactory) value;
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      this.idlLocatorItf = (IDLLocator) value;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(XMLNodeFactory.ITF_NAME, IDLLocator.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(XMLNodeFactory.ITF_NAME)) {
      return this.nodeFactoryItf;
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      return this.idlLocatorItf;
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
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      this.idlLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }
}
