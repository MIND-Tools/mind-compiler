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

package org.ow2.mind.adl.interfaces;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;

public class CollectionInterfaceDefinitionSourceGenerator
    implements
      DefinitionSourceGenerator,
      BindingController {

  public static final String       INDEXES_DECORATION_NAME          = "collectionIndexes";

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------
  public static final String       CLIENT_SOURCE_GENERATOR_ITF_NAME = "client-source-generator";
  /** The {@link DefinitionSourceGenerator} client interface. */
  public DefinitionSourceGenerator clientSourceGeneratorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {

    if (definition instanceof InterfaceContainer) {
      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        final int noe = ASTHelper.getNumberOfElement(itf);
        if (noe == -1) continue;
        addIndexesDecoration(itf, noe);
      }
    }

    if (definition instanceof InternalInterfaceContainer) {
      for (final Interface itf : ((InternalInterfaceContainer) definition)
          .getInternalInterfaces()) {
        final int noe = ASTHelper.getNumberOfElement(itf);
        if (noe == -1) continue;
        addIndexesDecoration(itf, noe);
      }
    }

    clientSourceGeneratorItf.visit(definition, context);
  }

  protected Integer[] addIndexesDecoration(final Interface itf,
      final int numberOfElement) {
    final Integer[] indexes = new Integer[numberOfElement];
    for (int i = 0; i < numberOfElement; i++) {
      indexes[i] = i;
    }
    itf.astSetDecoration(INDEXES_DECORATION_NAME, indexes);
    return indexes;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_SOURCE_GENERATOR_ITF_NAME)) {
      clientSourceGeneratorItf = (DefinitionSourceGenerator) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(CLIENT_SOURCE_GENERATOR_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_SOURCE_GENERATOR_ITF_NAME)) {
      return clientSourceGeneratorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_SOURCE_GENERATOR_ITF_NAME)) {
      clientSourceGeneratorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

}
