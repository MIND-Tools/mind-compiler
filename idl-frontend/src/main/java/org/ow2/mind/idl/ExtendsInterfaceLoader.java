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

import static org.objectweb.fractal.adl.NodeUtil.cloneGraph;
import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class ExtendsInterfaceLoader extends AbstractIDLLoader {

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /**
   * The {@link InterfaceReferenceResolver} client interface used by this loader
   * to resolve extended interfaces.
   */
  public InterfaceReferenceResolver interfaceReferenceResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);

    if (idl instanceof InterfaceDefinition) {
      final InterfaceDefinition itf = (InterfaceDefinition) idl;
      if (itf.getExtends() != null) {
        final InterfaceDefinition extendedItf = interfaceReferenceResolverItf
            .resolve(itf.getExtends(), itf, context);

        mergeInterface(itf, extendedItf);
        ExtendedInterfaceDecorationHelper
            .addExtendedInterface(itf, extendedItf);
      }
    }

    return idl;
  }

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected void mergeInterface(final InterfaceDefinition itf,
      final InterfaceDefinition superItf) {
    for (final Method method : superItf.getMethods()) {
      itf.addMethod(cloneGraph(method));
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
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), InterfaceReferenceResolver.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(InterfaceReferenceResolver.ITF_NAME)) {
      return interfaceReferenceResolverItf;
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
    } else {
      super.unbindFc(itfName);
    }
  }
}
