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

package org.ow2.mind.adl.generic;

import static org.ow2.mind.NameHelper.toValidName;
import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.io.IOErrors;
import org.ow2.mind.io.OutputFileLocator;

import com.google.inject.Inject;

public class GenericDefinitionNameSourceGenerator
    implements
      DefinitionSourceGenerator {

  public final static String  FILE_EXT = ".map";

  @Inject
  protected OutputFileLocator outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition input, final Map<Object, Object> context)
      throws ADLException {
    final String name = input.getName();
    final int i = name.indexOf('<');
    if (i == -1) return;
    final String templateName = name.substring(0, i);

    final File mapFile = outputFileLocatorItf.getMetadataOutputFile(
        fullyQualifiedNameToPath(templateName, FILE_EXT), context);
    final Properties map = new Properties();
    if (mapFile.exists()) {
      FileInputStream mapInStream = null;
      try {
        mapInStream = new FileInputStream(mapFile);
        map.load(mapInStream);
      } catch (final FileNotFoundException e) {
        // ignore
      } catch (final IOException e) {
        // ignore
      } finally {
        if (mapInStream != null) try {
          mapInStream.close();
        } catch (final IOException e) {
          // ignore
        }
      }
    }
    final String previousName = (String) map.put(toValidName(name), name);
    if (previousName != null && !previousName.equals(name)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Name clash in template instance name: \"" + name + "\" and \""
              + previousName + "\"");
    }
    FileOutputStream mapOutStream = null;
    try {
      mapOutStream = new FileOutputStream(mapFile);
      map.store(mapOutStream, "Template instance names");
    } catch (final FileNotFoundException e) {
      throw new CompilerError(IOErrors.WRITE_ERROR, e,
          mapFile.getAbsolutePath());
    } catch (final IOException e) {
      throw new CompilerError(IOErrors.WRITE_ERROR, e,
          mapFile.getAbsolutePath());
    } finally {
      if (mapOutStream != null)
        try {
          mapOutStream.close();
        } catch (final IOException e) {
          throw new CompilerError(IOErrors.WRITE_ERROR, e,
              mapFile.getAbsolutePath());
        }
    }
  }
}
