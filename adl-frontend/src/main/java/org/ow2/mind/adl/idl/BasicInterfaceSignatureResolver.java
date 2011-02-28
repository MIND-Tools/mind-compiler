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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLErrors;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

import com.google.inject.Inject;

public class BasicInterfaceSignatureResolver
    implements
      InterfaceSignatureResolver {

  @Inject
  protected ErrorManager errorManagerItf;

  @Inject
  protected NodeFactory  nodeFactoryItf;

  @Inject
  protected IDLLoader    idlLoaderItf;

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
      if (e.getError().getTemplate() == IDLErrors.IDL_NOT_FOUND) {
        errorManagerItf.logError(IDLErrors.IDL_NOT_FOUND, itf,
            itf.getSignature());
        itfDefinition = IDLASTHelper.newUnresolvedInterfaceDefinitionNode(
            nodeFactoryItf, itf.getSignature());
      } else {
        throw e;
      }
    }
    if (!(itfDefinition instanceof InterfaceDefinition)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Referenced IDL is not an interface definition");
    }
    return (InterfaceDefinition) itfDefinition;
  }
}
