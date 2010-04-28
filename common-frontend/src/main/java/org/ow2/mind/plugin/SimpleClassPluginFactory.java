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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.plugin;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.error.GenericErrors;

/**
 * A plugin factory that instantiates simple plugin components from their
 * implementation class.
 */
public class SimpleClassPluginFactory implements Factory {

  /**
   * Instantiates a plugin component from its definition class.
   * 
   * @param name the name of the plugin component's implementation class.
   * @param context optional additional information.
   * @return the plugin component that has been created.
   * @throws CompilerError if a problem occurs during the creation of the
   *           component.
   */
  public Object newComponent(final String name, final Map context)
      throws ADLException {
    try {
      return getClass().getClassLoader().loadClass(name).newInstance();
    } catch (final InstantiationException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Can't instantiate plugin \"" + name + "\".");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Can't instantiate plugin \"" + name + "\".");
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Could not find the plugin class \"" + name + "\".");
    }
  }

  public Object newComponentType(final String name, final Map context)
      throws ADLException {
    throw new UnsupportedOperationException();
  }
}
