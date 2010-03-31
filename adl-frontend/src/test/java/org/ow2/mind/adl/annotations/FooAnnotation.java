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

package org.ow2.mind.adl.annotations;

import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.ADLLoaderProcessor;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationTarget;

@ADLLoaderProcessor(processor = TestADLAnnotationProcessor.FooProcessor.class, phases = {
    ADLLoaderPhase.AFTER_PARSING, ADLLoaderPhase.AFTER_CHECKING,
    ADLLoaderPhase.ON_TEMPLATE_SUB_COMPONENT,
    ADLLoaderPhase.AFTER_TEMPLATE_INSTANTIATE})
public class FooAnnotation implements Annotation {

  private static final AnnotationTarget[] TARGETS = {
      ADLAnnotationTarget.SOURCE, ADLAnnotationTarget.BINDING};

  public AnnotationTarget[] getAnnotationTargets() {
    return TARGETS;
  }

  public boolean isInherited() {
    return false;
  }
}
