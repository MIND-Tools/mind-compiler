/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.value;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.PathHelper;
import org.ow2.mind.value.ValueEvaluator.AbstractDelegatingValueEvaluator;
import org.ow2.mind.value.ast.PathLiteral;
import org.ow2.mind.value.ast.Value;

public class PathValueEvaluator extends AbstractDelegatingValueEvaluator {

  public <T> T evaluate(final Value value, final Class<T> expectedType,
      final Map<Object, Object> context) throws ValueEvaluationException {
    if (value instanceof PathLiteral) {
      final String path = ((PathLiteral) value).getValue();
      if (expectedType.isInstance(new String())) {
        return expectedType.cast(path);
      } else {
        URL url;
        if (!PathHelper.isValid(path)) {
          throw new ValueEvaluationException("Invalid path '" + path + "'.",
              value);
        }
        if (PathHelper.isRelative(path)) {
          throw new ValueEvaluationException("Invalid path '" + path
              + "'. Path must be absolute", value);
        }
        url = ClassLoaderHelper.getClassLoader(this, context).getResource(
            path.substring(1));
        if (url == null) {
          throw new ValueEvaluationException("Unexisting path value '" + path
              + "' found in annotation.", value);
        }
        if (expectedType == URL.class) {
          return expectedType.cast(url);
        } else if (expectedType == File.class) {
          if (!url.getProtocol().equals("file")) {
            throw new ValueEvaluationException(
                "Path convertion impossible. URL:" + url
                    + " does not designate a local file", value);
          }
          return expectedType.cast(new File(url.getPath()));
        } else {
          throw new ValueEvaluationException(
              "Incompatible value type, found a Path where "
                  + expectedType.getName()
                  + " was expected. Compatible types for a PathLiteral are String, URL or File.",
              value);
        }
      }
    } else {
      return clientValueEvaluatorItf.evaluate(value, expectedType, context);
    }
  }

}
