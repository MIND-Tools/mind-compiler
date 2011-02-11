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

import static org.ow2.mind.idl.ast.IDLASTHelper.getIncludedIDL;
import static org.ow2.mind.idl.ast.IDLASTHelper.setIncludedIDL;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.idl.IncludeResolver.AbstractDelegatingIncludeResolver;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.Include;

import com.google.inject.Inject;

public class CachingIncludeResolver extends AbstractDelegatingIncludeResolver {

  @Inject
  protected IDLLoader idlLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the UsedIDLResolver interface
  // ---------------------------------------------------------------------------

  public IDL resolve(final Include usedIDL, final IDL encapsulatingIDL,
      final Map<Object, Object> context) throws ADLException {

    IDL idl = getIncludedIDL(usedIDL, idlLoaderItf, context);
    if (idl == null) {
      idl = clientResolverItf.resolve(usedIDL, encapsulatingIDL, context);
      setIncludedIDL(usedIDL, idl);
    }

    return idl;
  }
}
