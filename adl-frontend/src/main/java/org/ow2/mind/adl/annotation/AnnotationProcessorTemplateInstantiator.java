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

package org.ow2.mind.adl.annotation;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.cecilia.adl.plugin.PluginManager;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;

public class AnnotationProcessorTemplateInstantiator
    implements
      TemplateInstantiator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #clientInstantiatorItf} client interface. */
  public static final String         CLIENT_INSTANTIATOR_ITF_NAME = "client-instantiator";

  /** The client {@link TemplateInstantiator} interface. */
  public TemplateInstantiator        clientInstantiatorItf;

  /** The interface used to resolve referenced definitions. */
  public DefinitionReferenceResolver definitionReferenceResolverItf;

  /** The name of the {@link #pluginManagerItf} client interface */
  public final static String         PLUGIN_MANAGER_ITF_NAME      = "plugin-manager";

  /** Plugin manager client interface */
  public PluginManager               pluginManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TemplateInstantiator interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {
    // First, find direct sub-components that are actually instantiated
    final Map<Component, Definition> instantiatedSubComps = new HashMap<Component, Definition>();
    if (genericDefinition instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) genericDefinition)
          .getComponents()) {
        if (subComp instanceof FormalTypeParameterReference
            && ((FormalTypeParameterReference) subComp)
                .getTypeParameterReference() != null) {
          // sub component is an instance of a formal type parameter.
          final FormalTypeParameterReference typeParamRef = (FormalTypeParameterReference) subComp;
          final String ref = typeParamRef.getTypeParameterReference();
          // get the value of the formal type parameter.
          final Object value = typeArgumentValues.get(ref);
          assert value != null;

          if (value instanceof TypeArgument) {
            final DefinitionReference defRef = ((TypeArgument) value)
                .getDefinitionReference();
            if (defRef != null) {
              // the value of the formal type parameter is a definition
              // reference
              final Definition subCompDef = definitionReferenceResolverItf
                  .resolve(defRef, null, context);
              instantiatedSubComps.put(subComp, subCompDef);
            }
          }
        }
      }
    }

    Definition template = clientInstantiatorItf.instantiateTemplate(
        genericDefinition, typeArgumentValues, context);

    // Find Annotation on "instantiatedSubComp" with a ON_SUB_COMPONENT phase
    // and execute them.
    for (final Map.Entry<Component, Definition> instantiatedSubComp : instantiatedSubComps
        .entrySet()) {
      template = processSubComponent(template, instantiatedSubComp.getKey(),
          instantiatedSubComp.getValue(), context);
    }

    // Find Annotation in template with a AFTER_TEMPLATE_INSTANTIATE phase and
    // execute them.
    template = processAnnotations(template, template,
        ADLLoaderPhase.AFTER_TEMPLATE_INSTANTIATE, context);

    return template;
  }

  protected Definition processSubComponent(Definition compositeDef,
      final Component subComp, final Node node,
      final Map<Object, Object> context) throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final ADLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(ADLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final ADLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final ADLLoaderPhase processPhase : processPhases) {
          if (processPhase == ADLLoaderPhase.ON_TEMPLATE_SUB_COMPONENT) {
            compositeDef = executeProcessor(processorAnnotation, annotation,
                subComp, compositeDef,
                ADLLoaderPhase.ON_TEMPLATE_SUB_COMPONENT, context);
            break;
          }
        }
      }
    }

    // process sub nodes
    final String[] nodeTypes = node.astGetNodeTypes();
    if (nodeTypes != null) {
      for (final String nodeType : nodeTypes) {
        for (final Node subNode : node.astGetNodes(nodeType)) {
          if (subNode != null) {
            compositeDef = processSubComponent(compositeDef, subComp, subNode,
                context);
          }
        }
      }
    }
    return compositeDef;
  }

  protected Definition processAnnotations(Definition def, final Node node,
      final ADLLoaderPhase phase, final Map<Object, Object> context)
      throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final ADLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(ADLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final ADLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final ADLLoaderPhase processPhase : processPhases) {
          if (processPhase == phase) {
            def = executeProcessor(processorAnnotation, annotation, node, def,
                phase, context);
            break;
          }
        }
      }
    }

    // process sub nodes
    final String[] nodeTypes = node.astGetNodeTypes();
    if (nodeTypes != null) {
      for (final String nodeType : nodeTypes) {
        for (final Node subNode : node.astGetNodes(nodeType)) {
          if (subNode != null) {
            def = processAnnotations(def, subNode, phase, context);
          }
        }
      }
    }
    return def;
  }

  protected Definition executeProcessor(
      final ADLLoaderProcessor processorAnnotation,
      final Annotation annotation, final Node node,
      final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    final ADLLoaderAnnotationProcessor processor = pluginManagerItf.getPlugin(
        processorAnnotation.processor().getName(), context,
        ADLLoaderAnnotationProcessor.class);
    final Definition result = processor.processAnnotation(annotation, node,
        definition, phase, context);
    return (result != null) ? result : definition;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = (TemplateInstantiator) value;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.startsWith(PLUGIN_MANAGER_ITF_NAME)) {
      pluginManagerItf = (PluginManager) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    return listFcHelper(CLIENT_INSTANTIATOR_ITF_NAME,
        DefinitionReferenceResolver.ITF_NAME, PLUGIN_MANAGER_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      return clientInstantiatorItf;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.startsWith(PLUGIN_MANAGER_ITF_NAME)) {
      return pluginManagerItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = null;
    } else if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.startsWith(PLUGIN_MANAGER_ITF_NAME)) {
      pluginManagerItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

}
