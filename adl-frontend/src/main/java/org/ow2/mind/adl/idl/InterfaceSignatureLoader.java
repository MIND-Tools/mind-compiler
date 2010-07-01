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

package org.ow2.mind.adl.idl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper.setResolvedInterfaceDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class InterfaceSignatureLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager               errorManagerItf;

  /** The {@link NodeFactory} client interface used by this component. */
  public NodeFactory                nodeFactoryItf;

  /** The client interface used to resolve signature of component interfaces. */
  public InterfaceSignatureResolver interfaceSignatureResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition def = clientLoader.load(name, context);

    if (def instanceof InterfaceContainer) {
      for (final Interface itf : ((InterfaceContainer) def).getInterfaces()) {
        if (itf instanceof TypeInterface) {
          processItf(def, (TypeInterface) itf, def, context);
        }
      }
    }

    return def;
  }

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected void processItf(final Definition def, final TypeInterface itf,
      final Definition container, final Map<Object, Object> context)
      throws ADLException {
    InterfaceDefinition itfDef;
    try {
      itfDef = interfaceSignatureResolverItf.resolve(itf, container, context);
    } catch (final ADLException e) {
      errorManagerItf.logError(InterfaceErrors.INTERFACE_NOT_FOUND, itf,
          itf.getSignature());
      itfDef = IDLASTHelper.newUnresolvedInterfaceDefinitionNode(
          nodeFactoryItf, itf.getSignature());
    }

    setResolvedInterfaceDefinition(itf, itfDef);
    InputResourcesHelper.addInputResources(def,
        InputResourcesHelper.getInputResources(itfDef));
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      errorManagerItf = (ErrorManager) value;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (InterfaceSignatureResolver.ITF_NAME.equals(itfName)) {
      interfaceSignatureResolverItf = (InterfaceSignatureResolver) value;
    } else {
      super.bindFc(itfName, value);
    }
  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), InterfaceSignatureResolver.ITF_NAME,
        ErrorManager.ITF_NAME, NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      return errorManagerItf;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      return nodeFactoryItf;
    } else if (InterfaceSignatureResolver.ITF_NAME.equals(itfName)) {
      return interfaceSignatureResolverItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      errorManagerItf = null;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      nodeFactoryItf = null;
    } else if (InterfaceSignatureResolver.ITF_NAME.equals(itfName)) {
      interfaceSignatureResolverItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
