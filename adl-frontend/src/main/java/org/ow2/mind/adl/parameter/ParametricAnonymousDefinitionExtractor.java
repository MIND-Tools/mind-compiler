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

package org.ow2.mind.adl.parameter;

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
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.value.ast.Reference;

/**
 * This delegating {@link AnonymousDefinitionExtractor} component copies
 * {@link FormalParameter} from the <code>encapsulatingDefinition</code> to the
 * extracted anonymous definition. Moreover, this component adds the appropriate
 * {@link Argument arguments} to the {@link DefinitionReference} that references
 * the anonymous definition.
 */
public class ParametricAnonymousDefinitionExtractor
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

    if (encapsulatingDefinition instanceof FormalParameterContainer) {
      final FormalParameter[] parameters = ((FormalParameterContainer) encapsulatingDefinition)
          .getFormalParameters();
      if (parameters.length > 0) {

        // Add formal parameters to anonymous definition.
        final FormalParameterContainer subCompParams = turnsToParamContainer(anonymousDefinition);
        anonymousDefinition = (Definition) subCompParams;

        for (final FormalParameter parameter : parameters) {
          subCompParams.addFormalParameter(parameter);
        }

        // Add corresponding arguments to definition reference.
        final ArgumentContainer defRefArgs = turnsToArgumentContainer(component
            .getDefinitionReference());
        component.setDefinitionReference((DefinitionReference) defRefArgs);

        for (final FormalParameter parameter : parameters) {
          final Argument arg = newArgumentNode();
          arg.setName(parameter.getName());
          final Reference ref = newReferenceNode();
          ref.setRef(parameter.getName());
          arg.setValue(ref);
          defRefArgs.addArgument(arg);
        }
      }
    }

    return anonymousDefinition;
  }

  protected Argument newArgumentNode() {
    try {
      return (Argument) nodeFactoryItf.newNode("argument", Argument.class
          .getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected Reference newReferenceNode() {
    try {
      return (Reference) nodeFactoryItf.newNode("reference", Reference.class
          .getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected ArgumentContainer turnsToArgumentContainer(final Node node) {
    if (node instanceof ArgumentContainer) return (ArgumentContainer) node;

    // the given node does not implements ArgumentContainer.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = nodeFactoryItf.newNode(node.astGetType(),
          ArgumentContainer.class.getName());
      return (ArgumentContainer) nodeMergerItf.merge(node, n, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }

  protected FormalParameterContainer turnsToParamContainer(final Node node) {
    if (node instanceof FormalParameterContainer)
      return (FormalParameterContainer) node;

    // the given node does not implements FormalParameterContainer.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = nodeFactoryItf.newNode(node.astGetType(),
          FormalParameterContainer.class.getName());
      return (FormalParameterContainer) nodeMergerItf.merge(node, n, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

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
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 2];
    list[0] = NodeFactory.ITF_NAME;
    list[1] = NodeMerger.ITF_NAME;
    System.arraycopy(superList, 0, list, 2, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

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

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
