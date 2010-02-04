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

package org.ow2.mind.adl.generic;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.anonymous.AbstractAnonymousDefinitionExtractor;
import org.ow2.mind.adl.anonymous.AnonymousDefinitionExtractor;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

/**
 * Delegating {@link AnonymousDefinitionExtractor} component that copies
 * {@link FormalTypeParameter} from the <code>encapsulatingDefinition</code> to
 * the extracted anonymous definition. Moreover, this component adds the
 * appropriate {@link TypeArgument type arguments} to the
 * {@link DefinitionReference} that references the anonymous definition.
 */
public class GenericAnonymousDefinitionExtractor
    extends
      AbstractAnonymousDefinitionExtractor {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The node factory interface */
  public NodeFactory nodeFactoryItf;

  /** The node merger interface */
  public NodeMerger  nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the AnonymousDefinitionResolver interface
  // ---------------------------------------------------------------------------

  public Definition extractAnonymousDefinition(final Component component,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) {
    Definition anonymousDefinition = clientExtractorItf
        .extractAnonymousDefinition(component, encapsulatingDefinition, context);
    if (anonymousDefinition instanceof ComponentContainer
        && encapsulatingDefinition instanceof FormalTypeParameterContainer) {
      final FormalTypeParameter[] typeParameters = ((FormalTypeParameterContainer) encapsulatingDefinition)
          .getFormalTypeParameters();
      if (typeParameters.length > 0) {

        // Adds formal type parameters of encapsulating definition to anonymous
        // definition.

        final FormalTypeParameterContainer subCompTypeParams = turnsToTypeParamContainer(anonymousDefinition);
        anonymousDefinition = (Definition) subCompTypeParams;

        for (final FormalTypeParameter typeParameter : typeParameters) {
          subCompTypeParams.addFormalTypeParameter(typeParameter);
        }

        // Add corresponding type arguments to definition reference.
        final TypeArgumentContainer defRefArgs = turnsToTypeArgumentContainer(component
            .getDefinitionReference());
        component.setDefinitionReference((DefinitionReference) defRefArgs);

        for (final FormalTypeParameter typeParameter : typeParameters) {
          final TypeArgument arg = newTypeArgumentNode();
          arg.setTypeParameterName(typeParameter.getName());
          arg.setTypeParameterReference(typeParameter.getName());
          defRefArgs.addTypeArgument(arg);
        }
      }
    }

    return anonymousDefinition;
  }

  protected FormalTypeParameterContainer turnsToTypeParamContainer(
      final Node node) {
    if (node instanceof FormalTypeParameterContainer)
      return (FormalTypeParameterContainer) node;

    // the given node does not implements FormalParameterContainer.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = nodeFactoryItf.newNode(node.astGetType(),
          FormalTypeParameterContainer.class.getName());
      return (FormalTypeParameterContainer) nodeMergerItf.merge(node, n, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }

  protected TypeArgumentContainer turnsToTypeArgumentContainer(final Node node) {
    if (node instanceof TypeArgumentContainer)
      return (TypeArgumentContainer) node;

    // the given node does not implements ArgumentContainer.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = nodeFactoryItf.newNode(node.astGetType(),
          TypeArgumentContainer.class.getName());
      return (TypeArgumentContainer) nodeMergerItf.merge(node, n, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }

  protected TypeArgument newTypeArgumentNode() {
    try {
      return (TypeArgument) nodeFactoryItf.newNode("argument",
          TypeArgument.class.getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = (NodeMerger) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), NodeFactory.ITF_NAME,
        NodeMerger.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      return nodeMergerItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
