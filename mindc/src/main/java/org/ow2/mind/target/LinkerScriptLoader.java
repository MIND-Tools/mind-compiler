/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target;

import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.PathHelper;
import org.ow2.mind.PathHelper.InvalidRelativPathException;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.target.TargetDescriptorLoader.AbstractDelegatingTargetDescriptorLoader;
import org.ow2.mind.target.ast.Target;

import com.google.inject.Inject;

public class LinkerScriptLoader
    extends
      AbstractDelegatingTargetDescriptorLoader {

  @Inject
  protected ImplementationLocator implementationLocator;

  @Inject
  protected ErrorManager          errorManager;

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Target target = clientLoader.load(name, context);
    if (target.getLinkerScript() != null) {
      String path;
      try {
        path = PathHelper.fullyQualifiedNameToAbsolute(name, target
            .getLinkerScript().getPath());
      } catch (final InvalidRelativPathException e) {
        errorManager.logError(TargetDescErrors.INVALID_LINKER_SCRIPT,
            target.getLinkerScript(), target.getLinkerScript().getPath());
        return target;
      }

      final URL linkerScriptURL = implementationLocator.findSource(path,
          context);

      if (linkerScriptURL == null) {
        errorManager.logError(TargetDescErrors.LINKER_SCRIPT_NOT_FOUND,
            target.getLinkerScript(), target.getLinkerScript().getPath());
      } else {
        target.getLinkerScript().setPath(linkerScriptURL.getPath());
      }
    }
    return target;
  }

}
