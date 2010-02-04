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

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.ow2.mind.adl.membrane.ControllerInterfaceDecorationHelper.setReferencedInterface;
import static org.ow2.mind.adl.membrane.ast.MembraneASTHelper.isInternalInterface;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.adl.membrane.ast.InternalInterfaceContainer;

public class MembraneCheckerLoader extends AbstractLoader
    implements
      DefaultControllerInterfaceConstants {

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    checkMembrane(d, context);
    return d;
  }

  protected void checkMembrane(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final InterfaceContainer itfContainer = castNodeError(definition,
        InterfaceContainer.class);
    final Interface[] externalItfArray = itfContainer.getInterfaces();
    final Map<String, Interface> externalInterfaces = new HashMap<String, Interface>(
        externalItfArray.length);
    final Set<Interface> unusedExternalInterfaces = new HashSet<Interface>(
        externalItfArray.length);

    for (final Interface itf : externalItfArray) {
      externalInterfaces.put(itf.getName(), itf);
      unusedExternalInterfaces.add(itf);
    }

    final Map<String, Interface> internalInterfaces;
    final Set<Interface> unusedInternalInterfaces;
    if (definition instanceof InternalInterfaceContainer) {
      final Interface[] internalItfArray = ((InternalInterfaceContainer) definition)
          .getInternalInterfaces();
      internalInterfaces = new HashMap<String, Interface>(
          internalItfArray.length);
      unusedInternalInterfaces = new HashSet<Interface>(internalItfArray.length);
      for (final Interface itf : internalItfArray) {
        internalInterfaces.put(itf.getName(), itf);
        unusedInternalInterfaces.add(itf);
      }
    } else {
      internalInterfaces = Collections.emptyMap();
      unusedInternalInterfaces = Collections.emptySet();
    }

    if (definition instanceof ControllerContainer) {
      for (final Controller controller : ((ControllerContainer) definition)
          .getControllers()) {
        for (final ControllerInterface ctrlItf : controller
            .getControllerInterfaces()) {
          Interface itf;
          if (isInternalInterface(ctrlItf)) {
            itf = internalInterfaces.get(ctrlItf.getName());
            if (itf == null) {
              // TODO throw a specific error
              throw new ADLException(GenericErrors.GENERIC_ERROR, ctrlItf,
                  "Invalid controller interface : " + ctrlItf.getName());
            }
            unusedInternalInterfaces.remove(itf);
          } else {
            itf = externalInterfaces.get(ctrlItf.getName());
            if (itf == null) {
              // TODO throw a specific error
              throw new ADLException(GenericErrors.GENERIC_ERROR, ctrlItf,
                  "Invalid controller interface : " + ctrlItf.getName());
            }
            unusedExternalInterfaces.remove(itf);
          }
          setReferencedInterface(ctrlItf, itf);
        }
      }
    }

    if (!unusedInternalInterfaces.isEmpty()) {
      final Interface itf = unusedInternalInterfaces.iterator().next();
      // TODO throw a specific error
      throw new ADLException(GenericErrors.GENERIC_ERROR, itf,
          "Internal interface : " + itf.getName()
              + " is not used by the membrane");
    }

    if ((!unusedExternalInterfaces.isEmpty())
        && !ASTHelper.isType(definition)
        && !ASTHelper.isAbstract(definition)
        && (!(definition instanceof ImplementationContainer) || ((ImplementationContainer) definition)
            .getSources().length == 0)) {
      // some external interfaces are not implemented by membrane, and the
      // component do not contains implementation.
      final Iterator<Interface> iter = unusedExternalInterfaces.iterator();
      String itfNames = "\"" + iter.next().getName() + "\"";
      while (iter.hasNext())
        itfNames += ", \"" + iter.next().getName() + "\"";
      // TODO throw a specific error
      throw new ADLException(GenericErrors.GENERIC_ERROR, definition,
          "Interfaces : " + itfNames
              + " are not implemented. Add a source file or a controller.");
    }

    final Interface compItf = externalInterfaces.get(CI);
    if (compItf != null && externalItfArray[0] != compItf) {
      // the definition has a "component" controller interface that is not the
      // first one in the list of externalArray interfaces.
      for (final Interface itf : externalItfArray) {
        itfContainer.removeInterface(itf);
      }

      itfContainer.addInterface(compItf);
      for (final Interface itf : externalItfArray) {
        if (itf != compItf) itfContainer.addInterface(itf);
      }
    }
  }
}
