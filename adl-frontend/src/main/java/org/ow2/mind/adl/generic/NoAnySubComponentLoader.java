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

package org.ow2.mind.adl.generic;

import static org.ow2.mind.adl.generic.ast.GenericASTHelper.getAnyDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

public class NoAnySubComponentLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    if (d instanceof ComponentContainer) {
      final ComponentContainer container = (ComponentContainer) d;
      for (final Component subComp : container.getComponents()) {
        final TypeArgument typeArg = getAnyDefinition(subComp);
        if (typeArg != null) {
          errorManagerItf.logError(ADLErrors.INVALID_ANY_TEMPLATE_VALUE,
              typeArg, subComp.getName());
          // remove subComp from container to avoid error propagation
          container.removeComponent(subComp);
        }
      }
    }
    return d;
  }
}
