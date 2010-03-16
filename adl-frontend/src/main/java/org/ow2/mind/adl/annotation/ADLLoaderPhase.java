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

/**
 * Enumeration to the phases of the ADL-loader chain on which an annotation
 * processor can be executed.
 * 
 * @see ADLLoaderProcessor#phases()
 * @see ADLLoaderAnnotationProcessor
 */
public enum ADLLoaderPhase {

  /**
   * Corresponds to the phase just after the ADL file has been parsed and
   * annotations validated.
   */
  AFTER_PARSING,

  /**
   * Corresponds to the phase just after the resolution of extended ADLs.
   */
  AFTER_EXTENDS,

  /**
   * Corresponds to the phase after every checking has been performed on ADL
   * definition.
   */
  AFTER_CHECKING,

  /**
   * Allows to execute the annotation processor if the annotation is present on
   * the definition of a sub-component. <br>
   * The <code>definition</code> parameter passed to the
   * ADLLoaderAnnotationProcessor is the definition of the composite component,
   * and the <code>node</code> parameter is the component node for which the
   * definition has the annotation.
   */
  ON_SUB_COMPONENT,

  /**
   * This phase is only applicable for template definitions. Correspond to the
   * phase just after an instantiation of the template definition.
   */
  AFTER_TEMPLATE_INSTANTIATE,

  /**
   * This phase is only applicable for template definitions. Allows to execute
   * the annotation processor if the annotation is present on the concrete
   * definition of a template sub-component. <br>
   * The <code>definition</code> parameter passed to the
   * ADLLoaderAnnotationProcessor is the definition of the composite component,
   * and the <code>node</code> parameter is the component node for which the
   * definition has the annotation.
   */
  ON_TEMPLATE_SUB_COMPONENT
}
