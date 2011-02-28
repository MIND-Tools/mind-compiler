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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLErrors;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.SharedTypeDefinition;
import org.ow2.mind.idl.jtb.ParseException;
import org.ow2.mind.idl.jtb.Parser;
import org.ow2.mind.idl.jtb.TokenMgrError;
import org.ow2.mind.idl.jtb.syntaxtree.IDTFile;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class IDLFileLoader implements IDLLoader {

  @Inject
  protected ErrorManager           errorManagerItf;

  @Inject
  protected XMLNodeFactory         nodeFactoryItf;

  @Inject
  protected IDLLocator             idlLocatorItf;

  @Inject
  protected Provider<JTBProcessor> processorProvider;

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
    final InputStream is;
    final String path;
    final Object registeredIDL = IDLParserContextHelper.getRegisteredIDL(name,
        context);
    if (registeredIDL != null) {
      if (registeredIDL instanceof InterfaceDefinition) {
        return (InterfaceDefinition) registeredIDL;
      } else if (registeredIDL instanceof String) {
        is = new ByteArrayInputStream(((String) registeredIDL).getBytes());
        path = "<generated>";
      } else {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR,
            "Unexpected type for registered ADL");
      }
    } else {
      final URL idlFile = locateItf(name, context);
      path = idlFile.getPath();
      try {
        is = idlFile.openStream();
      } catch (final IOException e) {
        errorManagerItf.logFatal(IDLErrors.IO_ERROR, e, idlFile.getPath());
        // never executed (logFatal throw an ADLException).
        return null;
      }
    }
    InterfaceDefinition itf;
    try {
      final Parser parser = new Parser(is);
      final JTBProcessor processor = processorProvider.get();
      processor.setFilename(path);
      final ITFFile content = parser.ITFFile();
      itf = processor.toInterfaceDefinition(content);
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(path,
          e.currentToken.beginLine, e.currentToken.beginColumn);
      errorManagerItf.logFatal(IDLErrors.PARSE_ERROR, locator, e.getMessage());
      // never executed (logFatal throw an ADLException).
      return null;
    } catch (final TokenMgrError e) {
      // TokenMgrError do not have location info.
      final ErrorLocator locator = new BasicErrorLocator(path, -1, -1);
      throw new ADLException(ADLErrors.PARSE_ERROR, locator, e.getMessage());
    }

    if (!name.equals(itf.getName())) {
      errorManagerItf.logError(IDLErrors.UNEXPECTED_ITF_NAME, itf,
          itf.getName(), name);
    }

    InputResourcesHelper.addInputResource(itf,
        idlLocatorItf.toInterfaceInputResource(name));

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

  protected SharedTypeDefinition loadSharedTypeDefinition(final String name,
      final Map<Object, Object> context) throws ADLException {
    final InputStream is;
    final String path;
    final Object registeredIDL = IDLParserContextHelper.getRegisteredIDL(name,
        context);
    if (registeredIDL != null) {
      if (registeredIDL instanceof SharedTypeDefinition) {
        return (SharedTypeDefinition) registeredIDL;
      } else if (registeredIDL instanceof String) {
        is = new ByteArrayInputStream(((String) registeredIDL).getBytes());
        path = "<generated>";
      } else {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR,
            "Unexpected type for registered ADL");
      }
    } else {
      final URL idtFile = locateIdt(name, context);
      path = idtFile.getPath();
      try {
        is = idtFile.openStream();
      } catch (final IOException e) {
        errorManagerItf.logFatal(IDLErrors.IO_ERROR, e, path);
        // never executed (logFatal throw an ADLException).
        return null;
      }
    }

    SharedTypeDefinition idt;

    try {
      final Parser parser = new Parser(is);
      final JTBProcessor processor = processorProvider.get();
      processor.setFilename(path);
      final IDTFile content = parser.IDTFile();
      idt = processor.toSharedTypeDefinition(content);
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(path,
          e.currentToken.beginLine, e.currentToken.beginColumn);
      errorManagerItf.logFatal(IDLErrors.PARSE_ERROR, locator, e.getMessage());
      // never executed (logFatal throw an ADLException).
      return null;
    }

    idt.setName(name);

    InputResourcesHelper.addInputResource(idt,
        idlLocatorItf.toSharedTypeInputResource(name));

    return idt;

  }

  protected URL locateIdt(final String name, final Map<Object, Object> context)
      throws ADLException {
    final URL srcFile = idlLocatorItf.findSourceHeader(name, context);
    if (srcFile == null) {
      throw new ADLException(IDLErrors.IDL_NOT_FOUND, name);
    }
    return srcFile;
  }
}
