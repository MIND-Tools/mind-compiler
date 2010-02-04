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
import static org.ow2.mind.adl.ast.ASTHelper.isComposite;
import static org.ow2.mind.adl.ast.ASTHelper.isPrimitive;
import static org.ow2.mind.adl.ast.ASTHelper.isType;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.DefinitionReference;

/**
 * Basic implementation of the {@link DefinitionReferenceResolver} interface.
 * This component simply call its {@link #loaderItf} client interface to load
 * the {@link Definition} whose name is the
 * {@link DefinitionReference#getName() name} contained by the reference. <br>
 * Moreover this component checks that the loaded definition has the
 * {@link DefinitionReference#getExpectedKind() expected kind}.
 */
public class BasicDefinitionReferenceResolver
    implements
      DefinitionReferenceResolver,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

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
    Definition d;
    try {
      d = loaderItf.load(reference.getName(), context);
    } catch (final ADLException e) {
      ChainedErrorLocator.chainLocator(e, reference);
      throw e;
    }

    // check that resolved definition matches expected kind (if any)
    final String expectedKind = reference.getExpectedKind();
    if (expectedKind != null) {
      if (DefinitionReference.TYPE_KIND.equals(expectedKind) && !isType(d))
        throw new ADLException(ADLErrors.INVALID_REFERENCE_NOT_A_TYPE,
            reference, reference.getName());
      if (DefinitionReference.PRIMITIVE_KIND.equals(expectedKind)
          && !isPrimitive(d))
        throw new ADLException(ADLErrors.INVALID_REFERENCE_NOT_A_PRIMITIVE,
            reference, reference.getName());
      if (DefinitionReference.COMPOSITE_KIND.equals(expectedKind)
          && !isComposite(d))
        throw new ADLException(ADLErrors.INVALID_REFERENCE_NOT_A_COMPOSITE,
            reference, reference.getName());
    }
    return d;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(LOADER_ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (LOADER_ITF_NAME.equals(s)) {
      return loaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (LOADER_ITF_NAME.equals(s)) {
      loaderItf = (Loader) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (LOADER_ITF_NAME.equals(s)) {
      loaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

}
