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

import static org.ow2.mind.PathHelper.getExtension;
import static org.ow2.mind.idl.ast.IDLASTHelper.getIncludedPath;
import static org.ow2.mind.idl.ast.Include.HEADER_EXTENSION;
import static org.ow2.mind.idl.ast.Include.IDT_EXTENSION;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader.AbstractDelegatingIDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;

import com.google.inject.Inject;

public class IncludeLoader extends AbstractDelegatingIDLLoader {

  @Inject
  protected ErrorManager    errorManagerItf;

  @Inject
  protected IncludeResolver idlResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);

    if (idl instanceof IncludeContainer) {
      checkIncludes(idl, context);
    }

    return idl;
  }

  protected void checkIncludes(final IDL container,
      final Map<Object, Object> context) throws ADLException {
    for (final Include include : ((IncludeContainer) container).getIncludes()) {
      final String path = getIncludedPath(include);

      final String extension = getExtension(path);
      if (extension == null
          || !(extension.equals(IDT_EXTENSION) || extension
              .equals(HEADER_EXTENSION))) {
        errorManagerItf.logError(IDLErrors.INVALID_INCLUDE, include, path);
      }

      idlResolverItf.resolve(include, container, context);
    }
  }
}
