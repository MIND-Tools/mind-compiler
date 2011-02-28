/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.adl;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.Error;
import org.ow2.mind.error.ErrorCollection;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;

/**
 * This delegating loader simply delegate loading to client loader then throw
 * and {@link ADLException} if the {@link ErrorManager} contains errors.
 */
public class ErrorLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager errorManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    errorManagerItf.clear();

    final Definition d = clientLoader.load(name, context);

    final List<Error> errors = errorManagerItf.getErrors();
    if (!errors.isEmpty()) {
      throw new ADLException(new ErrorCollection(errors));
    }

    return d;
  }
}
