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
 * Contributors: Matthieu Leclercq 
 */

package org.ow2.mind.idl.annotation;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.cecilia.adl.plugin.PluginManager;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.AbstractIDLLoader;
import org.ow2.mind.idl.ast.IDL;

public class AnnotationProcessorLoader extends AbstractIDLLoader
    implements
      AnnotationProcessorLoaderAttributes,
      BindingController {

  IDLLoaderPhase             phase;

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #pluginManagerItf} client interface */
  public final static String PLUGIN_MANAGER_ITF_NAME = "plugin-manager";

  /** Plugin manager client interface */
  public PluginManager       pluginManagerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final IDL idl = clientIDLLoaderItf.load(name, context);

    processAnnotations(idl, idl, context);

    return idl;
  }

  protected void processAnnotations(IDL idl, final Node node,
      final Map<Object, Object> context) throws ADLException {

    // Process this node
    final Annotation[] annotations = AnnotationHelper.getAnnotations(node);
    for (final Annotation annotation : annotations) {
      final IDLLoaderProcessor processorAnnotation = annotation.getClass()
          .getAnnotation(IDLLoaderProcessor.class);
      if (processorAnnotation != null) {
        final IDLLoaderPhase[] processPhases = processorAnnotation.phases();
        for (final IDLLoaderPhase processPhase : processPhases) {
          if (phase == processPhase) {
            idl = executeProcessor(processorAnnotation, annotation, node, idl,
                context);
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
            processAnnotations(idl, subNode, context);
          }
        }
      }
    }
  }

  protected IDL executeProcessor(final IDLLoaderProcessor processorAnnotation,
      final Annotation annotation, final Node node, final IDL idl,
      final Map<Object, Object> context) throws ADLException {
    final IDLLoaderAnnotationProcessor processor = pluginManagerItf.getPlugin(
        processorAnnotation.processor().getName(), context,
        IDLLoaderAnnotationProcessor.class);
    final IDL result = processor.processAnnotation(annotation, node, idl,
        phase, context);
    return (result != null) ? result : idl;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the AnnotationProcessorLoaderAttributes interface
  // ---------------------------------------------------------------------------

  public String getPhase() {
    return phase.name();
  }

  public void setPhase(final String phase) {
    this.phase = IDLLoaderPhase.valueOf(phase);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String clientItfName, final Object serverItf)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(clientItfName);

    if (clientItfName.startsWith(PLUGIN_MANAGER_ITF_NAME)) {
      pluginManagerItf = (PluginManager) serverItf;
    } else {
      super.bindFc(clientItfName, serverItf);
    }
  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), PLUGIN_MANAGER_ITF_NAME);
  }

  @Override
  public Object lookupFc(final String clientItfName)
      throws NoSuchInterfaceException {
    checkItfName(clientItfName);

    if (PLUGIN_MANAGER_ITF_NAME.equals(clientItfName)) {
      return pluginManagerItf;
    } else {
      return super.lookupFc(clientItfName);
    }
  }

  @Override
  public void unbindFc(final String clientItfName)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(clientItfName);

    if (clientItfName.startsWith(PLUGIN_MANAGER_ITF_NAME)) {
      pluginManagerItf = null;
    } else {
      super.unbindFc(clientItfName);
    }
  }
}
