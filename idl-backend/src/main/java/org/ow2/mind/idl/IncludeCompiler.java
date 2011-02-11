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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.idl.ast.Header;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.inject.InjectDelegate;

import com.google.inject.Inject;

public class IncludeCompiler implements IDLVisitor {

  @Inject
  protected IDLLoader  idlLoaderItf;

  @InjectDelegate
  protected IDLVisitor clientVisitorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    visit(idl, new HashSet<String>(), context);
  }

  protected void visit(final IDL idl, final Set<String> visitedIDLS,
      final Map<Object, Object> context) throws ADLException {

    if (!visitedIDLS.add(idl.getName())) {
      // IDL already visited.
      return;
    }

    // First ensure that referenced IDL are actually loaded :
    if (idl instanceof IncludeContainer) {
      for (final Include include : ((IncludeContainer) idl).getIncludes()) {
        IDLASTHelper.getIncludedIDL(include, idlLoaderItf, context);
      }
    }
    IDLASTHelper.getReferencedInterfaces(idl, idlLoaderItf, context);

    // Compile IDL
    clientVisitorItf.visit(idl, context);

    // Compile included IDLs
    if (idl instanceof IncludeContainer) {
      for (final Include include : ((IncludeContainer) idl).getIncludes()) {
        final IDL includedIDL = IDLASTHelper.getIncludedIDL(include,
            idlLoaderItf, context);
        if (!(includedIDL instanceof Header)) {
          visit(includedIDL, visitedIDLS, context);
        }
      }
    }
    // Compile references IDLs
    for (final InterfaceDefinition itf : IDLASTHelper.getReferencedInterfaces(
        idl, idlLoaderItf, context)) {
      visit(itf, visitedIDLS, context);
    }

  }
}
