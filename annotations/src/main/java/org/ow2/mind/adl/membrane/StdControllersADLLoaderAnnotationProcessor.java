/**
 * Copyright (C) 2010 STMicroelectronics
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
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.ADLLoaderProcessor;
import org.ow2.mind.adl.annotations.controller.AttributeController;
import org.ow2.mind.adl.annotations.controller.BindingController;
import org.ow2.mind.adl.annotations.controller.Component;
import org.ow2.mind.adl.annotations.controller.ContentController;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.annotation.Annotation;

public class StdControllersADLLoaderAnnotationProcessor
    extends
      AbstractControllerADLLoaderAnnotationProcessor {

  protected static final Class<? extends Annotation> ATTRIBUTE_ANNO = AttributeController.class;
  protected static final Class<? extends Annotation> BINDING_ANNO   = BindingController.class;
  protected static final Class<? extends Annotation> COMPONENT_ANNO = Component.class;
  protected static final Class<? extends Annotation> CONTENT_ANNO   = ContentController.class;

  public Definition processAnnotation(final Annotation annotation,
      final Node node, Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    // execute processor of attribute controller if definition contains at least
    // one attribute
    if (definition instanceof AttributeContainer
        && ((AttributeContainer) definition).getAttributes().length > 0) {
      definition = execProcessor(ATTRIBUTE_ANNO, definition, phase, context);
    }

    // execute processor of binding controller if definition contains at least
    // one client interface
    if (definition instanceof InterfaceContainer) {
      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (TypeInterfaceUtil.isClient(itf)) {
          definition = execProcessor(BINDING_ANNO, definition, phase, context);
          break;
        }
      }
    }

    // execute processor of component controller in any case
    definition = execProcessor(COMPONENT_ANNO, definition, phase, context);

    // execute processor of content controller if definition can contains
    // sub-components
    if (definition instanceof ComponentContainer) {
      definition = execProcessor(CONTENT_ANNO, definition, phase, context);
    }

    return definition;
  }

  protected Definition execProcessor(
      final Class<? extends Annotation> annoClass, final Definition definition,
      final ADLLoaderPhase phase, final Map<Object, Object> context)
      throws ADLException {
    final ADLLoaderProcessor annoProcessor = annoClass
        .getAnnotation(ADLLoaderProcessor.class);
    if (annoProcessor == null || !execForPhase(annoProcessor, phase))
      return definition;

    final Class<? extends ADLLoaderAnnotationProcessor> processorClass = annoProcessor
        .processor();
    final ADLLoaderAnnotationProcessor processorInstance;
    Annotation annotationInstance;

    try {
      processorInstance = getProcessor(processorClass);
      annotationInstance = annoClass.newInstance();
    } catch (final InstantiationException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Can't execute annotaton processor");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Can't execute annotaton processor");
    }
    final Definition r = processorInstance.processAnnotation(
        annotationInstance, definition, definition, phase, context);
    return (r == null) ? definition : r;
  }

  protected ADLLoaderAnnotationProcessor getProcessor(
      final Class<? extends ADLLoaderAnnotationProcessor> processorClass)
      throws InstantiationException, IllegalAccessException {
    final ADLLoaderAnnotationProcessor processorInstance = processorClass
        .newInstance();
    if (processorInstance instanceof org.objectweb.fractal.api.control.BindingController) {
      final org.objectweb.fractal.api.control.BindingController processorBC = (org.objectweb.fractal.api.control.BindingController) processorInstance;
      final String[] processorClientItfs = processorBC.listFc();
      for (final String processorClientItf : processorClientItfs) {
        try {
          processorBC.bindFc(processorClientItf, lookupFc(processorClientItf));
        } catch (final NoSuchInterfaceException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Can't execute annotaton processor");
        } catch (final IllegalBindingException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Can't execute annotaton processor");
        } catch (final IllegalLifeCycleException e) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
              "Can't execute annotaton processor");
        }
      }
    }
    return processorInstance;
  }

  protected boolean execForPhase(final ADLLoaderProcessor annoProcessor,
      final ADLLoaderPhase phase) {
    final ADLLoaderPhase[] phases = annoProcessor.phases();
    for (final ADLLoaderPhase phase2 : phases) {
      if (phase2 == phase) return true;
    }
    return false;
  }
}
