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

package org.ow2.mind.idl.annotation;

import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.AnnotationTarget;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;
import org.ow2.mind.idl.ast.Parameter;
import org.ow2.mind.idl.ast.Type;

public enum IDLAnnotationTarget implements AnnotationTarget {
  /** {@link AnnotationTarget} suitable for {@link IDL} nodes */
  IDL {
    public boolean isValidTarget(final Node target) {
      return target instanceof IDL;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Type} nodes */
  TYPE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Type;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link InterfaceDefinition} nodes */
  INTERFACE {
    public boolean isValidTarget(final Node target) {
      return target instanceof InterfaceDefinition;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Method} nodes */
  METHOD {
    public boolean isValidTarget(final Node target) {
      return target instanceof Method;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Parameter} nodes */
  PARAMETER {
    public boolean isValidTarget(final Node target) {
      return target instanceof Parameter;
    }
  }
}
