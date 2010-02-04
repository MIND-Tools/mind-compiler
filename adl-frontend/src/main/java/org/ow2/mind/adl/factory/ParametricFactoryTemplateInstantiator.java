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

package org.ow2.mind.adl.factory;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.getTemplateName;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.BindingControllerImplHelper;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.value.ast.Reference;

public class ParametricFactoryTemplateInstantiator
    implements
      TemplateInstantiator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #clientInstantiatorItf} client interface. */
  public static final String  CLIENT_INSTANTIATOR_ITF_NAME = "client-instantiator";

  /** The client {@link TemplateInstantiator} interface. */
  public TemplateInstantiator clientInstantiatorItf;

  /** The node factory interface */
  public NodeFactory          nodeFactoryItf;

  /** The node merger interface */
  public NodeMerger           nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TemplateInstantiator interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    // Instantiate template
    Definition templateInstance = clientInstantiatorItf.instantiateTemplate(
        genericDefinition, typeArgumentValues, context);

    // Try to get the template name from the 'templateName' decoration'. This is
    // useful if the template to instantiate is a partially instantiated
    // template.
    String name = getTemplateName(genericDefinition);
    if (name == null) name = genericDefinition.getName();

    if ((FactoryLoader.FACTORY_DEFINITION_NAME.equals(name) || FactoryLoader.FACTORY_CONTROLLED_DEFINITION_NAME
        .equals(name))
        && (templateInstance instanceof FormalParameterContainer)) {
      final FormalParameter[] formalParameters = ((FormalParameterContainer) templateInstance)
          .getFormalParameters();
      if (formalParameters.length > 0) {

        final AttributeContainer attributeContainer = turnsToAttributeContainer(templateInstance);
        if (attributeContainer != templateInstance)
          templateInstance = (Definition) attributeContainer;

        for (final FormalParameter formalParameter : formalParameters) {
          assert formalParameter.getName().startsWith(
              FactoryLoader.FORMAL_TYPE_PARAMETER_NAME + "$");

          final Attribute attrNode = newAttributeNode();
          attrNode.setName(formalParameter.getName().substring(
              FactoryLoader.FORMAL_TYPE_PARAMETER_NAME.length() + 1));

          final Reference reference = newReferenceNode();
          reference.setRef(formalParameter.getName());
          attrNode.setValue(reference);

          // TODO handle attribute type correctly
          final ParameterType inferredParameterType = ParameterASTHelper
              .getInferredParameterType(formalParameter);
          if (inferredParameterType != null)
            attrNode.setType(inferredParameterType.toString());

          attributeContainer.addAttribute(attrNode);
        }
      }
    }

    return templateInstance;
  }

  protected Attribute newAttributeNode() {
    try {
      return (Attribute) nodeFactoryItf.newNode("attribute", Attribute.class
          .getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected Reference newReferenceNode() {
    try {
      return (Reference) nodeFactoryItf.newNode("value", Reference.class
          .getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected AttributeContainer turnsToAttributeContainer(final Node node) {
    if (node instanceof AttributeContainer) return (AttributeContainer) node;

    // the given node does not implements AttributeContainer.
    // Create a node that implements it and merge it with the given node.
    try {
      final Node n = nodeFactoryItf.newNode(node.astGetType(),
          AttributeContainer.class.getName());
      return (AttributeContainer) nodeMergerItf.merge(node, n, null);
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    } catch (final MergeException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node merge error");
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = (NodeMerger) value;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = (TemplateInstantiator) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    return BindingControllerImplHelper
        .listFcHelper(CLIENT_INSTANTIATOR_ITF_NAME, NodeFactory.ITF_NAME,
            NodeMerger.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      return nodeMergerItf;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      return clientInstantiatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
