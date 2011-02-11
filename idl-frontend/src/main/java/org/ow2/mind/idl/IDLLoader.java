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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.NameHelper;
import org.ow2.mind.PathHelper;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.inject.InjectDelegate;

/**
 * {@link IDL} loader interface.
 */
public interface IDLLoader {

  /**
   * Load the {@link IDL} whose name is given.
   * 
   * @param name the name of an IDL file. If the given name starts with
   *          <code>"/"</code>, the name is supposed to be an absolute path (see
   *          {@link PathHelper}) to an <code>.idt</code> file. Otherwise, the
   *          given name is supposed to be a fully qualified name (see
   *          {@link NameHelper}) of a {@link InterfaceDefinition}.
   * @param context optional additional information.
   * @return the {@link InterfaceDefinition} whose name is given.
   * @throws ADLException if the file cannot be found, or if it contains errors.
   */
  IDL load(String name, Map<Object, Object> context) throws ADLException;

  /**
   * An abstract delegating {@link IDLLoader} component.
   */
  public abstract class AbstractDelegatingIDLLoader implements IDLLoader {

    /**
     * The client {@link IDLLoader} used by this component.
     */
    @InjectDelegate
    protected IDLLoader clientIDLLoaderItf;
  }

}
