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

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;

import com.google.inject.Inject;

/**
 * @author Matthieu ANNE
 */
public class DumpASTAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  @Inject
  private Loader loaderItf;

  private static void showComponents(final Definition definition,
      final int depth, final Loader loaderItf, final Map<Object, Object> context) {
    String prf = "  ";

    for (int i = 0; i < depth; i++)
      prf = prf + "   ";

    if (definition.astGetSource() != null)
      System.out.println(prf + "Definition source: "
          + definition.astGetSource());
    else
      System.out.println(prf + "No definition source for "
          + definition.getName() + "- probably generated on the fly");

    final Interface[] interfaces = ((InterfaceContainer) definition)
        .getInterfaces();
    if (interfaces.length == 0) {
      System.out.println(prf + "No interface");
    } else {
      System.out.println(prf + "Interface quantity: " + interfaces.length);
    }
    for (int i = 0; i < interfaces.length; i++) {
      final MindInterface itf = (MindInterface) interfaces[i];
      System.out.print(prf + "Interface[" + i + "] is " + itf.getName()
          + " with role " + itf.getRole() + ", signature: "
          + itf.getSignature());
      if (itf.getCardinality() != null) {
        System.out.print(", cardinality: " + itf.getCardinality());
      }
      if (itf.getContingency() != null) {
        System.out.print(", contingency: " + itf.getContingency());
      }
      if (itf.getNumberOfElement() != null) {
        System.out.print(", number of element: " + itf.getNumberOfElement());
      }
      System.out.println();
    }

    if (ASTHelper.isComposite(definition)) {
      final Component[] subComponents = ((ComponentContainer) definition)
          .getComponents();
      System.out.print(prf + "Subcomponent quantity: " + subComponents.length
          + " (" + subComponents[0].getName());
      for (int i = 1; i < subComponents.length; i++) {
        System.out.print(", " + subComponents[i].getName());
      }
      System.out.println(")");

      final Binding[] bindings = ((BindingContainer) definition).getBindings();
      if (bindings.length == 0) {
        System.out.println(prf + "No binding");
      } else {
        System.out.println(prf + "Binding quantity: " + bindings.length);
      }
      for (int i = 0; i < bindings.length; i++) {
        final Binding binding = bindings[i];
        System.out.print(prf + "Binding #" + i + " is from "
            + binding.getFromComponent() + "." + binding.getFromInterface());
        if (binding.getFromInterfaceNumber() != null) {
          System.out.print("[" + binding.getFromInterfaceNumber() + "]");
        }
        System.out.print(" to " + binding.getToComponent() + "."
            + binding.getToInterface());
        if (binding.getToInterfaceNumber() != null) {
          System.out.print("[" + binding.getToInterfaceNumber() + "]");
        }
        System.out.println();
      }

      for (int i = 0; i < subComponents.length; i++) {
        final Component subComponent = subComponents[i];

        try {
          Definition subCompDef = ASTHelper.getResolvedComponentDefinition(
              subComponent, loaderItf, context);
          if (subCompDef == null)
            subCompDef = ASTHelper.getResolvedDefinition(
                subComponent.getDefinitionReference(), loaderItf, context);

          if (subCompDef != null) {
            System.out.println(prf + "Component #" + i + ": "
                + subComponent.getName() + " (" + subCompDef.getName() + ")");

            showComponents(subCompDef, depth + 1, loaderItf, context);
          } else
            System.out.println("Could not resolve \""
                + subComponent.getDefinitionReference() + "\" definition !");
        } catch (final ADLException e) {
          System.out.println("Could not resolve \""
              + subComponent.getDefinitionReference() + "\" definition !");
        }
      }

    } else if (ASTHelper.isPrimitive(definition)) {
      final Source[] sources = ((ImplementationContainer) definition)
          .getSources();
      if (sources.length == 0) {
        System.out.println(prf + "No source (implementation)");
      } else {
        System.out.println(prf + "Source(s) quantity: " + sources.length);
      }

      for (int i = 0; i < sources.length; i++) {
        if (sources[i].getPath() != null) {
          System.out.print(prf + "Source #" + i + " is file: "
              + sources[i].getPath());
        } else if (sources[i].getCCode() != null) {
          System.out.print(prf + "Source #" + i + " is inlined C code");
        }
        System.out.println();
      }
    }
  }

  public static void showDefinitionContent(final Definition definition,
      final Loader loaderItf, final Map<Object, Object> context) {
    System.out.println("Showing content of current definition: "
        + definition.getName() + ";\n");
    showComponents(definition, 0, loaderItf, context);

    System.out
        .println("\n\n---------------------------------------------------------------\n\n");

  }

  /*
   * (non-Javadoc)
   * @see
   * org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor#processAnnotation
   * (org.ow2.mind.annotation.Annotation, org.objectweb.fractal.adl.Node,
   * org.objectweb.fractal.adl.Definition,
   * org.ow2.mind.adl.annotation.ADLLoaderPhase, java.util.Map)
   */
  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof DumpAST;
    showDefinitionContent(definition, loaderItf, context);
    return null;
  }

}
