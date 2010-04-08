/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.idl.parser;

import org.ow2.mind.idl.jtb.syntaxtree.AbstractDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.AbstractDirectDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.AdditiveExpression;
import org.ow2.mind.idl.jtb.syntaxtree.AdditiveOperation;
import org.ow2.mind.idl.jtb.syntaxtree.AndExpression;
import org.ow2.mind.idl.jtb.syntaxtree.Annotation;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationAnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationParameters;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationValuePair;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationValuePairs;
import org.ow2.mind.idl.jtb.syntaxtree.Annotations;
import org.ow2.mind.idl.jtb.syntaxtree.ArrayAnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.ArraySpecification;
import org.ow2.mind.idl.jtb.syntaxtree.BooleanValue;
import org.ow2.mind.idl.jtb.syntaxtree.CastExpression;
import org.ow2.mind.idl.jtb.syntaxtree.ConstantDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.ConstantExpression;
import org.ow2.mind.idl.jtb.syntaxtree.Declarator;
import org.ow2.mind.idl.jtb.syntaxtree.Declarators;
import org.ow2.mind.idl.jtb.syntaxtree.DirectDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.EnumDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.EnumMember;
import org.ow2.mind.idl.jtb.syntaxtree.EnumMemberList;
import org.ow2.mind.idl.jtb.syntaxtree.EnumReference;
import org.ow2.mind.idl.jtb.syntaxtree.EnumSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.EnumValue;
import org.ow2.mind.idl.jtb.syntaxtree.FullyQualifiedName;
import org.ow2.mind.idl.jtb.syntaxtree.IDTFile;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;
import org.ow2.mind.idl.jtb.syntaxtree.IncludeDirective;
import org.ow2.mind.idl.jtb.syntaxtree.IntegerValue;
import org.ow2.mind.idl.jtb.syntaxtree.InterfaceBody;
import org.ow2.mind.idl.jtb.syntaxtree.InterfaceDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.InterfaceInheritanceSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.Literal;
import org.ow2.mind.idl.jtb.syntaxtree.LogicalAndExpression;
import org.ow2.mind.idl.jtb.syntaxtree.LogicalOrExpression;
import org.ow2.mind.idl.jtb.syntaxtree.MethodDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.MulExpression;
import org.ow2.mind.idl.jtb.syntaxtree.MulOperation;
import org.ow2.mind.idl.jtb.syntaxtree.NodeList;
import org.ow2.mind.idl.jtb.syntaxtree.NodeListOptional;
import org.ow2.mind.idl.jtb.syntaxtree.NodeOptional;
import org.ow2.mind.idl.jtb.syntaxtree.NodeSequence;
import org.ow2.mind.idl.jtb.syntaxtree.NodeToken;
import org.ow2.mind.idl.jtb.syntaxtree.NullValue;
import org.ow2.mind.idl.jtb.syntaxtree.OrExpression;
import org.ow2.mind.idl.jtb.syntaxtree.Parameter;
import org.ow2.mind.idl.jtb.syntaxtree.ParameterList;
import org.ow2.mind.idl.jtb.syntaxtree.ParameterQualifier;
import org.ow2.mind.idl.jtb.syntaxtree.Parameters;
import org.ow2.mind.idl.jtb.syntaxtree.PointerSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.PrimaryExpression;
import org.ow2.mind.idl.jtb.syntaxtree.QualifiedTypeSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.QualifierPointerSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.ShiftExpression;
import org.ow2.mind.idl.jtb.syntaxtree.ShiftOperation;
import org.ow2.mind.idl.jtb.syntaxtree.StringValue;
import org.ow2.mind.idl.jtb.syntaxtree.StructMember;
import org.ow2.mind.idl.jtb.syntaxtree.StructMemberList;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnion;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionReference;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeDefName;
import org.ow2.mind.idl.jtb.syntaxtree.TypeDefSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.TypeQualifier;
import org.ow2.mind.idl.jtb.syntaxtree.TypeSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeSpecifiers;
import org.ow2.mind.idl.jtb.syntaxtree.UnaryExpression;
import org.ow2.mind.idl.jtb.syntaxtree.UnaryOperation;
import org.ow2.mind.idl.jtb.syntaxtree.XorExpression;
import org.ow2.mind.idl.jtb.visitor.GJNoArguDepthFirst;

public class EndTokenVisitor extends GJNoArguDepthFirst<NodeToken> {

