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

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotations.controller.BindingController;
import org.ow2.mind.annotation.Annotation;

/**
 * {@link ADLLoaderAnnotationProcessor annotation processor} for the
 * {@link BindingController} annotation.
 */
public class BindingControllerADLLoaderAnnotationProcessor
    extends
      AbstractControllerADLLoaderAnnotationProcessor
    implements
      DefaultControllerInterfaceConstants {

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof BindingController;
    if (((BindingController) annotation).allowNoRequiredItf) {
      return addControllerInterface(definition, BC,
          BINDING_CONTROLLER_SIGNATURE, "BindingController",
          "/fractal/internal/BCdelegate.c");
    } else {
      for (final Interface itf : castNodeError(definition,
          InterfaceContainer.class).getInterfaces()) {
        if (TypeInterfaceUtil.isClient(itf))
          return addControllerInterface(definition, BC,
              BINDING_CONTROLLER_SIGNATURE, "BindingController",
              "/fractal/internal/BCdelegate.c");
      }

      // definition has no client interface
      errorManagerItf.logError(ADLErrors.INVALID_BINDING_CONTROLLER_NO_BINDING,
          node);
      return null;
    }
  }
}
