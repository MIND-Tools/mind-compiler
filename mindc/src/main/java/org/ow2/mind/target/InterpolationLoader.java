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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.target.TargetDescriptorLoader.AbstractDelegatingTargetDescriptorLoader;
import org.ow2.mind.target.ast.Target;

public class InterpolationLoader
    extends
      AbstractDelegatingTargetDescriptorLoader {

  private static Logger logger = FractalADLLogManager.getLogger("targetDesc");

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Target target = clientLoader.load(name, context);
    interpolate(target);
    return target;
  }

  protected void interpolate(final Node node) {
    final Map<String, String> attributes = node.astGetAttributes();
    for (final Map.Entry<String, String> attribute : attributes.entrySet()) {
      if (attribute.getValue() != null)
        attribute.setValue(interpolate(node, attribute.getValue()));
    }
    node.astSetAttributes(attributes);

    for (final String subNodeType : node.astGetNodeTypes()) {
      for (final Node subNode : node.astGetNodes(subNodeType)) {
        if (subNode != null) interpolate(subNode);
      }
    }
  }

  protected String interpolate(final Node node, final String s) {
    final int i = s.indexOf("${");
    if (i == -1) return s;

    final int j = s.indexOf("}", i);
    if (j == -1) {
      if (logger.isLoggable(Level.WARNING))
        logger.log(Level.WARNING, "At " + node.astGetSource()
            + ": Invalid string \"" + s + "\"");
      return s;
    }
    final String varName = s.substring(i + 2, j);
    if (varName.equals("inputADL")) {
      // Do not substitute ${inputADL} in ADLMapping
      return s;
    }
    String value = System.getProperty(varName);
    if (value == null) value = System.getenv(varName);

    if (value == null) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "At " + node.astGetSource()
            + ": Unknown variable \"" + varName + "\"");
      value = "";
    } else if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "At " + node.astGetSource() + ": replace \""
          + varName + "\" by \"" + value + "\".");
    }

    return interpolate(node, s.substring(0, i) + value + s.substring(j + 1));
  }
}
