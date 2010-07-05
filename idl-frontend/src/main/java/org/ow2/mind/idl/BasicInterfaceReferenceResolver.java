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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class BasicInterfaceReferenceResolver
    implements
      InterfaceReferenceResolver,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager       errorManagerItf;

  /** The {@link NodeFactory} client interface used by this component. */
  public NodeFactory        nodeFactoryItf;

  /** The {@link RecursiveIDLLoader} interface used to load referenced IDLs. */
  public RecursiveIDLLoader recursiveIdlLoaderItf;

  // ---------------------------------------------------------------------------
  // Overridden InterfaceReferenceResolver methods
  // ---------------------------------------------------------------------------

  public InterfaceDefinition resolve(final String itfName,
      final IDL encapsulatingIDL, final Map<Object, Object> context)
      throws ADLException {
    IDL itf;

    try {
      itf = recursiveIdlLoaderItf.load(encapsulatingIDL, itfName, context);
    } catch (final ADLException e) {
      // Log an error only if the exception is IDL_NOT_FOUND
      if (e.getError().getTemplate() == IDLErrors.IDL_NOT_FOUND) {
        errorManagerItf.logError(IDLErrors.IDL_NOT_FOUND, encapsulatingIDL,
            itfName);
      }
      return IDLASTHelper.newUnresolvedInterfaceDefinitionNode(nodeFactoryItf,
          itfName);
    }
    if (!(itf instanceof InterfaceDefinition)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Referenced IDL is not an interface definition");
    }
    return (InterfaceDefinition) itf;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      errorManagerItf = (ErrorManager) value;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(RecursiveIDLLoader.ITF_NAME)) {
      recursiveIdlLoaderItf = (RecursiveIDLLoader) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME, NodeFactory.ITF_NAME,
        RecursiveIDLLoader.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      return errorManagerItf;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      return nodeFactoryItf;
    } else if (itfName.equals(RecursiveIDLLoader.ITF_NAME)) {
      return recursiveIdlLoaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (ErrorManager.ITF_NAME.equals(itfName)) {
      errorManagerItf = null;
    } else if (NodeFactory.ITF_NAME.equals(itfName)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(RecursiveIDLLoader.ITF_NAME)) {
      recursiveIdlLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
