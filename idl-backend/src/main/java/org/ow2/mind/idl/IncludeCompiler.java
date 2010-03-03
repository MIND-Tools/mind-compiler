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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.ContextLocal;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.AbstractDelegatingVoidVisitor;
import org.ow2.mind.idl.ast.Header;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class IncludeCompiler extends AbstractDelegatingVoidVisitor<IDL>
    implements
      IDLVisitor {

  protected ContextLocal<Set<String>> contextualCompiledIDLs = new ContextLocal<Set<String>>();

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public IDLLoader                    idlLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    Set<String> compiledIDLs = contextualCompiledIDLs.get(context);
    if (compiledIDLs == null) {
      compiledIDLs = new HashSet<String>();
      contextualCompiledIDLs.set(context, compiledIDLs);
    }
    if (!compiledIDLs.add(idl.getName())) {
      // IDL already compiled
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
          visit(includedIDL, context);
        }
      }
    }
    // Compile references IDLs
    for (final InterfaceDefinition itf : IDLASTHelper.getReferencedInterfaces(
        idl, idlLoaderItf, context)) {
      visit(itf, context);
    }

  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = (IDLLoader) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(IDLLoader.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(IDLLoader.ITF_NAME)) {
      return idlLoaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
