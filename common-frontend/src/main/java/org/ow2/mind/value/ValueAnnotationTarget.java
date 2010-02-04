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

package org.ow2.mind.value;

import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.AnnotationTarget;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

public enum ValueAnnotationTarget implements AnnotationTarget {

  /** {@link AnnotationTarget} suitable for {@link NumberLiteral} nodes */
  NUMBER_LITERAL {
    public boolean isValidTarget(final Node target) {
      return target instanceof NumberLiteral;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link StringLiteral} nodes */
  STRING_LITERAL {
    public boolean isValidTarget(final Node target) {
      return target instanceof StringLiteral;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Array} nodes */
  ARRAY {
    public boolean isValidTarget(final Node target) {
      return target instanceof Array;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Reference} nodes */
  REFERENCE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Reference;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Value} nodes */
  VALUE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Value;
    }
  };
}
