/**
 * Copyright (C) 2010 France Telecom
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
 * Authors: Matthieu ANNE
 * Contributors:
 */

package org.ow2.mind.adl.annotations;

import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.ADLLoaderProcessor;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationTarget;

/**
 * @author Matthieu ANNE
 */
@ADLLoaderProcessor(processor = DumpASTAnnotationProcessor.class, phases = { ADLLoaderPhase.AFTER_CHECKING })
public class DumpAST implements Annotation {

    private static final AnnotationTarget[] ANNOTATION_TARGETS = { ADLAnnotationTarget.DEFINITION };

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.mind.annotation.Annotation#getAnnotationTargets()
     */
    public AnnotationTarget[] getAnnotationTargets() {
	return ANNOTATION_TARGETS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.mind.annotation.Annotation#isInherited()
     */
    public boolean isInherited() {
	return true;
    }

}
