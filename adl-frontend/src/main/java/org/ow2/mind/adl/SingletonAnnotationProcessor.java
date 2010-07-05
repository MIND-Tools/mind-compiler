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

package org.ow2.mind.adl;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeUtil;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.predefined.Singleton;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationHelper;

public class SingletonAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLoaderAnnotationProcessor interface
  // ---------------------------------------------------------------------------

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof Singleton;
    if (phase == ADLLoaderPhase.AFTER_CHECKING
        || phase == ADLLoaderPhase.AFTER_TEMPLATE_INSTANTIATE) {
      ASTHelper.setSingletonDecoration(definition);
      checkDuplicatedSingleton(definition, new HashMap<Definition, Node>(),
          context);

    } else if (phase == ADLLoaderPhase.ON_SUB_COMPONENT) {
      final Component subComp = NodeUtil.castNodeError(node, Component.class);
      if (!ASTHelper.isSingleton(definition)) {
        // The definition contains a sub-component that is a singleton but is
        // not marked as singleton itself. raise a warning
        errorManagerItf.logWarning(ADLErrors.WARNING_SINGLETON_SUB_COMPONENT,
            subComp, subComp.getName());
        AnnotationHelper.addAnnotation(definition, new Singleton());
      }

    } else if (phase == ADLLoaderPhase.ON_TEMPLATE_SUB_COMPONENT) {
      if (!ASTHelper.isSingleton(definition)) {
        // The template definition contains a sub-component that is a singleton
        // but is not marked as singleton itself.
        AnnotationHelper.addAnnotation(definition, new Singleton());
      }

    }
    return null;
  }

  protected void checkDuplicatedSingleton(final Definition definition,
      final Map<Definition, Node> singletonDefs,
      final Map<Object, Object> context) throws ADLException {
    if (definition instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) definition)
          .getComponents()) {
        final Definition subCompDef = ASTHelper.getResolvedComponentDefinition(
            subComp, loaderItf, context);
        if (ASTHelper.isSingleton(subCompDef)) {
          final Node previousUse = singletonDefs.get(subCompDef);
          if (previousUse != null) {
            errorManagerItf.logError(
                ADLErrors.INVALID_SUB_COMPONENT_DUPLICATE_SINGLETON, subComp,
                subComp.getName(), subCompDef.getName(),
                previousUse.astGetSource());
            continue;
          }
          singletonDefs.put(subCompDef, subComp);
        }
      }
    }
  }
}
