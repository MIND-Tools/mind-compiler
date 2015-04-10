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

package org.ow2.mind.adl.annotation.predefined;

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;

/**
 * ADL annotation that can be used to specify link flags. This annotation can be
 * attached to {@link Source} and {@link Definition} nodes.
 */
public class LDFlags implements Annotation {

  private static final AnnotationTarget[] ANNOTATION_TARGET = {
      ADLAnnotationTarget.DEFINITION, ADLAnnotationTarget.SOURCE};

  /** The c-flags. */
  @AnnotationElement
  public String                           value;

  public AnnotationTarget[] getAnnotationTargets() {
    return ANNOTATION_TARGET;
  }

  public boolean isInherited() {
    return true;
  }
}
