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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.idl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.idl.ast.ArrayOf;
import org.ow2.mind.idl.ast.ConstantDefinition;
import org.ow2.mind.idl.ast.EnumDefinition;
import org.ow2.mind.idl.ast.EnumReference;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.PointerOf;
import org.ow2.mind.idl.ast.PrimitiveType;
import org.ow2.mind.idl.ast.StructDefinition;
import org.ow2.mind.idl.ast.StructReference;
import org.ow2.mind.idl.ast.TypeDefReference;
import org.ow2.mind.idl.ast.TypeDefinition;
import org.ow2.mind.idl.ast.UnionDefinition;
import org.ow2.mind.idl.ast.UnionReference;

public class KindDecorationLoader extends AbstractIDLLoader {

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);
    addDecorations(idl);
    return idl;
  }

  protected void addDecorations(final Node node) {
    if (node instanceof InterfaceDefinition)
      node.astSetDecoration("kind", "interface");
    else if (node instanceof EnumDefinition)
      node.astSetDecoration("kind", "enum");
    else if (node instanceof EnumReference)
      node.astSetDecoration("kind", "enumRef");
    else if (node instanceof StructDefinition)
      node.astSetDecoration("kind", "struct");
    else if (node instanceof StructReference)
      node.astSetDecoration("kind", "structRef");
    else if (node instanceof UnionDefinition)
      node.astSetDecoration("kind", "union");
    else if (node instanceof UnionReference)
      node.astSetDecoration("kind", "unionRef");
    else if (node instanceof TypeDefinition)
      node.astSetDecoration("kind", "typedef");
    else if (node instanceof TypeDefReference)
      node.astSetDecoration("kind", "typedefRef");
    else if (node instanceof PrimitiveType)
      node.astSetDecoration("kind", "primitiveType");
    else if (node instanceof ArrayOf)
      node.astSetDecoration("kind", "arrayOf");
    else if (node instanceof PointerOf)
      node.astSetDecoration("kind", "pointerOf");
    else if (node instanceof ConstantDefinition)
      node.astSetDecoration("kind", "constDef");

    for (final String type : node.astGetNodeTypes()) {
      for (final Node n : node.astGetNodes(type)) {
        if (n != null) addDecorations(n);
      }
    }
  }
}
