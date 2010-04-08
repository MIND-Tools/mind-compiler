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

package org.ow2.mind.adl.membrane;

import static org.ow2.mind.adl.membrane.ast.MembraneASTHelper.isInternalInterface;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;

public class CompositeInternalInterfaceLoader extends AbstractMembraneLoader {

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    Definition d = clientLoader.load(name, context);
    if ((d instanceof InterfaceContainer) && (d instanceof ComponentContainer)) {
      d = addInternalInterfaces(d, context);
    }
    return d;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected Definition addInternalInterfaces(Definition d,
      final Map<Object, Object> context) {
    InternalInterfaceContainer internalItfContainer = turnToInternalInterfaceContainer(d);
    d = (Definition) internalItfContainer;
    final ControllerContainer ctrlContainer = turnToControllerContainer(d);
    d = (Definition) ctrlContainer;
    internalItfContainer = (InternalInterfaceContainer) ctrlContainer;

    // find external interfaces that are already implemented by a controller
    final Set<String> implementedItfs = new HashSet<String>();
    for (final Controller ctrl : ctrlContainer.getControllers()) {
      for (final ControllerInterface ctrlItf : ctrl.getControllerInterfaces()) {
        if (!isInternalInterface(ctrlItf)) {
          implementedItfs.add(ctrlItf.getName());
        }
      }
    }

    for (final Interface itf : ((InterfaceContainer) d).getInterfaces()) {
      // if itf is already implemented by a controller, ignore it.
      if (implementedItfs.contains(itf.getName())) continue;

      // add dual internal interface
      final Interface internalItf = getInternalInterface(itf);
      internalItfContainer.addInternalInterface(internalItf);
      ControllerInterfaceDecorationHelper.setDelegatedInterface(internalItf,
          itf);
      ControllerInterfaceDecorationHelper.setDelegatedInterface(itf,
          internalItf);

      // add controller
      final Controller ctrl = newControllerNode();
      ctrl.addControllerInterface(newControllerInterfaceNode(itf.getName(),
          false));
      ctrl.addControllerInterface(newControllerInterfaceNode(itf.getName(),
          true));
      ctrl.addSource(newSourceNode("InterfaceDelegator"));
      ctrlContainer.addController(ctrl);
    }
    return d;
  }

  protected Interface getInternalInterface(final Interface itf) {
    if (!(itf instanceof TypeInterface)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, itf,
          "Interface is not a TypeInterface");
    }

    // clone external interface to create its dual internal interface.
    final TypeInterface internalItf;
    try {
      internalItf = (TypeInterface) nodeMergerItf.merge(nodeFactoryItf.newNode(
          "internalInterface", TypeInterface.class.getName()), itf, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
    if (TypeInterfaceUtil.isClient(itf))
      internalItf.setRole(TypeInterface.SERVER_ROLE);
    else
      internalItf.setRole(TypeInterface.CLIENT_ROLE);

    return internalItf;
  }

}
