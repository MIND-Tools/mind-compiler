/**
 * Copyright (C) 2013 Schneider-Electric
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
 * Authors: Stephane Seyvoz
 * Contributors: 
 */

package org.ow2.mind.idl.annotations;

import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;
import org.ow2.mind.idl.annotation.IDLAnnotationTarget;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.annotation.IDLLoaderProcessor;

@IDLLoaderProcessor(processor = ImportIDLAnnotationProcessor.class, phases = {IDLLoaderPhase.AFTER_PARSING})
public class ImportIDL implements Annotation {

  private static final AnnotationTarget[] ANNOTATION_TARGETS = {IDLAnnotationTarget.INTERFACE};

  @AnnotationElement
  public String[]                         value;

  public AnnotationTarget[] getAnnotationTargets() {
    return ANNOTATION_TARGETS;
  }

  public boolean isInherited() {
    return true;
  }

}
