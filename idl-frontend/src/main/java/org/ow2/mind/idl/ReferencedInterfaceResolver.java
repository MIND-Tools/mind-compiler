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

import static org.ow2.mind.idl.ast.IDLASTHelper.addReferencedInterface;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.idl.InterfaceReferenceResolver.AbstractDelegatingInterfaceReferenceResolver;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class ReferencedInterfaceResolver
    extends
      AbstractDelegatingInterfaceReferenceResolver {

  // ---------------------------------------------------------------------------
  // Implementation of the InterfaceReferenceResolver interface
  // ---------------------------------------------------------------------------

  public InterfaceDefinition resolve(final String itfName,
      final IDL encapsulatingIDL, final Map<Object, Object> context)
      throws ADLException {
    final InterfaceDefinition itf = clientResolverItf.resolve(itfName,
        encapsulatingIDL, context);
    if (encapsulatingIDL != null) {
      addReferencedInterface(encapsulatingIDL, itf);
    }
    return itf;
  }
}
