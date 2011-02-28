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

package org.ow2.mind.target;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ParserException;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.adl.xml.XMLParser;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.target.ast.Target;
import org.xml.sax.SAXParseException;

import com.google.inject.Inject;

public class BasicTargetDescriptorLoader implements TargetDescriptorLoader {

  @Inject
  protected ErrorManager   errorManager;

  @Inject
  protected XMLNodeFactory nodeFactoryItf;

  // --------------------------------------------------------------------------
  // Implementation of the Loader interface
  // --------------------------------------------------------------------------

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final XMLParser parser = new XMLParser(nodeFactoryItf);
    final String file = name.replace('.', '/') + ".td";
    ClassLoader cl = (ClassLoader) context.get("classloader");
    if (cl == null) cl = this.getClass().getClassLoader();
    final URL url = cl.getResource(file);
    if (url == null) {
      errorManager.logFatal(TargetDescErrors.TARGET_DESC_NOT_FOUND_FATAL, name);
      return null;
    }
    final Target target;
    try {
      target = (Target) parser.parse(url.openStream(), file);
    } catch (final IOException e) {
      errorManager.logFatal(TargetDescErrors.PARSE_ERROR_FATAL, e,
          e.getMessage());
      return null;
    } catch (final ParserException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof SAXParseException) {
        final SAXParseException parseException = (SAXParseException) cause;
        final ErrorLocator locator = new BasicErrorLocator(url.getPath(),
            parseException.getLineNumber(), parseException.getColumnNumber());
        errorManager.logFatal(TargetDescErrors.PARSE_ERROR_FATAL, locator,
            name, parseException.getMessage());
      } else {
        errorManager.logFatal(TargetDescErrors.PARSE_ERROR_FATAL, name,
            e.getMessage());
      }
      return null;
    }
    if (!target.getName().equals(name)) {
      errorManager.logError(TargetDescErrors.INVALID_NAME, target,
          target.getName(), name);
    }
    return target;

  }
}
