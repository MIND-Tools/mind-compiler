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
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.jtb.ParseException;
import org.ow2.mind.adl.jtb.Parser;
import org.ow2.mind.adl.jtb.TokenMgrError;
import org.ow2.mind.adl.jtb.syntaxtree.ADLFile;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Parser the ADL source file located by the {@link #adlLocatorItf}.
 */
public class ADLParser implements Loader {

  @Inject
  protected ErrorManager           errorManagerItf;

  @Inject
  protected XMLNodeFactory         nodeFactoryItf;

  @Inject
  protected ADLLocator             adlLocatorItf;

  @Inject
  protected Provider<JTBProcessor> processorProvider;

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
        path = "<generated:" + name + ">";
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
        errorManagerItf.logFatal(ADLErrors.IO_ERROR, e, path);
        // never executed (logFatal throws an ADLException).
        return null;
      }
    }

    Definition d;
    try {
      d = readADL(is, path);
    } catch (final IOException e) {
      errorManagerItf.logFatal(ADLErrors.IO_ERROR, e, path);
      // never executed (logFatal throw an ADLException).
      return null;
    } catch (final ParseException e) {
      final ErrorLocator locator = new BasicErrorLocator(path,
          e.currentToken.next.beginLine, e.currentToken.next.endLine,
          e.currentToken.next.beginColumn, e.currentToken.next.endColumn);
      errorManagerItf.logFatal(ADLErrors.PARSE_ERROR, locator, e.getMessage());
      // never executed (logFatal throw an ADLException).
      return null;
    } catch (final TokenMgrError e) {
      // TokenMgrError do not have location info.
      final ErrorLocator locator = new BasicErrorLocator(path, -1, -1);
      errorManagerItf.logFatal(ADLErrors.PARSE_ERROR, locator, e.getMessage());
      // never executed (logFatal throw an ADLException).
      return null;
    }

    InputResourcesHelper.addInputResource(d,
        adlLocatorItf.toInputResource(name));

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
    final ADLFile content = parser.ADLFile();

    final JTBProcessor processor = processorProvider.get();
    processor.setFilename(fileName);
    return processor.toDefinition(content);
  }
}
