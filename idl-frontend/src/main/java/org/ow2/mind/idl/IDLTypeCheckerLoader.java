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

import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader.AbstractDelegatingIDLLoader;
import org.ow2.mind.idl.ast.ArrayOf;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Member;
import org.ow2.mind.idl.ast.MemberContainer;
import org.ow2.mind.idl.ast.Method;
import org.ow2.mind.idl.ast.Parameter;
import org.ow2.mind.idl.ast.PointerOf;
import org.ow2.mind.idl.ast.Type;
import org.ow2.mind.idl.ast.TypeCollectionContainer;
import org.ow2.mind.idl.ast.TypeContainer;
import org.ow2.mind.idl.ast.TypeDefReference;
import org.ow2.mind.idl.ast.TypeDefinition;

import com.google.inject.Inject;

public class IDLTypeCheckerLoader extends AbstractDelegatingIDLLoader {

  @Inject
  protected ErrorManager               errorManagerItf;

  @Inject
  protected InterfaceReferenceResolver interfaceReferenceResolverItf;

  @Inject
  protected IDLLocator                 idlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLocator interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);
    checkTypes(idl, context);
    return idl;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected void checkTypes(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    // check types defines at top-level
    if (idl instanceof TypeCollectionContainer) {
      for (final Type type : ((TypeCollectionContainer) idl).getTypes()) {
        checkType(idl, type, context);
      }
    }

    // check types in method
    if (idl instanceof InterfaceDefinition) {
      for (final Method method : ((InterfaceDefinition) idl).getMethods()) {
        // check return type
        checkType(idl, method.getType(), context);

        // check params
        for (final Parameter parameter : method.getParameters()) {
          checkType(idl, parameter.getType(), context);
        }
      }
    }
  }

  protected void checkType(final IDL idl, final Type type,
      final Map<Object, Object> context) throws ADLException {
    if (type instanceof TypeDefReference) {
      final URL itf = idlLocatorItf.findSourceItf(
          ((TypeDefReference) type).getName(), context);
      if (itf != null) {
        interfaceReferenceResolverItf.resolve(
            ((TypeDefReference) type).getName(), idl, context);
      }
    } else if (type instanceof MemberContainer) {
      for (final Member member : ((MemberContainer) type).getMembers()) {
        checkType(idl, member.getType(), context);
      }
    } else if (type instanceof TypeDefinition || type instanceof ArrayOf
        || type instanceof PointerOf) {
      checkType(idl, ((TypeContainer) type).getType(), context);
    }
  }
}
