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

package org.ow2.mind.adl.annotation;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.Annotation;

/**
 * Interface implemented by component that process annotations during ADL
 * loading.
 * 
 * @see ADLLoaderProcessor
 */
public interface ADLLoaderAnnotationProcessor {

  /**
   * Process the given annotation that has been found on the given node that
   * belongs to the given definition.
   * 
   * @param annotation the annotation to process
   * @param node the node on which the annotation has been found
   * @param definition the enclosing definition
   * @param phase the current loading phase
   * @param context additional parameters
   * @return the new enclosing definition if it is different from the given one,
   *         <code>null</code> otherwise.
   * @throws ADLException if an error occurs.
   */
  Definition processAnnotation(Annotation annotation, Node node,
      Definition definition, ADLLoaderPhase phase, Map<Object, Object> context)
      throws ADLException;
}
