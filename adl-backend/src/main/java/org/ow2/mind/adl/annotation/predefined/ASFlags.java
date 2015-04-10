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
 * Contributors: Julien Tous
 */

package org.ow2.mind.adl.annotation.predefined;

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;

/**
 * ADL annotation that can be used to specify assembly compilation flags. This
 * annotation can be attached to :
 * <ul>
 * <li>{@link Source} node to specify AS-flags that must be used when compiling
 * the given implementation file.</li>
 * <li>{@link Definition} node to specify AS-flags that must be used when
 * compiling every source file related to this definition, include generated
 * files.</li>
 * </ul>
 */
public class ASFlags implements Annotation {

  private static final AnnotationTarget[] ANNOTATION_TARGET = {
      ADLAnnotationTarget.DEFINITION, ADLAnnotationTarget.SOURCE};

  /** The as-flags. */
  @AnnotationElement
  public String                           value;

  public AnnotationTarget[] getAnnotationTargets() {
    return ANNOTATION_TARGET;
  }

  public boolean isInherited() {
    return true;
  }
}
