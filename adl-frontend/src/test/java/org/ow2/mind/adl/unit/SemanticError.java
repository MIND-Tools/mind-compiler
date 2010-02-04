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

package org.ow2.mind.adl.unit;

import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;

/**
 * A generic annotation that can be used on any ADL element to define an
 * expected semantic error.
 */
public class SemanticError implements Annotation {

  private static final AnnotationTarget[] TARGETS = {
      ADLAnnotationTarget.IMPORT, ADLAnnotationTarget.DEFINITION,
      ADLAnnotationTarget.INTERFACE, ADLAnnotationTarget.ATTRIBUTE,
      ADLAnnotationTarget.COMPONENT, ADLAnnotationTarget.DATA,
      ADLAnnotationTarget.SOURCE, ADLAnnotationTarget.BINDING};

  @AnnotationElement(hasDefaultValue = true)
  public String                           GroupId = ADLErrors.GROUP_ID;

  @AnnotationElement
  public String                           ErrorId;

  /*
   * (non-Javadoc)
   * @see
   * org.ow2.mind.annotation.Annotation#getAnnotationTargets()
   */
  public AnnotationTarget[] getAnnotationTargets() {
    return TARGETS;
  }

  public boolean isInherited() {
    return false;
  }

}
