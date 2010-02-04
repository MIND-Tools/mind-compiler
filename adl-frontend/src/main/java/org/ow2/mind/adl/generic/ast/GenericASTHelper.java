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

package org.ow2.mind.adl.generic.ast;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.TemplateInstantiator;

/**
 * Helper methods for generic AST nodes.
 */
public final class GenericASTHelper extends ASTHelper {

  public static final String ANY_DEFINITION_DECORATION_NAME = "any_definition";

  public static void setAnyDefinition(final Component component,
      final TypeArgument typeArgument) {
    component.astSetDecoration(ANY_DEFINITION_DECORATION_NAME, typeArgument);
  }

  public static TypeArgument getAnyDefinition(final Component component) {
    return (TypeArgument) component
        .astGetDecoration(ANY_DEFINITION_DECORATION_NAME);
  }

  /**
   * The name of the decoration used to mark used type parameter nodes. This
   * decoration can be used to check if a type parameter node is actually used
   * by a definition.
   * 
   * @see #setUsedTypeParameter(FormalTypeParameter)
   * @see #isUsedTypeParameter(FormalTypeParameter)
   */
  public static final String USED_TYPE_PARAMETER_DECORATION_NAME = "used-type-parameter";

  /**
   * Mark the given type parameter as used.
   * 
   * @param typeParameter a type parameter node.
   * @see #USED_TYPE_PARAMETER_DECORATION_NAME
   * @see #isUsedTypeParameter(FormalTypeParameter)
   */
  public static void setUsedTypeParameter(
      final FormalTypeParameter typeParameter) {
    typeParameter.astSetDecoration(USED_TYPE_PARAMETER_DECORATION_NAME,
        Boolean.TRUE);
  }

  /**
   * Returns <code>true</code> if the given type parameter is marked as used.
   * 
   * @param typeParameter a type parameter node.
   * @return <code>true</code> if the given import is marked as used.
   * @see #USED_TYPE_PARAMETER_DECORATION_NAME
   * @see #setUsedTypeParameter(FormalTypeParameter)
   */
  public static boolean isUsedTypeParameter(
      final FormalTypeParameter typeParameter) {
    final Boolean b = (Boolean) typeParameter
        .astGetDecoration(USED_TYPE_PARAMETER_DECORATION_NAME);
    return (b != null) && b;
  }

  /**
   * The name of a decoration used internally by {@link TemplateInstantiator} to
   * store the original name of a partially instantiated template.
   * 
   * @see #setTemplateName(Definition, String)
   * @see #getTemplateName(Definition)
   */
  public static final String TEMPLATE_NAME_DECORATION_NAME = "template-name";

  /**
   * Set the value of the {@link #TEMPLATE_NAME_DECORATION_NAME} decoration.
   * 
   * @param template a template definition.
   * @param name the value of the decoration.
   */
  public static void setTemplateName(final Definition template,
      final String name) {
    template.astSetDecoration(TEMPLATE_NAME_DECORATION_NAME, name);
  }

  /**
   * Returns the value of the {@link #TEMPLATE_NAME_DECORATION_NAME} decoration.
   * 
   * @param template a template definition.
   * @return the value of the decoration.
   */
  public static String getTemplateName(final Definition template) {
    return (String) template.astGetDecoration(TEMPLATE_NAME_DECORATION_NAME);
  }

  /**
   * The name of a decoration used internally by {@link TemplateInstantiator} to
   * to specify that a definition is a partially instantiated template.
   * 
   * @see #setPartiallyInstiantedTemplate(Definition, boolean)
   * @see #isPartiallyInstantiatedTemplate(Definition)
   */
  public static final String PARTIALLY_INSTANTIATED_TEMPLATE_DECORATION_NAME = "partially-instantiated";

  /**
   * Set the value of the
   * {@link #PARTIALLY_INSTANTIATED_TEMPLATE_DECORATION_NAME} decoration.
   * 
   * @param templateInstance a template definition.
   * @param value the value of the decoration.
   */
  public static void setPartiallyInstiantedTemplate(
      final Definition templateInstance, final boolean value) {
    templateInstance.astSetDecoration(
        PARTIALLY_INSTANTIATED_TEMPLATE_DECORATION_NAME, value);
  }

  /**
   * Returns the value of the
   * {@link #PARTIALLY_INSTANTIATED_TEMPLATE_DECORATION_NAME} decoration.
   * 
   * @param templateInstance a template definition.
   * @return the value of the decoration.
   */
  public static boolean isPartiallyInstantiatedTemplate(
      final Definition templateInstance) {
    final Boolean b = (Boolean) templateInstance
        .astGetDecoration(PARTIALLY_INSTANTIATED_TEMPLATE_DECORATION_NAME);
    return b != null && ((Boolean) b);
  }

  /**
   * Create a new {@link FormalTypeParameter} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the value of the {@link FormalTypeParameter#getName() type
   *          parameter name}.
   * @param conformsto the reference to the type definition to which the formal
   *          type parameter conforms to.
   * @return a new {@link FormalTypeParameter} node.
   */
  public static FormalTypeParameter newFormalTypeParameter(
      final NodeFactory nodeFactory, final String name,
      final DefinitionReference conformsto) {
    final FormalTypeParameter para = ASTHelper.newNode(nodeFactory,
        "formalTypeParameter", FormalTypeParameter.class);
    para.setName(name);
    para.setDefinitionReference(conformsto);
    return para;
  }

  /**
   * Create a new {@link TypeArgument} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link TypeArgument} node.
   */
  public static TypeArgument newTypeArgument(final NodeFactory nodeFactory) {
    final TypeArgument typeArg = ASTHelper.newNode(nodeFactory, "typeArgument",
        TypeArgument.class);
    return typeArg;
  }

  /**
   * Transforms the given node to an {@link FormalTypeParameterContainer}. If
   * the node already implements the {@link FormalTypeParameterContainer}
   * interface, this method simply cast it. Otherwise this method use the given
   * node factory and node merger to create a copy of the given node that
   * implements the {@link FormalTypeParameterContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as
   *         {@link FormalTypeParameterContainer}, or a copy of the given node
   *         that implements {@link FormalTypeParameterContainer}.
   */
  public static FormalTypeParameterContainer turnsToFormalTypeParameterContainer(
      final Node node, final NodeFactory nodeFactory,
      final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, FormalTypeParameterContainer.class,
        nodeFactory, nodeMerger);
  }

  /**
   * Transforms the given node to an {@link TypeArgumentContainer}. If the node
   * already implements the {@link TypeArgumentContainer} interface, this method
   * simply cast it. Otherwise this method use the given node factory and node
   * merger to create a copy of the given node that implements the
   * {@link TypeArgumentContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link TypeArgumentContainer}, or a
   *         copy of the given node that implements
   *         {@link TypeArgumentContainer}.
   */
  public static TypeArgumentContainer turnsToTypeArgumentContainer(
      final Node node, final NodeFactory nodeFactory,
      final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, TypeArgumentContainer.class, nodeFactory,
        nodeMerger);
  }
}
