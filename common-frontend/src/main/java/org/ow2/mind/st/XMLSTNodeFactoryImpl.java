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

import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.xml.XMLNode;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.xml.sax.SAXException;

/**
 * A {@link NodeFactory} component that uses {@link AbstractXMLSTNode} as
 * default base class of generated node classes. So nodes created by this node
 * factory are suitable for StringTemplate.
 */
public class XMLSTNodeFactoryImpl extends XMLNodeFactoryImpl {

  @Override
  public XMLNode newXMLNode(final String systemId, final String qualifiedName)
      throws SAXException {
    return newXMLNode(systemId, qualifiedName, AbstractXMLSTNode.class);
  }
}