  // ---------------------------------------------------------------------------
  // Generic nodes
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final NodeList n) {
    for (int i = n.size() - 1; i >= 0; i--) {
      final NodeToken t = n.elementAt(i).accept(this);
      if (t != null) return t;
    }
    return null;
  }

  @Override
  public NodeToken visit(final NodeSequence n) {
    for (int i = n.size() - 1; i >= 0; i--) {
      final NodeToken t = n.elementAt(i).accept(this);
      if (t != null) return t;
    }
    return null;
  }

  @Override
  public NodeToken visit(final NodeListOptional n) {
    for (int i = n.size() - 1; i >= 0; i--) {
      final NodeToken t = n.elementAt(i).accept(this);
      if (t != null) return t;
    }
    return null;
  }

  @Override
  public NodeToken visit(final NodeOptional n) {
    if (n.present()) {
      return n.node.accept(this);
    }
    return null;
  }

  @Override
  public NodeToken visit(final NodeToken n) {
    return n;
  }

  // ---------------------------------------------------------------------------
  // File level grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final ITFFile n) {
    return n.f4;
  }

  @Override
  public NodeToken visit(final IDTFile n) {
    return n.f13;
  }

  @Override
  public NodeToken visit(final IncludeDirective n) {
    return n.f3;
  }

  @Override
  public NodeToken visit(final ConstantDefinition n) {
    return n.f3;
  }

  // ---------------------------------------------------------------------------
  // Type Definition grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final TypeDefinition n) {
    return n.f1;
  }

  @Override
  public NodeToken visit(final TypeDefSpecification n) {
    return n.f2.accept(this);
  }

  @Override
  public NodeToken visit(final QualifiedTypeSpecification n) {
    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final TypeQualifier n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final TypeSpecification n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final TypeDefName n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final TypeSpecifiers n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final StructOrUnionSpecification n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final StructOrUnionDefinition n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final StructOrUnionReference n) {
    return n.f1;
  }

  @Override
  public NodeToken visit(final StructOrUnion n) {
    return n.f0.accept(this);
  };

  @Override
  public NodeToken visit(final StructMemberList n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final StructMember n) {
    return n.f4;
  }

  @Override
  public NodeToken visit(final EnumSpecification n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final EnumDefinition n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final EnumReference n) {
    return n.f1;
  }

  @Override
  public NodeToken visit(final EnumMemberList n) {
    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final EnumMember n) {
    final NodeToken t = n.f2.accept(this);
    if (t != null) return t;

    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final EnumValue n) {
    return n.f1.accept(this);
  }

  // ---------------------------------------------------------------------------
  // Declarators grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final Declarators n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final Declarator n) {
    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final PointerSpecification n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final QualifierPointerSpecification n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final DirectDeclarator n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final ArraySpecification n) {
    return n.f2;
  }

  @Override
  public NodeToken visit(final AbstractDeclarator n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AbstractDirectDeclarator n) {
    return n.f0.accept(this);
  }

  // ---------------------------------------------------------------------------
  // Interface Definition grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final InterfaceDefinition n) {
    final NodeToken t = n.f4.accept(this);
    if (t != null) return t;

    return n.f3.accept(this);
  }

  @Override
  public NodeToken visit(final InterfaceInheritanceSpecification n) {
    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final InterfaceBody n) {
    return n.f2.accept(this);
  }

  @Override
  public NodeToken visit(final MethodDefinition n) {
    return n.f4.accept(this);
  }

  @Override
  public NodeToken visit(final Parameters n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final ParameterList n) {
    NodeToken t = n.f2.accept(this);
    if (t != null) return t;

    t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final Parameter n) {
    final NodeToken t = n.f3.accept(this);
    if (t != null) return t;

    return n.f2.accept(this);
  }

  @Override
  public NodeToken visit(final ParameterQualifier n) {
    return n.f0.accept(this);
  }

  // ---------------------------------------------------------------------------
  // Expression grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final ConstantExpression n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final LogicalOrExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final LogicalAndExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final OrExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final XorExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AndExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final ShiftExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AdditiveExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final MulExpression n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final CastExpression n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final UnaryExpression n) {
    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final PrimaryExpression n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final Literal n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final ShiftOperation n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AdditiveOperation n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final MulOperation n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final UnaryOperation n) {
    return n.f0.accept(this);
  }

  // ---------------------------------------------------------------------------
  // Annotation grammar
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final Annotations n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final Annotation n) {
    final NodeToken t = n.f2.accept(this);
    if (t != null) return t;

    return n.f1.accept(this);
  }

  @Override
  public NodeToken visit(final AnnotationParameters n) {
    return n.f2;
  }

  @Override
  public NodeToken visit(final AnnotationValuePairs n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AnnotationValuePair n) {
    return n.f2.accept(this);
  }

  @Override
  public NodeToken visit(final AnnotationValue n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final AnnotationAnnotationValue n) {
    return n.f0.accept(this);
  }

  @Override
  public NodeToken visit(final ArrayAnnotationValue n) {
    return n.f2;
  }

  @Override
  public NodeToken visit(final StringValue n) {
    return n.f0;
  }

  @Override
  public NodeToken visit(final IntegerValue n) {
    return n.f1;
  }

  @Override
  public NodeToken visit(final BooleanValue n) {
    return n.f0;
  }

  @Override
  public NodeToken visit(final NullValue n) {
    return n.f0;
  }

  // ---------------------------------------------------------------------------
  // FQN
  // ---------------------------------------------------------------------------

  @Override
  public NodeToken visit(final FullyQualifiedName n) {
    final NodeToken t = n.f1.accept(this);
    if (t != null) return t;

    return n.f0.accept(this);
  }

}
