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

package org.ow2.mind.adl.parameter;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractNormalizerLoader;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;

public class ParameterNormalizerLoader
    extends
      AbstractNormalizerLoader<FormalParameter> {

  @Override
  protected void handleNameClash(final FormalParameter previousDeclaration,
      final FormalParameter subNode) throws ADLException {
    throw new ADLException(ADLErrors.DUPLICATED_ARGUMENT_VARIABLE_NAME,
        subNode, subNode.getName());
  }

  @Override
  protected FormalParameter[] getSubNodes(final Node node) {
    if (node instanceof FormalParameterContainer) {
      return ((FormalParameterContainer) node).getFormalParameters();
    } else {
      return null;
    }
  }

  @Override
  protected Object getId(final FormalParameter node) throws ADLException {
    return node.getName();
  }
}
