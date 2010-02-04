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

package org.ow2.mind.adl.parameter.ast;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.ast.ASTHelper;

/**
 * Helper methods for parameter AST nodes.
 */
public final class ParameterASTHelper {
  private ParameterASTHelper() {
  }

  /**
   * The name of the decoration used to mark used Parameter nodes. This
   * decoration can be used to check if a parameter is actually used by a
   * definition.
   * 
   * @see #setUsedFormalParameter(FormalParameter)
   * @see #isUsedFormalParameter(FormalParameter)
   */
  public static final String USED_PARAMETER_DECORATION_NAME = "used-import";

  /**
   * Mark the given parameter as used.
   * 
   * @param formalParameter an parameter node.
   * @see #USED_PARAMETER_DECORATION_NAME
   * @see #isUsedFormalParameter(FormalParameter)
   */
  public static void setUsedFormalParameter(
      final FormalParameter formalParameter) {
    formalParameter.astSetDecoration(USED_PARAMETER_DECORATION_NAME,
        Boolean.TRUE);
  }

  /**
   * Returns <code>true</code> if the given parameter is marked as used.
   * 
   * @param formalParameter a parameter node.
   * @return <code>true</code> if the given parameter is marked as used.
   * @see #USED_PARAMETER_DECORATION_NAME
   * @see #setUsedFormalParameter(FormalParameter)
   */
  public static boolean isUsedFormalParameter(
      final FormalParameter formalParameter) {
    final Boolean b = (Boolean) formalParameter
        .astGetDecoration(USED_PARAMETER_DECORATION_NAME);
    return (b != null) && b;
  }

  /**
   * Create a new {@link FormalParameter} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the value of the {@link FormalParameter#getName() parameter
   *          name}.
   * @return a new {@link FormalParameter} node.
   */
  public static FormalParameter newFormalParameter(
      final NodeFactory nodeFactory, final String name) {
    final FormalParameter para = ASTHelper.newNode(nodeFactory,
        "formalParameter", FormalParameter.class);
    para.setName(name);
    return para;
  }

  /**
   * Create a new {@link Argument} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the value of the {@link Argument#getName() argument name}.
   * @return a new {@link Argument} node.
   */
  public static Argument newArgument(final NodeFactory nodeFactory,
      final String name) {
    final Argument arg = ASTHelper.newNode(nodeFactory, "argument",
        Argument.class);
    arg.setName(name);
    return arg;
  }

  /**
   * Transforms the given node to an {@link ArgumentContainer}. If the node
   * already implements the {@link ArgumentContainer} interface, this method
   * simply cast it. Otherwise this method use the given node factory and node
   * merger to create a copy of the given node that implements the
   * {@link ArgumentContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link ArgumentContainer}, or a
   *         copy of the given node that implements {@link ArgumentContainer}.
   */
  public static ArgumentContainer turnsToArgumentContainer(final Node node,
      final NodeFactory nodeFactory, final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, ArgumentContainer.class, nodeFactory,
        nodeMerger);
  }

  /**
   * Transforms the given node to an {@link FormalParameterContainer}. If the
   * node already implements the {@link FormalParameterContainer} interface,
   * this method simply cast it. Otherwise this method use the given node
   * factory and node merger to create a copy of the given node that implements
   * the {@link FormalParameterContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link FormalParameterContainer},
   *         or a copy of the given node that implements
   *         {@link FormalParameterContainer}.
   */
  public static FormalParameterContainer turnsToParamContainer(final Node node,
      final NodeFactory nodeFactory, final NodeMerger nodeMerger) {
    return ASTHelper.turnsTo(node, FormalParameterContainer.class, nodeFactory,
        nodeMerger);
  }

  public static enum ParameterType {
    INTEGER, STRING;
  }

  public static final String INFERRED_ARGUMENT_TYPE_DECORATION_NAME = "inferred-type";

  public static ParameterType setInferredParameterType(
      final FormalParameter parameter, final ParameterType type) {
    final ParameterType previousType = getInferredParameterType(parameter);
    parameter.astSetDecoration(INFERRED_ARGUMENT_TYPE_DECORATION_NAME, type);
    return previousType;
  }

  public static ParameterType getInferredParameterType(
      final FormalParameter parameter) {
    return (ParameterType) parameter
        .astGetDecoration(INFERRED_ARGUMENT_TYPE_DECORATION_NAME);
  }
}
