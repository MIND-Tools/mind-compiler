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

import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
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

public class IDLTypeCheckerLoader extends AbstractIDLLoader {

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /**
   * The {@link InterfaceReferenceResolver} client interface used by this loader
   * to resolve extended interfaces.
   */
  public InterfaceReferenceResolver interfaceReferenceResolverItf;

  /**
   * The {@link IDLLocator} client interface used to locate IDL source files to
   * parse.
   */
  public IDLLocator                 idlLocatorItf;

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

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(InterfaceReferenceResolver.ITF_NAME)) {
      interfaceReferenceResolverItf = (InterfaceReferenceResolver) value;
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = (IDLLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), InterfaceReferenceResolver.ITF_NAME,
        IDLLocator.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(InterfaceReferenceResolver.ITF_NAME)) {
      return interfaceReferenceResolverItf;
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      return idlLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(InterfaceReferenceResolver.ITF_NAME)) {
      interfaceReferenceResolverItf = null;
    } else if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
