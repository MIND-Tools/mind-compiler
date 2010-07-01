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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class BasicInterfaceSignatureResolver
    implements
      InterfaceSignatureResolver,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager errorManagerItf;

  /** The {@link NodeFactory} client interface used by this component. */
  public NodeFactory  nodeFactoryItf;

  /** The Loader interface used to load referenced IDLs. */
  public IDLLoader    idlLoaderItf;

  // ---------------------------------------------------------------------------
  // Overridden InterfaceReferenceResolver methods
  // ---------------------------------------------------------------------------

  public InterfaceDefinition resolve(final TypeInterface itf,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    IDL itfDefinition;

    try {
      itfDefinition = idlLoaderItf.load(itf.getSignature(), context);
    } catch (final ADLException e) {
      errorManagerItf.logError(InterfaceErrors.INTERFACE_NOT_FOUND, itf,
          itf.getSignature());
      itfDefinition = IDLASTHelper.newUnresolvedIDLNode(nodeFactoryItf,
          itf.getSignature());
    }
    if (!(itfDefinition instanceof InterfaceDefinition)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Referenced IDL is not an interface definition");
    }
    return (InterfaceDefinition) itfDefinition;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = (ErrorManager) value;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = (IDLLoader) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME, NodeFactory.ITF_NAME,
        IDLLoader.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      return errorManagerItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      return idlLoaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
