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
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.inject.InjectDelegate;

/**
 * An interface that allows to resolve an {@link Include} node is a given
 * context.
 */
public interface IncludeResolver {

  /**
   * Resolves the given <code>include</code> node and returns the corresponding
   * {@link IDL} tree. The <code>encapsulatingIDL</code> and
   * <code>encapsulatingName</code> parameters allow to specify the context into
   * which the <code>include</code> node will be resolved.
   * 
   * @param include the {@link Include} node to resolve.
   * @param encapsulatingIDL the IDL that contains the given
   *          <code>include</code> node. May be <code>null</code>
   * @param encapsulatingName the name of the entity that contains the given
   *          <code>include</code> node. This name can be a fully qualified name
   *          (of an interface definition or an ADL) or a path (of a IDT file).
   *          May be <code>null</code> if and only if
   *          <code>encapsulatingIDL</code> is not <code>null</code>.
   * @param context additional parameters.
   * @return the {@link IDL} tree corresponding to the given include node.
   * @throws ADLException if an error occurs.
   */
  IDL resolve(Include include, IDL encapsulatingIDL, String encapsulatingName,
      Map<Object, Object> context) throws ADLException;

  /**
   * An abstract delegating {@link IncludeResolver} component.
   */
  public abstract class AbstractDelegatingIncludeResolver
      implements
        IncludeResolver {

    /**
     * The client {@link IncludeResolver} used by this component.
     */
    @InjectDelegate
    protected IncludeResolver clientResolverItf;
  }
}
