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

package org.ow2.mind.value.ast;

import static org.ow2.mind.CommonASTHelper.newNode;

import org.objectweb.fractal.adl.NodeFactory;

public final class ValueASTHelper {
  private ValueASTHelper() {
  }

  public static int getValue(final NumberLiteral intValue) {
    return Integer.parseInt(intValue.getValue());
  }

  public static String getValue(final StringLiteral strValue) {
    return strValue.getValue();
  }

  public static boolean getValue(final BooleanLiteral boolValue) {
    return boolValue.getValue() != null
        && boolValue.getValue().equals(BooleanLiteral.TRUE);
  }

  /**
   * Create a new {@link NumberLiteral} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param value the {@link NumberLiteral#getValue() literal value}.
   * @return a new {@link NumberLiteral} node.
   */
  public static NumberLiteral newNumberLiteral(final NodeFactory nodeFactory,
      final int value) {
    final NumberLiteral literal = newNode(nodeFactory, "value",
        NumberLiteral.class);
    literal.setValue(Integer.toString(value));
    return literal;
  }

  /**
   * Create a new {@link StringLiteral} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param value the {@link StringLiteral#getValue() literal value}.
   * @return a new {@link StringLiteral} node.
   */
  public static StringLiteral newStringLiteral(final NodeFactory nodeFactory,
      final String value) {
    final StringLiteral literal = newNode(nodeFactory, "value",
        StringLiteral.class);
    literal.setValue("\"" + value + "\"");
    return literal;
  }

  /**
   * Create a new {@link BooleanLiteral} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param value the {@link BooleanLiteral#getValue() literal value}.
   * @return a new {@link BooleanLiteral} node.
   */
  public static BooleanLiteral newBooleanLiteral(final NodeFactory nodeFactory,
      final boolean value) {
    final BooleanLiteral literal = newNode(nodeFactory, "value",
        BooleanLiteral.class);
    literal.setValue((value) ? BooleanLiteral.TRUE : BooleanLiteral.FALSE);
    return literal;
  }

  /**
   * Create a new {@link NullLiteral} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link NullLiteral} node.
   */
  public static NullLiteral newNullLiteral(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "value", NullLiteral.class);
  }

  /**
   * Create a new {@link Reference} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param ref the value of the {@link Reference#getRef() referenced value}.
   * @return a new {@link Reference} node.
   */
  public static Reference newReference(final NodeFactory nodeFactory,
      final String ref) {
    final Reference literal = newNode(nodeFactory, "value", Reference.class);
    literal.setRef(ref);
    return literal;
  }

  /**
   * Create a new {@link Array} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link Array} node.
   */
  public static Array newArray(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "value", Array.class);
  }

}
