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

package org.ow2.mind.adl.attribute;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractNormalizerLoader;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;

/**
 * Attribute normalizer.
 */
public class AttributesNormalizerLoader
    extends
      AbstractNormalizerLoader<Attribute> {

  // ---------------------------------------------------------------------------
  // Implementation of the abstract methods of AbstractNormalizerLoader
  // ---------------------------------------------------------------------------

  @Override
  protected Attribute[] getSubNodes(final Node node) {
    if (node instanceof AttributeContainer)
      return ((AttributeContainer) node).getAttributes();
    return null;
  }

  @Override
  protected Object getId(final Attribute node) {
    return node.getName();
  }

  @Override
  protected void handleNameClash(final Attribute previousDeclaration,
      final Attribute subNode) throws ADLException {
    throw new ADLException(ADLErrors.DUPLICATED_ATTRIBUTE_NAME, subNode
        .getName(), new NodeErrorLocator(previousDeclaration));
  }
}
