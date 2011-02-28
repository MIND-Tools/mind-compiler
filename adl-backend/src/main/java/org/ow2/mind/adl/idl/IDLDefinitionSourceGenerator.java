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
 * Authors: Matthieu Leclercq, Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.adl.idl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;

import com.google.inject.Inject;

public class IDLDefinitionSourceGenerator implements DefinitionSourceGenerator {

  @Inject
  protected IDLLoader  idlLoaderItf;

  @Inject
  protected IDLVisitor idlCompilerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    if (definition instanceof InterfaceContainer) {
      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (!(itf instanceof TypeInterface)) continue;
        final InterfaceDefinition itfDef = InterfaceDefinitionDecorationHelper
            .getResolvedInterfaceDefinition((TypeInterface) itf, idlLoaderItf,
                context);
        idlCompilerItf.visit(itfDef, context);
      }
    }

    for (final InterfaceDefinition itfDef : InterfaceDefinitionDecorationHelper
        .getUsedInterfaceDefinitions(definition, idlLoaderItf, context)) {
      idlCompilerItf.visit(itfDef, context);
    }

    // TODO this can be moved in a dedicated component
    // ensure that the "memory.api.Allocator" interface is compiled
    final IDL itfDef = idlLoaderItf.load("memory.api.Allocator", context);
    idlCompilerItf.visit(itfDef, context);
  }
}
