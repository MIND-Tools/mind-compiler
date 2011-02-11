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

import static org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper.setResolvedInterfaceDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

import com.google.inject.Inject;

public class InterfaceSignatureLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager               errorManagerItf;

  @Inject
  protected NodeFactory                nodeFactoryItf;

  @Inject
  protected InterfaceSignatureResolver interfaceSignatureResolverItf;

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
      if (e.getError().getTemplate() == InterfaceErrors.INTERFACE_NOT_FOUND) {
        errorManagerItf.logError(InterfaceErrors.INTERFACE_NOT_FOUND, itf,
            itf.getSignature());
      }
      itfDef = IDLASTHelper.newUnresolvedInterfaceDefinitionNode(
          nodeFactoryItf, itf.getSignature());
    }

    setResolvedInterfaceDefinition(itf, itfDef);
    InputResourcesHelper.addInputResources(def,
        InputResourcesHelper.getInputResources(itfDef));
  }
}
