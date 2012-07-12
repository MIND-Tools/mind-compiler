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

import static org.ow2.mind.CommonASTHelper.newNode;
import static org.ow2.mind.CommonASTHelper.turnsTo;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.value.ast.NullLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

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
    final FormalParameter para = newNode(nodeFactory, "formalParameter",
        FormalParameter.class);
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
    final Argument arg = newNode(nodeFactory, "argument", Argument.class);
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
    return turnsTo(node, ArgumentContainer.class, nodeFactory, nodeMerger);
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
    return turnsTo(node, FormalParameterContainer.class, nodeFactory,
        nodeMerger);
  }

  public static class ParameterType {

    public static final ParameterType    STRING        = new ParameterType(
                                                           "string");
    public static final ParameterType    INT           = new ParameterType(
                                                           "int");
    public static final ParameterType    INT8_T        = new ParameterType(
                                                           "int8_t");
    public static final ParameterType    UINT8_T       = new ParameterType(
                                                           "uint8_t");
    public static final ParameterType    INT16_T       = new ParameterType(
                                                           "int16_t");
    public static final ParameterType    UINT16_T      = new ParameterType(
                                                           "uint16_t");
    public static final ParameterType    INT32_T       = new ParameterType(
                                                           "int32_t");
    public static final ParameterType    UINT32_T      = new ParameterType(
                                                           "uint32_t");
    public static final ParameterType    INT64_T       = new ParameterType(
                                                           "int64_t");
    public static final ParameterType    UINT64_T      = new ParameterType(
                                                           "uint64_t");
    public static final ParameterType    INTPTR_T      = new ParameterType(
                                                           "intptr_t");
    public static final ParameterType    UINTPTR_T     = new ParameterType(
                                                           "uintptr_t");

    private static final ParameterType[] INTEGER_TYPES = {INT, INT8_T, UINT8_T,
                                                           INT16_T, UINT16_T,
                                                           INT32_T, UINT32_T,
                                                           INT64_T, UINT64_T,
                                                           INTPTR_T, UINTPTR_T};

    private final String                 idtFile;
    private final String                 cType;

    private ParameterType(final String cType) {
      this(null, cType);
    }

    private ParameterType(final String idt, final String cType) {
      this.idtFile = idt;
      this.cType = cType;
    }

    public boolean isIntegerType() {
      for (final ParameterType t : INTEGER_TYPES) {
        if (this == t) return true;
      }
      return false;
    }

    public boolean isStringType() {
      return this == STRING;
    }

    public boolean isPrimitiveType() {
      return isIntegerType() || isStringType();
    }

    public boolean isComplexType() {
      return !isPrimitiveType();
    }

    public boolean isCompatible(final ParameterType type) {
      if (this.equals(type)) return true;
      if (this == STRING) return type == STRING;
      if (this.isIntegerType()) return type.isIntegerType();
      if (this.isComplexType()) return true;
      return false;
    }

    public boolean isCompatible(final Value value) {
      if (this.isStringType())
        return value instanceof StringLiteral || value instanceof NullLiteral;

      if (this.isIntegerType()) return value instanceof NumberLiteral;

      if (this.isComplexType())
      // Cannot check complex types, so assumes that value is compatible. If
      // not the C compiler will raise an error
        return true;

      return false;
    }

    public String getIdtFile() {
      return idtFile;
    }

    public String getCType() {
      return cType;
    }

    @Override
    public int hashCode() {
      int hashcode = cType.hashCode();
      if (idtFile != null) {
        hashcode = hashcode * 37 + idtFile.hashCode();
      }
      return hashcode;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof ParameterType)) return false;

      final ParameterType other = (ParameterType) obj;
      if (this.idtFile == null) {
        if (other.idtFile != null) return false;
      } else if (!this.idtFile.equals(other.idtFile)) return false;
      return this.cType.equals(other.cType);
    }

    public static ParameterType fromCType(final String idt, final String cType) {
      if (idt == null) {
        for (final ParameterType type : INTEGER_TYPES) {
          if (type.cType.equals(cType)) return type;
        }
        if (cType.equals(STRING.cType)) return STRING;
      }

      return new ParameterType(idt, cType);
    }
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
