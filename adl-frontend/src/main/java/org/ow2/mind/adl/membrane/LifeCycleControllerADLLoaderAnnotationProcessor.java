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

package org.ow2.mind.adl.membrane;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.predefined.controller.LifeCycleController;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.annotation.Annotation;

/**
 * {@link ADLLoaderAnnotationProcessor annotation processor} for the
 * {@link LifeCycleController} annotation.
 */
public class LifeCycleControllerADLLoaderAnnotationProcessor
    extends
      AbstractControllerADLLoaderAnnotationProcessor
    implements
      DefaultControllerInterfaceConstants {

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    if (phase == ADLLoaderPhase.ON_SUB_COMPONENT) {
      assert node instanceof Component;
      node.astSetDecoration("hasLifeCycleController", Boolean.TRUE);
    }

    return addControllerInterfae(definition, LCC,
        LIFECYCLE_CONTROLLER_SIGNATURE, "LifeCycleController",
        "/fractal/internal/LCCdelegate.c");
  }
}
