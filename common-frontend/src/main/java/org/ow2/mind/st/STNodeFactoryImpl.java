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

package org.ow2.mind.st;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeFactoryImpl;

/**
 * A {@link NodeFactory} component that uses {@link AbstractSTNode} as default
 * base class of generated node classes. So nodes created by this node factory
 * are directly suitable for StringTemplate and do not need to be transformed
 * using the {@link StringTemplateASTTransformer}.
 */
public class STNodeFactoryImpl extends NodeFactoryImpl {

  @Override
  public Node newNode(final String nodeType, final String... interfaces)
      throws ClassNotFoundException {
    return newNode(nodeType, AbstractSTNode.class, interfaces);
  }
}
