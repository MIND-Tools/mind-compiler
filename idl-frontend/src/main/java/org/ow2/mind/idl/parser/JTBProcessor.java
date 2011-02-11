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

package org.ow2.mind.idl.parser;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.objectweb.fractal.adl.NodeUtil.cloneTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.annotation.ast.AnnotationArgument;
import org.ow2.mind.annotation.ast.AnnotationContainer;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.idl.ast.ArrayOf;
import org.ow2.mind.idl.ast.BinaryOperation;
import org.ow2.mind.idl.ast.CastOperation;
import org.ow2.mind.idl.ast.ConstantDefinition;
import org.ow2.mind.idl.ast.ConstantExpression;
import org.ow2.mind.idl.ast.ConstantExpressionContainer;
import org.ow2.mind.idl.ast.EnumDefinition;
import org.ow2.mind.idl.ast.EnumMember;
import org.ow2.mind.idl.ast.EnumReference;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Literal;
import org.ow2.mind.idl.ast.Member;
import org.ow2.mind.idl.ast.MemberContainer;
import org.ow2.mind.idl.ast.Method;
import org.ow2.mind.idl.ast.Parameter;
import org.ow2.mind.idl.ast.PointerOf;
import org.ow2.mind.idl.ast.PrimitiveType;
import org.ow2.mind.idl.ast.SharedTypeDefinition;
import org.ow2.mind.idl.ast.StructDefinition;
import org.ow2.mind.idl.ast.StructReference;
import org.ow2.mind.idl.ast.Type;
import org.ow2.mind.idl.ast.TypeCollectionContainer;
import org.ow2.mind.idl.ast.TypeContainer;
import org.ow2.mind.idl.ast.TypeDefReference;
import org.ow2.mind.idl.ast.TypeDefinition;
import org.ow2.mind.idl.ast.TypeQualifier;
import org.ow2.mind.idl.ast.UnaryOperation;
import org.ow2.mind.idl.ast.UnionDefinition;
import org.ow2.mind.idl.ast.UnionReference;
import org.ow2.mind.idl.jtb.Parser;
import org.ow2.mind.idl.jtb.ParserConstants;
import org.ow2.mind.idl.jtb.syntaxtree.AbstractDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.AbstractDirectDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.AdditiveExpression;
import org.ow2.mind.idl.jtb.syntaxtree.AdditiveOperation;
import org.ow2.mind.idl.jtb.syntaxtree.AndExpression;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationAnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationParameters;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.AnnotationValuePair;
import org.ow2.mind.idl.jtb.syntaxtree.ArrayAnnotationValue;
import org.ow2.mind.idl.jtb.syntaxtree.ArraySpecification;
import org.ow2.mind.idl.jtb.syntaxtree.BooleanValue;
import org.ow2.mind.idl.jtb.syntaxtree.CastExpression;
import org.ow2.mind.idl.jtb.syntaxtree.Declarator;
import org.ow2.mind.idl.jtb.syntaxtree.Declarators;
import org.ow2.mind.idl.jtb.syntaxtree.DirectDeclarator;
import org.ow2.mind.idl.jtb.syntaxtree.EnumSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.FullyQualifiedName;
import org.ow2.mind.idl.jtb.syntaxtree.IDTFile;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;
import org.ow2.mind.idl.jtb.syntaxtree.IncludeDirective;
import org.ow2.mind.idl.jtb.syntaxtree.IntegerValue;
import org.ow2.mind.idl.jtb.syntaxtree.InterfaceInheritanceSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.LogicalAndExpression;
import org.ow2.mind.idl.jtb.syntaxtree.LogicalOrExpression;
import org.ow2.mind.idl.jtb.syntaxtree.MethodDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.MulExpression;
import org.ow2.mind.idl.jtb.syntaxtree.MulOperation;
import org.ow2.mind.idl.jtb.syntaxtree.NodeChoice;
import org.ow2.mind.idl.jtb.syntaxtree.NodeList;
import org.ow2.mind.idl.jtb.syntaxtree.NodeListOptional;
import org.ow2.mind.idl.jtb.syntaxtree.NodeSequence;
import org.ow2.mind.idl.jtb.syntaxtree.NodeToken;
import org.ow2.mind.idl.jtb.syntaxtree.NullValue;
import org.ow2.mind.idl.jtb.syntaxtree.OrExpression;
import org.ow2.mind.idl.jtb.syntaxtree.ParameterList;
import org.ow2.mind.idl.jtb.syntaxtree.ParameterQualifier;
import org.ow2.mind.idl.jtb.syntaxtree.PointerSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.PrimaryExpression;
import org.ow2.mind.idl.jtb.syntaxtree.QualifiedTypeSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.QualifierPointerSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.ShiftExpression;
import org.ow2.mind.idl.jtb.syntaxtree.ShiftOperation;
import org.ow2.mind.idl.jtb.syntaxtree.StringValue;
import org.ow2.mind.idl.jtb.syntaxtree.StructMember;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionDefinition;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionReference;
import org.ow2.mind.idl.jtb.syntaxtree.StructOrUnionSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeDefName;
import org.ow2.mind.idl.jtb.syntaxtree.TypeDefSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeSpecification;
import org.ow2.mind.idl.jtb.syntaxtree.TypeSpecifiers;
import org.ow2.mind.idl.jtb.syntaxtree.UnaryExpression;
import org.ow2.mind.idl.jtb.syntaxtree.XorExpression;
import org.ow2.mind.idl.jtb.visitor.GJDepthFirst;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.BooleanLiteral;
import org.ow2.mind.value.ast.MultipleValueContainer;
import org.ow2.mind.value.ast.NullLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.SingleValueContainer;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Translate the JTB AST of an IDL file into a "fractal-adl like" AST.
 */
