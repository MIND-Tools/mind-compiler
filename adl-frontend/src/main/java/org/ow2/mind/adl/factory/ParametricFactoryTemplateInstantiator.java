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
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.generic.TemplateInstantiator.AbstractDelegatingTemplateInstantiator;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.value.ast.Reference;

import com.google.inject.Inject;

public class ParametricFactoryTemplateInstantiator
    extends
      AbstractDelegatingTemplateInstantiator {

  @Inject
  protected NodeFactory nodeFactoryItf;

  @Inject
  protected NodeMerger  nodeMergerItf;

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

          final ParameterType inferredParameterType = ParameterASTHelper
              .getInferredParameterType(formalParameter);
          if (inferredParameterType != null)
            attrNode.setType(inferredParameterType.getCType());
          else
            attrNode.setType("int");

          attributeContainer.addAttribute(attrNode);
        }
      }
    }

    return templateInstance;
  }

  protected Attribute newAttributeNode() {
    try {
      return (Attribute) nodeFactoryItf.newNode("attribute",
          Attribute.class.getName());
    } catch (final ClassNotFoundException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Node factory error");
    }
  }

  protected Reference newReferenceNode() {
    try {
      return (Reference) nodeFactoryItf.newNode("value",
          Reference.class.getName());
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
}
