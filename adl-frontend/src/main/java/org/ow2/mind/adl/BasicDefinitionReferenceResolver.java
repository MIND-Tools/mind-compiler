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

package org.ow2.mind.adl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.error.ErrorManager;

/**
 * Basic implementation of the {@link DefinitionReferenceResolver} interface.
 * This component simply call its {@link #loaderItf} client interface to load
 * the {@link Definition} whose name is the
 * {@link DefinitionReference#getName() name} contained by the reference.
 */
public class BasicDefinitionReferenceResolver
    implements
      DefinitionReferenceResolver,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager        errorManagerItf;

  /** The {@link NodeFactory} client interface used by this component. */
  public NodeFactory         nodeFactoryItf;

  /** The name of the {@link #loaderItf} client interface. */
  public static final String LOADER_ITF_NAME = "loader";

  /** The Loader interface used to load referenced definitions. */
  public Loader              loaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    // load referenced ADL
    final Definition d;
    try {
      d = loaderItf.load(reference.getName(), context);
    } catch (final ADLException e) {
      // Log an error only if the exception is ADL_NOT_FOUND
      if (e.getError().getTemplate() == ADLErrors.ADL_NOT_FOUND) {
        errorManagerItf.logError(ADLErrors.ADL_NOT_FOUND, reference,
            reference.getName());
      }
      return ASTHelper.newUnresolvedDefinitionNode(nodeFactoryItf,
          reference.getName());
    }

    return d;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME, LOADER_ITF_NAME,
        NodeFactory.ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      return errorManagerItf;
    } else if (LOADER_ITF_NAME.equals(s)) {
      return loaderItf;
    } else if (NodeFactory.ITF_NAME.equals(s)) {
      return nodeFactoryItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      errorManagerItf = (ErrorManager) o;
    } else if (LOADER_ITF_NAME.equals(s)) {
      loaderItf = (Loader) o;
    } else if (NodeFactory.ITF_NAME.equals(s)) {
      nodeFactoryItf = (NodeFactory) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (ErrorManager.ITF_NAME.equals(s)) {
      errorManagerItf = null;
    } else if (LOADER_ITF_NAME.equals(s)) {
      loaderItf = null;
    } else if (NodeFactory.ITF_NAME.equals(s)) {
      nodeFactoryItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

}