public class JTBProcessor extends GJDepthFirst<Object, Node>
    implements
      ParserConstants {

  public static final String      IDL_DTD           = "idl-dtd";

  private final XMLNodeFactory    nodeFactory;
  private final String            idlDtd;
  private final BeginTokenVisitor beginTokenVisitor = new BeginTokenVisitor();
  private final EndTokenVisitor   endTokenVisitor   = new EndTokenVisitor();

  private String                  filename;

  @Inject
  protected JTBProcessor(final XMLNodeFactory nodeFactory,
      @Named(IDL_DTD) final String idlDtd) {
    this.nodeFactory = nodeFactory;
    this.idlDtd = idlDtd;
    try {
      nodeFactory.checkDTD(idlDtd);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Error in dtd file '" + idlDtd + "'");
    }
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(final String filename) {
    this.filename = filename;
  }

  /**
   * Translate the given JTB AST into a "fractal-adl like" AST.
   * 
   * @param fileContent a JTB AST obtained by the {@link Parser ADL Parser}.
   * @return the top level {@link InterfaceDefinition} corresponding to the
   *         given JTB AST.
   */
  public InterfaceDefinition toInterfaceDefinition(final ITFFile fileContent) {
    return (InterfaceDefinition) visit(fileContent, null);
  }

  /**
   * Translate the given JTB AST into a "fractal-adl like" AST.
   * 
   * @param fileContent a JTB AST obtained by the {@link Parser ADL Parser}.
   * @return the top level {@link SharedTypeDefinition} corresponding to the
   *         given JTB AST.
   */
  public SharedTypeDefinition toSharedTypeDefinition(final IDTFile fileContent) {
    return (SharedTypeDefinition) visit(fileContent, null);
  }

  private Node newNode(final String name) {
    return newNode(name, null);
  }

  private Node newNode(final String name, final NodeToken source) {
    return newNode(name, source, source);
  }

  private Node newNode(final String name, final NodeToken beginToken,
      final NodeToken endToken) {
    Node node;
    try {
      node = nodeFactory.newXMLNode(idlDtd, name);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to create node");
    }
    setSource(node, beginToken, endToken);

    return node;
  }

  private Node newNode(final String name,
      final org.ow2.mind.idl.jtb.syntaxtree.Node syntaxNode) {
    return newNode(name, syntaxNode.accept(beginTokenVisitor),
        syntaxNode.accept(endTokenVisitor));
  }

  private void setSource(final Node node, final NodeToken source) {
    setSource(node, source, source);
  }

  private void setSource(final Node node, final NodeToken beginToken,
      final NodeToken endToken) {
    if (beginToken == null)
      node.astSetSource(filename);
    else if (endToken == null)
      node.astSetSource(NodeErrorLocator.fullLocation(filename,
          beginToken.beginLine, beginToken.endLine, beginToken.beginColumn,
          beginToken.endColumn));
    else
      node.astSetSource(NodeErrorLocator.fullLocation(filename,
          beginToken.beginLine, endToken.endLine, beginToken.beginColumn,
          endToken.endColumn));

  }

  private void setSource(final Node node,
      final org.ow2.mind.idl.jtb.syntaxtree.Node syntaxNode) {
    setSource(node, syntaxNode.accept(beginTokenVisitor),
        syntaxNode.accept(endTokenVisitor));
  }

  private void copySource(final Node node, final Node from) {
    if (from == null)
      node.astSetSource(filename);
    else
      node.astSetSource(from.astGetSource());
  }

  private void setName(final Node argu, final String name) {
    final Map<String, String> m = argu.astGetAttributes();
    m.put("name", name);
    argu.astSetAttributes(m);
  }

  // ---------------------------------------------------------------------------
  // File level grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(final ITFFile n, final Node argu) {
    assert argu == null;

    final InterfaceDefinition itf = (InterfaceDefinition) newNode("itf", n);

    // process include directives
    n.f0.accept(this, itf);

    // process type and constant definitions
    n.f1.accept(this, itf);

    // process interface definition
    n.f2.accept(this, itf);

    return itf;
  }

  @Override
  public Object visit(final IDTFile n, final Node argu) {
    assert argu == null;

    final SharedTypeDefinition idtFile = (SharedTypeDefinition) newNode("idt",
        n);

    // process include directives
    n.f8.accept(this, idtFile);

    // process type and constant definitions
    n.f9.accept(this, idtFile);

    return idtFile;
  }

  @Override
  public Object visit(final IncludeDirective n, final Node argu) {
    final IncludeContainer container = castNodeError(argu,
        IncludeContainer.class);

    final Include include = (Include) newNode("include", n);

    include.setPath(n.f2.tokenImage);

    container.addInclude(include);

    return include;
  }

  // ---------------------------------------------------------------------------
  // Type Definition grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.TypeDefinition n,
      final Node argu) {
    assert argu != null;
    final TypeCollectionContainer container = castNodeError(argu,
        TypeCollectionContainer.class);

    final Object def = n.f0.accept(this, argu);

    if (def instanceof List<?>) {
      for (final Object d : (List<?>) def) {
        assert d instanceof Type;
        container.addType((Type) d);
      }
    } else {
      assert def instanceof Type;
      container.addType((Type) def);
    }

    return null;
  }

  @Override
  public Object visit(final TypeDefSpecification n, final Node argu) {

    final TypeDefinition typedef = (TypeDefinition) newNode("typedef", n);

    // process qualified type specification
    n.f1.accept(this, typedef);

    // process type qualifiers
    n.f1.f0.accept(this, typedef);

    // process declarators
    return n.f2.accept(this, typedef);
  }

  @Override
  public Object visit(final QualifiedTypeSpecification n, final Node argu) {
    assert argu != null;
    final TypeContainer typeContainer = castNodeError(argu, TypeContainer.class);

    // process type specification
    final Type type = castNodeError(n.f1.accept(this, argu), Type.class);

    typeContainer.setType(type);
    return type;
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.TypeQualifier n,
      final Node argu) {
    assert argu != null;
    final TypeQualifier typeQualifier = castNodeError(argu, TypeQualifier.class);
    if (((NodeToken) n.f0.choice).kind == CONST) {
      typeQualifier.setIsConst(TypeQualifier.TRUE);
    } else {
      assert ((NodeToken) n.f0.choice).kind == VOLATILE;
      typeQualifier.setIsVolatile(TypeQualifier.TRUE);
    }

    return argu;
  }

  @Override
  public Object visit(final TypeSpecification n, final Node argu) {
    return n.f0.accept(this, argu);
  }

  @Override
  public Object visit(final TypeDefName n, final Node argu) {
    assert argu != null;
    final TypeContainer typeContainer = castNodeError(argu, TypeContainer.class);

    final TypeDefReference typeDefReference = (TypeDefReference) newNode(
        "typedefRef", n);
    typeDefReference.setName(fullyQualifiedName(n.f0));

    typeContainer.setType(typeDefReference);
    return typeDefReference;
  }

  @Override
  public Object visit(final TypeSpecifiers n, final Node argu) {

    final PrimitiveType primitiveType = (PrimitiveType) newNode(
        "primitiveType", n);

    final StringBuilder sb = new StringBuilder();
    final Iterator<org.ow2.mind.idl.jtb.syntaxtree.Node> iter = n.f0.nodes
        .iterator();
    while (iter.hasNext()) {
      final org.ow2.mind.idl.jtb.syntaxtree.Node syntaxNode = iter.next();
      sb.append(((NodeToken) ((NodeChoice) syntaxNode).choice).tokenImage);
      if (iter.hasNext()) sb.append(" ");
    }
    primitiveType.setName(sb.toString());

    return primitiveType;
  }

  @Override
  public Object visit(final StructOrUnionSpecification n, final Node argu) {
    return n.f0.accept(this, argu);
  }

  @Override
  public Object visit(final StructOrUnionDefinition n, final Node argu) {

    final Type type;

    // process StructOrUnion and identifier
    final NodeToken structOrUnion = (NodeToken) n.f0.f0.choice;
    if (structOrUnion.kind == STRUCT) {
      final StructDefinition struct = (StructDefinition) newNode("struct", n);
      if (n.f1.present()) struct.setName(((NodeToken) n.f1.node).tokenImage);
      type = struct;
    } else {
      assert structOrUnion.kind == UNION;
      final UnionDefinition union = (UnionDefinition) newNode("union", n);
      if (n.f1.present()) union.setName(((NodeToken) n.f1.node).tokenImage);
      type = union;
    }

    // process members
    n.f3.accept(this, type);

    return type;
  }

  @Override
  public Object visit(final StructOrUnionReference n, final Node argu) {

    final Type type;

    // process StructOrUnion and identifier
    final NodeToken structOrUnion = (NodeToken) n.f0.f0.choice;
    if (structOrUnion.kind == STRUCT) {
      final StructReference struct = (StructReference) newNode("structRef", n);
      struct.setName(n.f1.tokenImage);
      type = struct;
    } else {
      assert structOrUnion.kind == UNION;
      final UnionReference union = (UnionReference) newNode("unionRef", n);
      union.setName(n.f1.tokenImage);
      type = union;
    }

    return type;
  }

  @Override
  public Object visit(final StructMember n, final Node argu) {
    assert argu != null;
    final MemberContainer memberContainer = castNodeError(argu,
        MemberContainer.class);

    final Member member = (Member) newNode("member", n);

    // process annotations
    n.f0.accept(this, member);

    // process qualified type specification
    n.f1.accept(this, member);
    // process type qualifiers
    n.f1.f0.accept(this, member);

    // process declarators
    final List<?> members = (List<?>) n.f2.accept(this, member);

    // process bit-field size (if any)
    if (n.f3.present()) {
      n.f3.accept(this, member);
    }

    for (final Object m : members) {
      memberContainer.addMember((Member) m);
    }

    return null;
  }

  @Override
  public Object visit(final EnumSpecification n, final Node argu) {
    return n.f0.accept(this, argu);
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.EnumDefinition n,
      final Node argu) {

    // process StructOrUnion and identifier
    final EnumDefinition enummeration = (EnumDefinition) newNode("enum", n);
    if (n.f1.present())
      enummeration.setName(((NodeToken) n.f1.node).tokenImage);
    // process members
    n.f3.accept(this, enummeration);

    return enummeration;
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.EnumReference n,
      final Node argu) {

    // process StructOrUnion and identifier
    final EnumReference enummeration = (EnumReference) newNode("enumRef", n);
    enummeration.setName(n.f1.tokenImage);

    return enummeration;
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.EnumMember n,
      final Node argu) {
    assert argu != null;
    final EnumDefinition enummeration = castNodeError(argu,
        EnumDefinition.class);

    final EnumMember member = (EnumMember) newNode("enumMember", n);

    // process annotations
    n.f0.accept(this, member);

    // process member name
    member.setName(n.f1.tokenImage);

    // process value (if any)
    n.f2.accept(this, member);

    enummeration.addEnumMember(member);

    return null;
  }

  // ---------------------------------------------------------------------------
  // Declarators grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(final Declarators n, final Node argu) {
    assert argu != null;
    final TypeContainer container = castNodeError(argu, TypeContainer.class);
    final Type typeSpecifier = container.getType();

    final List<Node> result = new ArrayList<Node>();

    // visit first declarator
    TypeContainer n1 = cloneTree(container);
    n1.setType(typeSpecifier);
    n.f0.accept(this, n1);
    setSource(n1, n.f0);
    result.add(n1);

    // visit other declarators (if any)
    for (final org.ow2.mind.idl.jtb.syntaxtree.Node syntaxNode : n.f1.nodes) {
      n1 = cloneTree(container);
      n1.setType(cloneTypeSpecifier(typeSpecifier));
      syntaxNode.accept(this, n1);
      setSource(n1, ((NodeSequence) syntaxNode).elementAt(1));
      result.add(n1);
    }

    return result;
  }

  @Override
  public Object visit(final Declarator n, Node argu) {
    assert argu != null;

    // process pointers
    argu = (Node) n.f0.accept(this, argu);

    // process direct declarator
    argu = (Node) n.f1.accept(this, argu);

    return argu;
  }

  @Override
  public Object visit(final PointerSpecification n, Node argu) {
    assert argu != null;

    // process qualified pointer spec
    for (int i = 0; i < n.f0.size(); i++) {
      argu = (Node) n.f0.elementAt(i).accept(this, argu);
    }

    return argu;
  }

  @Override
  public Object visit(final QualifierPointerSpecification n, final Node argu) {
    assert argu != null;
    final TypeContainer typeContainer = castNodeError(argu, TypeContainer.class);

    final PointerOf pointerOf = (PointerOf) newNode("pointerOf", n);

    // process type qualifier
    n.f1.accept(this, pointerOf);

    // insert pointerOf node as direct sub-node of given typeContainer.
    pointerOf.setType(typeContainer.getType());
    typeContainer.setType(pointerOf);

    return argu;
  }

  @Override
  public Object visit(final DirectDeclarator n, Node argu) {
    assert argu != null;

    if (n.f0.choice instanceof NodeToken) {
      // direct declarator is an identifier
      final NodeToken id = (NodeToken) n.f0.choice;
      setName(argu, id.tokenImage);
      setSource(argu, id);

      // process array specifications in reverse order
      for (int i = n.f1.size() - 1; i >= 0; i--) {
        argu = (Node) n.f1.elementAt(i).accept(this, argu);
      }

    } else {
      // direct declarator is an inner declarator;
      assert n.f0.choice instanceof NodeSequence;

      // process array specifications first in reverse order
      for (int i = n.f1.size() - 1; i >= 0; i--) {
        argu = (Node) n.f1.elementAt(i).accept(this, argu);
      }

      argu = (Node) ((NodeSequence) n.f0.choice).elementAt(1)
          .accept(this, argu);
    }

    return argu;
  }

  @Override
  public Object visit(final AbstractDeclarator n, Node argu) {
    assert argu != null;

    // process pointers
    argu = (Node) n.f0.accept(this, argu);

    // process abstract direct declarator
    argu = (Node) n.f1.accept(this, argu);

    return argu;
  }

  @Override
  public Object visit(final AbstractDirectDeclarator n, Node argu) {
    assert argu != null;
    if (n.f0.which == 0) {
      final AbstractDeclarator abstractDeclarator = (AbstractDeclarator) ((NodeSequence) n.f0.choice)
          .elementAt(1);
      final NodeListOptional arraySpecification = (NodeListOptional) ((NodeSequence) n.f0.choice)
          .elementAt(3);

      // process array specifications first in reverse order
      for (int i = arraySpecification.size() - 1; i >= 0; i--) {
        argu = (Node) arraySpecification.elementAt(i).accept(this, argu);
      }

      argu = (Node) abstractDeclarator.accept(this, argu);
    } else {
      final NodeList arraySpecification = (NodeList) n.f0.choice;

      // process array specifications first in reverse order
      for (int i = arraySpecification.size() - 1; i >= 0; i--) {
        argu = (Node) arraySpecification.elementAt(i).accept(this, argu);
      }
    }

    return argu;
  }

  @Override
  public Object visit(final ArraySpecification n, final Node argu) {
    assert argu != null;
    final TypeContainer typeContainer = castNodeError(argu, TypeContainer.class);

    final ArrayOf arrayOf = (ArrayOf) newNode("arrayOf", n);

    // process array size (if any)
    n.f1.accept(this, arrayOf);

    // insert arrayOf node as direct sub-node of given typeContainer.
    arrayOf.setType(typeContainer.getType());
    typeContainer.setType(arrayOf);

    return argu;
  }

  // ---------------------------------------------------------------------------
  // Constant Definition grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(
      final org.ow2.mind.idl.jtb.syntaxtree.ConstantDefinition n,
      final Node argu) {
    assert argu != null;
    final TypeCollectionContainer container = castNodeError(argu,
        TypeCollectionContainer.class);

    final ConstantDefinition constDef = (ConstantDefinition) newNode(
        "constant", n);
    constDef.setName(n.f2.tokenImage);
    constDef.setValue(n.f3.tokenImage);

    container.addType(constDef);

    return constDef;
  }

  // ---------------------------------------------------------------------------
  // Interface Definition grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(
      final org.ow2.mind.idl.jtb.syntaxtree.InterfaceDefinition n,
      final Node argu) {
    assert argu != null;
    final InterfaceDefinition itfDef = castNodeError(argu,
        InterfaceDefinition.class);

    // process interface definition
    n.f0.accept(this, itfDef);

    setSource(itfDef, n);

    // process "unmanaged" qualifier
    if (n.f2.present()) {
      itfDef.setIsUnmanaged(InterfaceDefinition.TRUE);
    }

    // process name
    itfDef.setName(fullyQualifiedName(n.f3));

    // process inheritence specification
    n.f4.accept(this, itfDef);

    // process body
    n.f5.accept(this, itfDef);

    return itfDef;
  }

  @Override
  public Object visit(final InterfaceInheritanceSpecification n, final Node argu) {
    assert argu != null;
    final InterfaceDefinition itfDef = castNodeError(argu,
        InterfaceDefinition.class);

    itfDef.setExtends(fullyQualifiedName(n.f1));

    return itfDef;
  }

  @Override
  public Object visit(final MethodDefinition n, final Node argu) {
    assert argu != null;
    final InterfaceDefinition itfDef = castNodeError(argu,
        InterfaceDefinition.class);

    final Method method = (Method) newNode("method", n);

    // process annotations
    n.f0.accept(this, method);

    // set name
    method.setName(n.f3.tokenImage);

    // process return type
    n.f1.accept(this, method);
    // process type qualifiers
    n.f1.f0.accept(this, method);
    // process pointer specification
    n.f2.accept(this, method);

    // process parameters
    n.f4.accept(this, method);

    // check parameter names
    final Parameter[] parameters = method.getParameters();
    final Set<String> usedParamNames = new HashSet<String>(parameters.length);
    boolean hasNullParamName = false;
    for (final Parameter param : parameters) {
      final String paramName = param.getName();
      if (paramName != null)
        usedParamNames.add(paramName);
      else
        hasNullParamName = true;
    }

    if (hasNullParamName) {
      int i = 0;
      for (final Parameter param : parameters) {
        String paramName = param.getName();
        if (paramName == null) {
          // find a paramName that is not already used.
          do {
            paramName = "p" + (i++);
          } while (!usedParamNames.add(paramName));
          param.setName(paramName);
        }
      }
    }

    // add method in interface definition
    itfDef.addMethod(method);

    return method;
  }

  @Override
  public Object visit(final ParameterList n, final Node argu) {
    assert argu != null;
    final Method method = castNodeError(argu, Method.class);

    // process parameters
    n.f0.accept(this, argu);
    n.f1.accept(this, argu);

    // process ...
    if (n.f2.present()) {
      method.setVaArgs(Method.TRUE);
    }
    return method;
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.Parameter n,
      final Node argu) {
    assert argu != null;
    final Method method = castNodeError(argu, Method.class);

    final Parameter parameter = (Parameter) newNode("parameter", n);

    // process annotations
    n.f0.accept(this, parameter);

    // process qualifiers
    n.f1.accept(this, parameter);

    // process type specification
    n.f2.accept(this, parameter);
    // process type qualifiers
    n.f2.f0.accept(this, parameter);

    // process declarator
    n.f3.accept(this, parameter);

    // add parameter in method
    method.addParameter(parameter);

    return parameter;
  }

  @Override
  public Object visit(final ParameterQualifier n, final Node argu) {
    assert argu != null;
    final Parameter parameter = castNodeError(argu, Parameter.class);

    if (((NodeToken) n.f0.choice).kind == IN) {
      parameter.setIsIn(Parameter.TRUE);
    } else {
      assert ((NodeToken) n.f0.choice).kind == OUT;
      parameter.setIsOut(Parameter.TRUE);
    }

    return parameter;
  }

  // ---------------------------------------------------------------------------
  // Expression grammar
  // ---------------------------------------------------------------------------

  @Override
  public Object visit(
      final org.ow2.mind.idl.jtb.syntaxtree.ConstantExpression n,
      final Node argu) {
    assert argu != null;
    final ConstantExpressionContainer container = castNodeError(argu,
        ConstantExpressionContainer.class);

    final ConstantExpression expr = (ConstantExpression) n.f0
        .accept(this, argu);

    container.setConstantExpression(expr);

    return expr;
  }

  @Override
  public Object visit(final LogicalOrExpression n, final Node argu) {
    if (n.f1.present()) {
      return visitBinaryExpression(BinaryOperation.LOGICAL_OR, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final LogicalAndExpression n, final Node argu) {
    if (n.f1.present()) {
      return visitBinaryExpression(BinaryOperation.LOGICAL_AND, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final OrExpression n, final Node argu) {
    if (n.f1.present()) {
      return visitBinaryExpression(BinaryOperation.OR, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final XorExpression n, final Node argu) {
    if (n.f1.present()) {
      return visitBinaryExpression(BinaryOperation.XOR, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final AndExpression n, final Node argu) {
    if (n.f1.present()) {
      return visitBinaryExpression(BinaryOperation.AND, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final ShiftExpression n, final Node argu) {
    if (n.f1.present()) {
      final BinaryOperation expr = visitBinaryExpression(null, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);

      // process operand
      ((NodeSequence) n.f1.node).elementAt(0).accept(this, expr);

      return expr;
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final AdditiveExpression n, final Node argu) {
    if (n.f1.present()) {
      final BinaryOperation expr = visitBinaryExpression(null, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);

      // process operand
      ((NodeSequence) n.f1.node).elementAt(0).accept(this, expr);

      return expr;
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final MulExpression n, final Node argu) {
    if (n.f1.present()) {
      final BinaryOperation expr = visitBinaryExpression(null, n.f0,
          ((NodeSequence) n.f1.node).elementAt(1), n, argu);

      // process operand
      ((NodeSequence) n.f1.node).elementAt(0).accept(this, expr);

      return expr;
    } else {
      return n.f0.accept(this, argu);
    }
  }

  @Override
  public Object visit(final CastExpression n, final Node argu) {
    if (n.f0.choice instanceof UnaryExpression) {
      return n.f0.accept(this, argu);
    } else {
      final CastOperation expr = (CastOperation) newNode("castOperation", n);

      // process type specification
      ((NodeSequence) n.f0.choice).elementAt(1).accept(this, expr);
      // process declarator
      ((NodeSequence) n.f0.choice).elementAt(2).accept(this, expr);

      // process inner cast expression
      expr.setConstantExpression((ConstantExpression) ((NodeSequence) n.f0.choice)
          .elementAt(4).accept(this, expr));
      return expr;
    }
  }

  @Override
  public Object visit(final UnaryExpression n, final Node argu) {
    if (n.f0.present()) {
      final UnaryOperation expr = (UnaryOperation) newNode("unaryOperation", n);
      n.f0.accept(this, expr);
      expr.setConstantExpression((ConstantExpression) n.f1.accept(this, expr));
      return expr;
    } else {
      return n.f1.accept(this, argu);
    }
  }

  @Override
  public Object visit(final PrimaryExpression n, final Node argu) {
    if (n.f0.choice instanceof org.ow2.mind.idl.jtb.syntaxtree.Literal) {
      return n.f0.accept(this, argu);
    } else {
      return ((NodeSequence) n.f0.choice).elementAt(1).accept(this, argu);
    }
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.Literal n,
      final Node argu) {

    final NodeToken literal = (NodeToken) n.f0.choice;
    final Literal expr = (Literal) newNode("literal", literal);

    expr.setExpr(literal.tokenImage);

    return expr;
  }

  @Override
  public Object visit(final ShiftOperation n, final Node argu) {
    assert argu != null;
    final BinaryOperation expr = castNodeError(argu, BinaryOperation.class);
    switch (((NodeToken) n.f0.choice).kind) {
      case LSHIFT :
        expr.setOperation(BinaryOperation.LEFT_SHIFT);
        break;
      case RSHIFT :
        expr.setOperation(BinaryOperation.RIGHT_SHIFT);
        break;
    }
    return expr;
  }

  @Override
  public Object visit(final AdditiveOperation n, final Node argu) {
    assert argu != null;
    final BinaryOperation expr = castNodeError(argu, BinaryOperation.class);
    switch (((NodeToken) n.f0.choice).kind) {
      case PLUS :
        expr.setOperation(BinaryOperation.ADD);
        break;
      case MINUS :
        expr.setOperation(BinaryOperation.SUB);
        break;
    }
    return expr;
  }

  @Override
  public Object visit(final MulOperation n, final Node argu) {
    assert argu != null;
    final BinaryOperation expr = castNodeError(argu, BinaryOperation.class);
    switch (((NodeToken) n.f0.choice).kind) {
      case STAR :
        expr.setOperation(BinaryOperation.MULL);
        break;
      case SLASH :
        expr.setOperation(BinaryOperation.DIV);
        break;
      case PERCENT :
        expr.setOperation(BinaryOperation.MOD);
        break;
    }
    return expr;
  }

  @Override
  public Object visit(final org.ow2.mind.idl.jtb.syntaxtree.UnaryOperation n,
      final Node argu) {
    assert argu != null;
    final UnaryOperation expr = castNodeError(argu, UnaryOperation.class);
    switch (((NodeToken) n.f0.choice).kind) {
      case AMP :
        expr.setOperation(UnaryOperation.AMP);
        break;
      case STAR :
        expr.setOperation(UnaryOperation.STAR);
        break;
      case PLUS :
        expr.setOperation(UnaryOperation.PLUS);
        break;
      case MINUS :
        expr.setOperation(UnaryOperation.MINUS);
        break;
      case TILDE :
        expr.setOperation(UnaryOperation.TILDE);
        break;
      case NOT :
        expr.setOperation(UnaryOperation.NOT);
        break;
    }
    return expr;
  }

  // ---------------------------------------------------------------------------
  // Value grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final StringValue n, final Node argu) {
    assert argu != null;

    final StringLiteral value = (StringLiteral) newNode("string", n.f0);
    value.setValue(n.f0.tokenImage);

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(value);
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(value);
    }

    return value;
  }

  @Override
  public Node visit(final IntegerValue n, final Node argu) {
    assert argu != null;

    final NumberLiteral value = (NumberLiteral) newNode("integer", n.f1);
    if (n.f0.present()) {
      value.setValue(((NodeToken) ((NodeChoice) n.f0.node).choice).tokenImage
          + n.f1.tokenImage);
    } else {
      value.setValue(n.f1.tokenImage);
    }

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(value);
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(value);
    }

    return value;
  }

  @Override
  public Node visit(final BooleanValue n, final Node argu) {
    assert argu != null;

    final BooleanLiteral value = (BooleanLiteral) newNode("boolean", n.f0);
    if (n.f0.tokenImage.equals(BooleanLiteral.TRUE)) {
      value.setValue(BooleanLiteral.TRUE);
    } else {
      value.setValue(BooleanLiteral.FALSE);
    }

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(value);
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(value);
    }

    return value;
  }

  @Override
  public Node visit(final NullValue n, final Node argu) {
    assert argu != null;

    final NullLiteral value = (NullLiteral) newNode("null", n.f0);

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(value);
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(value);
    }

    return value;
  }

  // ---------------------------------------------------------------------------
  // Annotation grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final org.ow2.mind.idl.jtb.syntaxtree.Annotation n,
      final Node argu) {
    assert argu != null;

    final AnnotationNode annotation = (AnnotationNode) newNode("annotation",
        n.f0);

    // process type
    annotation.setType(fullyQualifiedName(n.f1));

    // process parameters
    n.f2.accept(this, annotation);

    castNodeError(argu, AnnotationContainer.class).addAnnotation(annotation);

    return annotation;
  }

  @Override
  public Node visit(final AnnotationParameters n, final Node argu) {
    assert argu != null;
    final AnnotationNode annotation = (AnnotationNode) argu;

    if (n.f1.present()) {
      if (((NodeChoice) n.f1.node).choice instanceof AnnotationValue) {
        final AnnotationArgument defaultParam = (AnnotationArgument) newNode(
            "annotationArgument", n.f0);
        defaultParam.setName(AnnotationArgument.DEFAULT_NAME);
        annotation.addAnnotationArgument(defaultParam);

        // process default value
        n.f1.accept(this, defaultParam);
      } else {
        n.f1.accept(this, annotation);
      }
    }
    return annotation;
  }

  @Override
  public Node visit(final AnnotationValuePair n, final Node argu) {
    assert argu != null;
    final AnnotationNode annotation = (AnnotationNode) argu;

    final AnnotationArgument param = (AnnotationArgument) newNode(
        "annotationArgument", n.f0);
    annotation.addAnnotationArgument(param);

    // process name
    param.setName(n.f0.tokenImage);

    // process value
    n.f2.accept(this, param);

    return annotation;
  }

  @Override
  public Node visit(final AnnotationAnnotationValue n, final Node argu) {
    assert argu != null;

    final AnnotationNode value = (AnnotationNode) newNode("annotationValue",
        n.f0.f0);

    // process type
    value.setType(fullyQualifiedName(n.f0.f1));

    // process parameters
    n.f0.f2.accept(this, value);

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(castNodeError(value, Value.class));
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(
          castNodeError(value, Value.class));
    }

    return value;
  }

  @Override
  public Node visit(final ArrayAnnotationValue n, final Node argu) {
    assert argu != null;

    final Array value = (Array) newNode("array", n.f0);

    // process sub values
    n.f1.accept(this, value);

    if (argu instanceof SingleValueContainer) {
      ((SingleValueContainer) argu).setValue(value);
    } else {
      castNodeError(argu, MultipleValueContainer.class).addValue(value);
    }

    return value;
  }

  // ---------------------------------------------------------------------------
  // Utility
  // ---------------------------------------------------------------------------

  private String fullyQualifiedName(final FullyQualifiedName n) {
    String name = n.f0.tokenImage;
    for (final org.ow2.mind.idl.jtb.syntaxtree.Node node : n.f1.nodes) {
      name += "." + ((NodeToken) ((NodeSequence) node).elementAt(1)).tokenImage;
    }
    return name;
  }

  private Type cloneTypeSpecifier(final Type typeSpecifier) {
    if (typeSpecifier instanceof StructDefinition) {
      final StructDefinition structDef = (StructDefinition) typeSpecifier;
      if (structDef.getName() == null) {
        // anonymous struct definition, clone the whole definition
        return cloneTree(structDef);
      } else {
        // named struct definition, generated a structRef node
        final StructReference structRef = (StructReference) newNode("structRef");
        copySource(structRef, structDef);
        structRef.setName(structDef.getName());
        return structRef;
      }
    } else if (typeSpecifier instanceof UnionDefinition) {
      final UnionDefinition unionDef = (UnionDefinition) typeSpecifier;
      if (unionDef.getName() == null) {
        // anonymous union definition, clone the whole definition
        return cloneTree(unionDef);
      } else {
        // named union definition, generated a unionRef node
        final UnionReference unionRef = (UnionReference) newNode("unionRef");
        copySource(unionRef, unionDef);
        unionRef.setName(unionDef.getName());
        return unionRef;
      }
    } else if (typeSpecifier instanceof EnumDefinition) {
      final EnumDefinition enumDef = (EnumDefinition) typeSpecifier;
      if (enumDef.getName() == null) {
        // anonymous enum definition, clone the whole definition
        return cloneTree(enumDef);
      } else {
        // named enum definition, generated a enumRef node
        final EnumReference enumRef = (EnumReference) newNode("unionRef");
        copySource(enumRef, enumDef);
        enumRef.setName(enumDef.getName());
        return enumRef;
      }
    } else {
      // in other cases simply clone the type specifier
      return cloneTree(typeSpecifier);
    }
  }

  private BinaryOperation visitBinaryExpression(final String operator,
      final org.ow2.mind.idl.jtb.syntaxtree.Node left,
      final org.ow2.mind.idl.jtb.syntaxtree.Node right,
      final org.ow2.mind.idl.jtb.syntaxtree.Node location, final Node argu) {
    final BinaryOperation operation = (BinaryOperation) newNode(
        "binaryOperation", location);
    operation.setOperation(operator);
    operation.addConstantExpression((ConstantExpression) left.accept(this,
        operation));
    operation.addConstantExpression((ConstantExpression) right.accept(this,
        argu));
    return operation;
  }
}
