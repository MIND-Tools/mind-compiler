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

package org.ow2.mind;

import static junit.framework.Assert.fail;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.ow2.mind.annotation.ast.AnnotationArgument;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;
import org.testng.annotations.BeforeMethod;

public class AbstractTestcase {
  NodeFactory nodeFactory;

  @BeforeMethod(alwaysRun = true)
  public void setUpNodeFactory() {
    nodeFactory = new NodeFactoryImpl();
  }

  protected AnnotationNode newAnnotationNode(final String type,
      final AnnotationArgument... args) {
    final AnnotationNode annoAST = newNode("annotation", AnnotationNode.class);
    annoAST.setType(type);
    for (final AnnotationArgument arg : args) {
      annoAST.addAnnotationArgument(arg);
    }
    return annoAST;
  }

  protected AnnotationArgument newAnnotationArgument(final String name,
      final Value value) {
    final AnnotationArgument arg = newNode("annotationArgument",
        AnnotationArgument.class);
    arg.setName(name);
    arg.setValue(value);
    return arg;
  }

  protected Value newAnnotationValue(final String type,
      final AnnotationArgument... args) {
    final AnnotationNode annoAST = newNode("annotation", AnnotationNode.class,
        Value.class);
    annoAST.setType(type);
    for (final AnnotationArgument arg : args) {
      annoAST.addAnnotationArgument(arg);
    }
    return (Value) annoAST;
  }

  protected NumberLiteral newNumberLiteral(final Number value) {
    final NumberLiteral n = newNode("value", NumberLiteral.class);
    n.setValue(value.toString());
    return n;
  }

  protected StringLiteral newStringLiteral(final String value) {
    final StringLiteral n = newNode("value", StringLiteral.class);
    n.setValue("\"" + value + "\"");
    return n;
  }

  protected Array newArray(final Value... values) {
    final Array a = newNode("value", Array.class);
    for (final Value value : values) {
      a.addValue(value);
    }
    return a;
  }

  protected <T extends Node> T newNode(final String type,
      final Class<T> nodeItf, final Class<?>... otherItfs) {
    try {
      Node node;
      if (otherItfs == null || otherItfs.length == 0) {
        node = nodeFactory.newNode(type, nodeItf.getName());
      } else {
        final String[] names = new String[otherItfs.length + 1];
        names[0] = nodeItf.getName();
        for (int i = 0; i < otherItfs.length; i++) {
          names[i + 1] = otherItfs[i].getName();
        }
        node = nodeFactory.newNode(type, names);
      }
      return nodeItf.cast(node);
    } catch (final ClassNotFoundException e) {
      fail(e.getMessage());
      return null;
    }
  }
}
