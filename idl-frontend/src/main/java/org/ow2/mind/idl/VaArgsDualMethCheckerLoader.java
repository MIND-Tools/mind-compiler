/**
 * Copyright (C) 2011 France Telecom
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
 * Authors: Matthieu ANNE
 * Contributors: 
 */

package org.ow2.mind.idl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader.AbstractDelegatingIDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

import com.google.inject.Inject;

public class VaArgsDualMethCheckerLoader extends AbstractDelegatingIDLLoader {

  @Inject
  protected ErrorManager errorManagerItf;

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);
    checkDualMeths(idl, context);
    return idl;
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  protected void checkDualMeths(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    if (idl instanceof InterfaceDefinition) {
      for (final Method method : ((InterfaceDefinition) idl).getMethods()) {
        if (method.getVaArgs2() != null) {
          if (method.getVaArgs2().getDualMethodName() == null) {
            errorManagerItf.logWarning(IDLErrors.UNDEFINED_DUALMETH, method,
                method.getName());
          }
        }
      }
    }
  }
}
