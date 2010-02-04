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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.adl.annotation;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationTarget;

/**
 * Enumeration of {@link AnnotationTarget} for ADL files.
 * 
 * @see Annotation#getAnnotationTargets()
 */
public enum ADLAnnotationTarget implements AnnotationTarget {

  /** {@link AnnotationTarget} suitable for {@link Import} nodes */
  IMPORT {
    public boolean isValidTarget(final Node target) {
      return target instanceof Import;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Definition} nodes */
  DEFINITION {
    public boolean isValidTarget(final Node target) {
      return target instanceof Definition;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Interface} nodes */
  INTERFACE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Interface;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Attribute} nodes */
  ATTRIBUTE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Attribute;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Component} nodes */
  COMPONENT {
    public boolean isValidTarget(final Node target) {
      return target instanceof Component;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Data} nodes */
  DATA {
    public boolean isValidTarget(final Node target) {
      return target instanceof Data;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Source} nodes */
  SOURCE {
    public boolean isValidTarget(final Node target) {
      return target instanceof Source;
    }
  },

  /** {@link AnnotationTarget} suitable for {@link Binding} nodes */
  BINDING {
    public boolean isValidTarget(final Node target) {
      return target instanceof Binding;
    }
  }
}
