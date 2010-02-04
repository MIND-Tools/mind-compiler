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

import static org.ow2.mind.PathHelper.isRelative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.idl.ast.Header;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.Include;

public final class IncludeDecorationHelper {
  private IncludeDecorationHelper() {
  }

  public static final String INCLUDED_IDL_DECORATION_NAME = "included-idl";

  public static void setIncludedIDL(final Include include, final IDL idl) {
    IncludedIDLDecoration decoration = getIncludedIDLDecoration(include);
    if (decoration == null) {
      decoration = new IncludedIDLDecoration(idl);
      include.astSetDecoration(INCLUDED_IDL_DECORATION_NAME, decoration);
    } else {
      decoration.setIDL(idl);
    }
  }

  public static IDL getIncludedIDL(final Include include,
      final IDLLoader loaderItf, final Map<Object, Object> context)
      throws ADLException {
    final IncludedIDLDecoration decoration = getIncludedIDLDecoration(include);
    if (decoration == null) return null;
    if (decoration.getIDL() == null && loaderItf != null) {
      decoration.setIDL(loaderItf.load(decoration.getPath(), context));
    }
    return decoration.getIDL();
  }

  private static IncludedIDLDecoration getIncludedIDLDecoration(
      final Include include) {
    return (IncludedIDLDecoration) include
        .astGetDecoration(INCLUDED_IDL_DECORATION_NAME);
  }

  public static final class IncludedIDLDecoration
      implements
        NodeContainerDecoration,
        Serializable {
    private transient IDL idl;
    private final String  path;

    public IncludedIDLDecoration(final IDL idl) {
      if (idl == null) throw new IllegalArgumentException("idl can't be null");
      if (idl.getName() == null)
        throw new IllegalArgumentException("idl's name can't be null");
      if (isRelative(idl.getName()) && !(idl instanceof Header))
        throw new IllegalArgumentException("idl's name can't be relative");

      this.idl = idl;
      this.path = idl.getName();
    }

    public IDL getIDL() {
      return idl;
    }

    public void setIDL(final IDL idl) {
      if (idl == null) throw new IllegalArgumentException("idl can't be null");
      if (idl.getName() == null)
        throw new IllegalArgumentException("idl's name can't be null");
      if (!idl.getName().equals(path))
        throw new IllegalArgumentException("Wrong idl path \"" + path
            + "\" expected instead of \"" + idl.getName() + "\".");

      this.idl = idl;
    }

    public String getPath() {
      return path;
    }

    // -------------------------------------------------------------------------
    // Implementation of the NodeContainerDecoration interface
    // -------------------------------------------------------------------------

    public Collection<Node> getNodes() {
      if (idl == null) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        l.add(idl);
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      if (replacements.containsKey(idl)) {
        idl = (IDL) replacements.get(idl);
      }
    }
  }
}
