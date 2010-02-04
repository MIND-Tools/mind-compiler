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

package org.ow2.mind.idl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.Node;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class ReferencedInterfacesDecorationHelper {

  public static final String REFERENCED_INTERFACE_DECORATION = "referenced-interfaces";

  public static void addReferencedInterface(final IDL idl,
      final InterfaceDefinition itf) {
    getDecoration(idl).addReferencedInterface(idl, itf);
  }

  public static Collection<InterfaceDefinition> getReferencedInterfaces(
      final IDL idl) {
    return getDecoration(idl).getReferencedInterfaces();
  }

  private static ReferencedInterfaceDecoration getDecoration(final IDL idl) {
    ReferencedInterfaceDecoration decoration = (ReferencedInterfaceDecoration) idl
        .astGetDecoration(REFERENCED_INTERFACE_DECORATION);
    if (decoration == null) {
      decoration = new ReferencedInterfaceDecoration();
      idl.astSetDecoration(REFERENCED_INTERFACE_DECORATION, decoration);
    }
    return decoration;
  }

  public static class ReferencedInterfaceDecoration
      implements
        NodeContainerDecoration,
        Serializable {

    Map<String, InterfaceDefinition> itfs = new HashMap<String, InterfaceDefinition>();

    public void addReferencedInterface(final IDL idl,
        final InterfaceDefinition itf) {
      if (!itfs.containsKey(itf.getName())) {
        itfs.put(itf.getName(), itf);
      }
    }

    public Collection<InterfaceDefinition> getReferencedInterfaces() {
      return itfs.values();
    }

    public Collection<Node> getNodes() {
      if (itfs.isEmpty()) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        for (final InterfaceDefinition itfDef : itfs.values()) {
          l.add(itfDef);
        }
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      for (final Map.Entry<Node, Node> replacement : replacements.entrySet()) {
        itfs.put(((InterfaceDefinition) replacement.getKey()).getName(),
            (InterfaceDefinition) replacement.getValue());
      }
    }
  }

}
