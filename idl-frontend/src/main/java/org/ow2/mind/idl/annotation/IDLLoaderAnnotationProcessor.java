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

package org.ow2.mind.idl.annotation;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.ast.IDL;

/**
 * Interface implemented by component that process annotations while loading
 * {@link IDL}.
 */
public interface IDLLoaderAnnotationProcessor {

  /**
   * Process the given annotation that has been found on the given node that
   * belongs to the given IDL.
   * 
   * @param annotation the annotation to process
   * @param node the node on which the annotation has been found
   * @param idl the enclosing IDL
   * @param phase the current loading phase
   * @param context additional parameters
   * @return the new enclosing IDL if it is different from the given one,
   *         <code>null</code> otherwise.
   * @throws ADLException if an error occurs.
   */
  IDL processAnnotation(Annotation annotation, Node node, IDL idl,
      IDLLoaderPhase phase, Map<Object, Object> context) throws ADLException;
}
