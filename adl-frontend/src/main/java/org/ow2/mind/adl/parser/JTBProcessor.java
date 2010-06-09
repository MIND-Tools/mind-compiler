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

package org.ow2.mind.adl.parser;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.fractal.adl.ADLErrors;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.adl.anonymous.ast.AnonymousDefinitionContainer;
import org.ow2.mind.adl.ast.AbstractDefinition;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.ast.DefinitionReferenceContainer;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindDefinition;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;
import org.ow2.mind.adl.jtb.Parser;
import org.ow2.mind.adl.jtb.ParserConstants;
import org.ow2.mind.adl.jtb.syntaxtree.ADLFile;
import org.ow2.mind.adl.jtb.syntaxtree.AnnotationAnnotationValue;
import org.ow2.mind.adl.jtb.syntaxtree.AnnotationParameters;
import org.ow2.mind.adl.jtb.syntaxtree.AnnotationValue;
import org.ow2.mind.adl.jtb.syntaxtree.AnnotationValuePair;
import org.ow2.mind.adl.jtb.syntaxtree.ArchitectureDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.ArgumentAssignement;
import org.ow2.mind.adl.jtb.syntaxtree.ArrayAnnotationValue;
import org.ow2.mind.adl.jtb.syntaxtree.AttributeDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.AttributeType;
import org.ow2.mind.adl.jtb.syntaxtree.BindingDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.BooleanValue;
import org.ow2.mind.adl.jtb.syntaxtree.CompositeAnonymousDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.CompositeAnonymousExtension;
import org.ow2.mind.adl.jtb.syntaxtree.CompositeDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.CompositeDefinitionReference;
import org.ow2.mind.adl.jtb.syntaxtree.DataDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.ExtendedCompositeDefinitions;
import org.ow2.mind.adl.jtb.syntaxtree.ExtendedPrimitiveDefinitions;
import org.ow2.mind.adl.jtb.syntaxtree.ExtendedTypeDefinitions;
import org.ow2.mind.adl.jtb.syntaxtree.FormalParameterDeclaration;
import org.ow2.mind.adl.jtb.syntaxtree.FormalTypeParameterDeclaration;
import org.ow2.mind.adl.jtb.syntaxtree.FullyQualifiedName;
import org.ow2.mind.adl.jtb.syntaxtree.ImplementationDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.ImportDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.IntegerValue;
import org.ow2.mind.adl.jtb.syntaxtree.InterfaceDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.NodeChoice;
import org.ow2.mind.adl.jtb.syntaxtree.NodeOptional;
import org.ow2.mind.adl.jtb.syntaxtree.NodeSequence;
import org.ow2.mind.adl.jtb.syntaxtree.NodeToken;
import org.ow2.mind.adl.jtb.syntaxtree.NullValue;
import org.ow2.mind.adl.jtb.syntaxtree.Path;
import org.ow2.mind.adl.jtb.syntaxtree.PrimitiveAnonymousDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.PrimitiveAnonymousExtension;
import org.ow2.mind.adl.jtb.syntaxtree.PrimitiveDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.PrimitiveDefinitionReference;
import org.ow2.mind.adl.jtb.syntaxtree.ReferenceValue;
import org.ow2.mind.adl.jtb.syntaxtree.SimpleSubComponentReference;
import org.ow2.mind.adl.jtb.syntaxtree.StringValue;
import org.ow2.mind.adl.jtb.syntaxtree.SubComponentDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.TypeArgumentAssignement;
import org.ow2.mind.adl.jtb.syntaxtree.TypeDefinition;
import org.ow2.mind.adl.jtb.syntaxtree.TypeDefinitionReference;
import org.ow2.mind.adl.jtb.visitor.GJDepthFirst;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.annotation.ast.AnnotationArgument;
import org.ow2.mind.annotation.ast.AnnotationContainer;
import org.ow2.mind.annotation.ast.AnnotationNode;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.BooleanLiteral;
import org.ow2.mind.value.ast.MultipleValueContainer;
import org.ow2.mind.value.ast.NullLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.SingleValueContainer;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;
import org.xml.sax.SAXException;

/**
 * Translate the JTB AST of an ADL file into a "fractal-adl like" AST.
 */
public class JTBProcessor extends GJDepthFirst<Node, Node>
    implements
      ParserConstants {

  private final String            filename;
  private final XMLNodeFactory    nodeFactory;
  private final String            adlDtd;

  private final Set<String>       typeParameters    = new HashSet<String>();
  private ADLException            exception;
  private final BeginTokenVisitor beginTokenVisitor = new BeginTokenVisitor();
  private final EndTokenVisitor   endTokenVisitor   = new EndTokenVisitor();

  /**
   * @param nodeFactory The node factory to be used for instantiating AST nodes.
   * @param adlDtd The grammar definition for ADL nodes.
   * @param filename The name of the parsed file.
   */
  public JTBProcessor(final XMLNodeFactory nodeFactory, final String adlDtd,
      final String filename) {
    this.nodeFactory = nodeFactory;
    this.adlDtd = adlDtd;
    this.filename = filename;
    try {
      nodeFactory.checkDTD(adlDtd);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Error in dtd file '" + adlDtd + "'");
    }
  }

  /**
   * Translate the given JTB AST into a "fractal-adl like" AST.
   * 
   * @param fileContent a JTB AST obtained by the {@link Parser ADL Parser}.
   * @return the top level Definition {@link Node} corresponding to the given
   *         JTB AST.
   * @throws ADLException If an error occurs.
   */
  public Definition toDefinition(final ADLFile fileContent) throws ADLException {
    exception = null;
    typeParameters.clear();
    final Definition def = (Definition) visit(fileContent, null);
    if (exception != null) throw exception;
    return def;
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
      node = nodeFactory.newXMLNode(adlDtd, name);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to create node");
    }
    setSource(node, beginToken, endToken);

    return node;
  }

  private Node newNode(final String name,
      final org.ow2.mind.adl.jtb.syntaxtree.Node syntaxNode) {
    return newNode(name, syntaxNode.accept(beginTokenVisitor), syntaxNode
        .accept(endTokenVisitor));
  }

  private void setSource(final Node node, final NodeToken source) {
    setSource(node, source, source);
  }

  private void setSource(final Node node, final NodeToken beginToken,
      final NodeToken endToken) {
    if (beginToken == null)
      node.astSetSource(filename);
    else
      node.astSetSource(NodeErrorLocator.fullLocation(filename,
          beginToken.beginLine, endToken.endLine, beginToken.beginColumn,
          endToken.endColumn));

  }

  private void setSource(final Node node,
      final org.ow2.mind.adl.jtb.syntaxtree.Node syntaxNode) {
    setSource(node, syntaxNode.accept(beginTokenVisitor), syntaxNode
        .accept(endTokenVisitor));
  }

  private void copySource(final Node node, final Node from) {
    if (from == null)
      node.astSetSource(filename);
    else
      node.astSetSource(from.astGetSource());
  }

  // ---------------------------------------------------------------------------
  // File level grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final ADLFile n, final Node argu) {
    assert argu == null;
    final Definition def = (Definition) n.f1.accept(this, argu);
    assert def != null;

    // process imports
    n.f0.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final ImportDefinition n, final Node argu) {
    assert argu != null;
    final ImportContainer container = castNodeError(argu, ImportContainer.class);

    String packageName;
    final Import imp = (Import) newNode("import", n);

    packageName = n.f2.tokenImage;

    for (final org.ow2.mind.adl.jtb.syntaxtree.Node name : n.f4.nodes) {
      packageName += "."
          + ((NodeToken) ((NodeSequence) name).elementAt(0)).tokenImage;
    }
    imp.setPackageName(packageName);

    final NodeToken t = (NodeToken) n.f5.choice;
    if (t.kind == START) {
      imp.setSimpleName(Import.ON_DEMAND_IMPORT);
    } else {
      imp.setSimpleName(t.tokenImage);
    }

    container.addImport(imp);

    // process annotations
    n.f0.accept(this, imp);
    return imp;
  }

  @Override
  public Node visit(final ArchitectureDefinition n, final Node argu) {
    return n.f0.accept(this, argu);
  }

  // ---------------------------------------------------------------------------
  // Definition prototypes grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final TypeDefinition n, final Node argu) {
    assert argu == null;

    final MindDefinition def = (MindDefinition) newNode("type", n);

    // process annotations
    n.f0.accept(this, def);

    // set name
    def.setName(fullyQualifiedName(n.f2));

    // process "extends ..."
    n.f3.accept(this, def);

    // process body
    n.f4.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final ExtendedTypeDefinitions n, final Node argu) {
    assert argu != null;
    final MindDefinition def = castNodeError(argu, MindDefinition.class);

    final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
        "extends", n);

    def.setExtends(extend);

    // process definition reference list.
    n.f1.accept(this, extend);
    n.f2.accept(this, extend);
    return extend;
  }

  @Override
  public Node visit(final TypeDefinitionReference n, final Node argu) {

    final DefinitionReference defRef = (DefinitionReference) newNode(
        "definition", n);
    defRef.setName(fullyQualifiedName(n.f0));

    if (argu != null) {
      castNodeError(argu, DefinitionReferenceContainer.class)
          .addDefinitionReference(defRef);
    }

    return defRef;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final PrimitiveDefinition n, final Node argu) {
    assert argu == null;

    final MindDefinition def = (MindDefinition) newNode("primitive", n);

    // process annotations
    n.f0.accept(this, def);

    // process abstract
    if (n.f1.present())
      castNodeError(def, AbstractDefinition.class).setIsAbstract(
          AbstractDefinition.TRUE);

    // set name
    def.setName(fullyQualifiedName(n.f3));

    // process formal parameters
    n.f4.accept(this, def);

    // process "extends ..."
    n.f5.accept(this, def);

    // process body
    n.f6.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final FormalParameterDeclaration n, final Node argu) {
    assert argu != null;
    final FormalParameterContainer container = castNodeError(argu,
        FormalParameterContainer.class);

    final FormalParameter param = (FormalParameter) newNode("parameter", n);
    param.setName(n.f0.tokenImage);
    container.addFormalParameter(param);
    return null;
  }

  @Override
  public Node visit(final ExtendedPrimitiveDefinitions n, final Node argu) {
    assert argu != null;
    final MindDefinition def = castNodeError(argu, MindDefinition.class);

    final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
        "extends", n);

    def.setExtends(extend);

    // process definition reference list.
    n.f1.accept(this, extend);
    n.f2.accept(this, extend);
    return extend;
  }

  @Override
  public Node visit(final PrimitiveDefinitionReference n, final Node argu) {

    final DefinitionReference defRef = (DefinitionReference) newNode(
        "definition", n);
    defRef.setName(fullyQualifiedName(n.f0));

    // process arguments
    n.f1.accept(this, defRef);

    if (argu != null)
      castNodeError(argu, DefinitionReferenceContainer.class)
          .addDefinitionReference(defRef);

    return defRef;
  }

  @Override
  public Node visit(final ArgumentAssignement n, final Node argu) {
    final Argument arg = (Argument) n.f2.accept(this, argu);
    arg.setName(n.f0.tokenImage);
    setSource(arg, n.f0);
    return arg;
  }

  @Override
  public Node visit(final org.ow2.mind.adl.jtb.syntaxtree.Argument n,
      final Node argu) {
    assert argu != null;

    final Argument arg = (Argument) newNode("argument", n);

    // process argument value
    n.f0.accept(this, arg);
    assert arg.getValue() != null;
    copySource(arg, arg.getValue());

    castNodeError(argu, ArgumentContainer.class).addArgument(arg);

    return arg;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final CompositeDefinition n, final Node argu) {
    assert argu == null;

    final MindDefinition def = (MindDefinition) newNode("composite", n);

    // process annotations
    n.f0.accept(this, def);

    // set name
    def.setName(fullyQualifiedName(n.f2));

    // process formal type parameters
    n.f3.accept(this, def);

    // process formal parameters
    n.f4.accept(this, def);

    // process "extends ..."
    n.f5.accept(this, def);

    // process body
    n.f6.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final FormalTypeParameterDeclaration n, final Node argu) {
    assert argu != null;
    final FormalTypeParameter typeParam = (FormalTypeParameter) newNode(
        "typeParameter", n);
    typeParam.setName(n.f0.tokenImage);
    typeParam.setDefinitionReference((DefinitionReference) n.f2.accept(this,
        null));

    // add typeParam in set of type parameter.
    typeParameters.add(n.f0.tokenImage);

    castNodeError(argu, FormalTypeParameterContainer.class)
        .addFormalTypeParameter(typeParam);

    return typeParam;
  }

  @Override
  public Node visit(final CompositeDefinitionReference n, final Node argu) {

    final DefinitionReference defRef = (DefinitionReference) newNode(
        "definition", n);
    defRef.setName(fullyQualifiedName(n.f0));

    // process type arguments
    n.f1.accept(this, defRef);

    // process arguments
    n.f2.accept(this, defRef);

    if (argu != null)
      castNodeError(argu, DefinitionReferenceContainer.class)
          .addDefinitionReference(defRef);

    return defRef;
  }

  @Override
  public Node visit(final TypeArgumentAssignement n, final Node argu) {
    final TypeArgument arg = (TypeArgument) n.f2.accept(this, argu);
    arg.setTypeParameterName(n.f0.tokenImage);
    setSource(arg, n.f0);
    return arg;
  }

  @Override
  public Node visit(final org.ow2.mind.adl.jtb.syntaxtree.TypeArgument n,
      final Node argu) {
    assert argu != null;

    final TypeArgument typeArg = (TypeArgument) newNode("typeArgument", n);

    if (n.f0.choice instanceof CompositeDefinitionReference) {
      final CompositeDefinitionReference cdr = (CompositeDefinitionReference) n.f0.choice;

      // if CompositeDefinitionReference contains only a simple <IDENTIFIER>
      // that matches a declared formal type parameter
      if (!cdr.f0.f1.present() && !cdr.f1.present() && !cdr.f2.present()
          && typeParameters.contains(cdr.f0.f0.tokenImage)) {
        // CompositeDefinitionReference is in fact a reference to a
        // FormalTypeParameter.
        setSource(typeArg, cdr.f0.f0);
        typeArg.setTypeParameterReference(cdr.f0.f0.tokenImage);
      } else {
        // CompositeDefinitionReference is actually a definition reference
        final DefinitionReference defRef = (DefinitionReference) n.f0.choice
            .accept(this, null);
        typeArg.setDefinitionReference(defRef);
        copySource(typeArg, defRef);
      }
    } else {
      assert n.f0.choice instanceof NodeToken
          && ((NodeToken) n.f0.choice).kind == ANY;

      // nothing to do.
    }

    castNodeError(argu, TypeArgumentContainer.class).addTypeArgument(typeArg);

    return typeArg;
  }

  @Override
  public Node visit(final ExtendedCompositeDefinitions n, final Node argu) {
    assert argu != null;
    final MindDefinition def = castNodeError(argu, MindDefinition.class);

    final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
        "extends", n);

    def.setExtends(extend);

    // process definition reference list.
    n.f1.accept(this, extend);
    n.f2.accept(this, extend);
    return extend;
  }

  // ---------------------------------------------------------------------------
  // Content grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final InterfaceDefinition n, final Node argu) {
    assert argu != null;
    final MindInterface itf = (MindInterface) newNode("interface", n);

    // process annotations
    n.f0.accept(this, itf);

    // process PROVIDES/REQUIRES
    if (((NodeToken) n.f1.choice).kind == PROVIDES) {
      itf.setRole(TypeInterface.SERVER_ROLE);
    } else {
      assert ((NodeToken) n.f1.choice).kind == REQUIRES;
      itf.setRole(TypeInterface.CLIENT_ROLE);
    }

    // process contingency
    if (n.f2.present()) itf.setContingency(TypeInterface.OPTIONAL_CONTINGENCY);

    // process IDL signature
    itf.setSignature(fullyQualifiedName(n.f3));

    // process name
    itf.setName(n.f5.tokenImage);

    // process [count]
    if (n.f6.present()) {
      itf.setCardinality(TypeInterface.COLLECTION_CARDINALITY);
      if (((NodeOptional) ((NodeSequence) n.f6.node).elementAt(1)).present()) {
        final NodeToken count = (NodeToken) ((NodeOptional) ((NodeSequence) n.f6.node)
            .elementAt(1)).node;
        itf.setNumberOfElement(count.tokenImage);
      }
    }

    castNodeError(argu, InterfaceContainer.class).addInterface(itf);
    return itf;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final AttributeDefinition n, final Node argu) {
    assert argu != null;

    final Attribute attr = (Attribute) newNode("attribute", n);

    // process annotations
    n.f0.accept(this, attr);

    // process attribute type
    n.f2.accept(this, attr);

    // process name
    attr.setName(n.f3.tokenImage);

    // process value
    n.f4.accept(this, attr);

    castNodeError(argu, AttributeContainer.class).addAttribute(attr);

    return attr;
  }

  @Override
  public Node visit(final AttributeType n, final Node argu) {
    assert argu != null;

    castNodeError(argu, Attribute.class).setType(
        ((NodeToken) n.f0.choice).tokenImage);

    return argu;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final DataDefinition n, final Node argu) {
    assert argu != null;

    final Data data = (Data) newNode("data", n);

    // process Path() | <CCode>
    if (n.f2.choice instanceof NodeToken) {
      assert ((NodeToken) n.f2.choice).kind == INLINED_CODE;
      final String inlinedCCode = ((NodeToken) n.f2.choice).tokenImage;
      data.setCCode(inlinedCCode.substring(2, inlinedCCode.length() - 2));
    } else {
      assert n.f2.choice instanceof Path;
      data.setPath(path((Path) n.f2.choice));
    }
    // process annotations
    n.f0.accept(this, data);

    final ImplementationContainer implContainer = castNodeError(argu,
        ImplementationContainer.class);
    if (implContainer.getData() != null) {
      exception = new ADLException(org.ow2.mind.adl.ADLErrors.MULTIPLE_DATA,
          data);
    }
    implContainer.setData(data);

    return data;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final ImplementationDefinition n, final Node argu) {
    assert argu != null;

    final Source src = (Source) newNode("source", n);

    // process annotations
    n.f0.accept(this, src);

    // process Path() | <CCode>
    if (n.f2.choice instanceof NodeToken) {
      assert ((NodeToken) n.f2.choice).kind == INLINED_CODE;
      final String inlinedCCode = ((NodeToken) n.f2.choice).tokenImage;
      src.setCCode(inlinedCCode.substring(2, inlinedCCode.length() - 2));
    } else {
      assert n.f2.choice instanceof Path;
      src.setPath(path((Path) n.f2.choice));
    }

    castNodeError(argu, ImplementationContainer.class).addSource(src);

    return src;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final BindingDefinition n, final Node argu) {
    assert argu != null;

    final Binding bind = (Binding) newNode("binding", n);

    // process annotations
    n.f0.accept(this, bind);

    // process from component name
    bind.setFromComponent(((NodeToken) n.f2.f0.choice).tokenImage);

    // process from interface name
    bind.setFromInterface(n.f4.tokenImage);

    // process from interface index (if any)
    if (n.f5.present())
      bind.setFromInterfaceNumber(((NodeToken) ((NodeSequence) n.f5.node)
          .elementAt(1)).tokenImage);

    // process to component name
    bind.setToComponent(((NodeToken) n.f7.f0.choice).tokenImage);

    // process to interface name
    bind.setToInterface(n.f9.tokenImage);

    // process to interface index (if any)
    if (n.f10.present())
      bind.setToInterfaceNumber(((NodeToken) ((NodeSequence) n.f10.node)
          .elementAt(1)).tokenImage);

    castNodeError(argu, BindingContainer.class).addBinding(bind);

    return bind;
  }

  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final SubComponentDefinition n, final Node argu) {
    assert argu != null;

    final Component comp = (Component) newNode("component", n);

    // process annotations
    n.f0.accept(this, comp);

    // process SubComponentReference
    n.f2.accept(this, comp);

    castNodeError(argu, ComponentContainer.class).addComponent(comp);

    return comp;
  }

  @Override
  public Node visit(final SimpleSubComponentReference n, final Node argu) {
    assert argu != null;
    final Component comp = (Component) argu;
    boolean hasDefRef = false;

    // process definition reference
    if (n.f0.present()) {
      hasDefRef = true;
      final CompositeDefinitionReference cdr = (CompositeDefinitionReference) n.f0.node;

      // if CompositeDefinitionReference contains only a simple <IDENTIFIER>
      // that matches a declared formal type parameter
      if (!cdr.f0.f1.present() && !cdr.f1.present() && !cdr.f2.present()
          && typeParameters.contains(cdr.f0.f0.tokenImage)) {
        // CompositeDefinitionReference is in fact a reference to a
        // FormalTypeParameter.
        castNodeError(comp, FormalTypeParameterReference.class)
            .setTypeParameterReference(cdr.f0.f0.tokenImage);
      } else {
        // CompositeDefinitionReference is actually a definition reference
        final DefinitionReference defRef = (DefinitionReference) n.f0.accept(
            this, null);
        comp.setDefinitionReference(defRef);
      }
    }

    // process name
    comp.setName(n.f2.tokenImage);

    // process anonymous definition
    if (n.f3.present()) {
      if (comp instanceof FormalTypeParameterReference
          && ((FormalTypeParameterReference) comp).getTypeParameterReference() != null) {
        // both reference to template parameter and anonymous definition
        exception = new ADLException(ADLErrors.PARSE_ERROR, comp,
            "The contains construct cannot reference a template parameter and "
                + "have an anonymous definition.");
      }
      n.f3.accept(this, comp);
    } else if (!hasDefRef) {
      // neither defRef nor anonymous definition
      exception = new ADLException(ADLErrors.PARSE_ERROR, comp,
          "The contains construct must reference "
              + "another definition or have an anonymous definition.");
    }

    return comp;
  }

  @Override
  public Node visit(final CompositeAnonymousDefinition n, final Node argu) {
    assert argu != null;
    final Component comp = (Component) argu;

    final MindDefinition def = (MindDefinition) newNode("anonymousComposite", n);
    castNodeError(comp, AnonymousDefinitionContainer.class)
        .setAnonymousDefinition(def);

    final DefinitionReference defRef = comp.getDefinitionReference();
    if (defRef != null) {
      comp.setDefinitionReference(null);
      final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
          "extends", n.f1);
      def.setExtends(extend);
      extend.addDefinitionReference(defRef);
    }

    // process annotations
    n.f0.accept(this, def);

    // process composite definition elements
    n.f3.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final PrimitiveAnonymousDefinition n, final Node argu) {
    assert argu != null;
    final Component comp = (Component) argu;

    final MindDefinition def = (MindDefinition) newNode("anonymousPrimitive", n);
    castNodeError(comp, AnonymousDefinitionContainer.class)
        .setAnonymousDefinition(def);

    final DefinitionReference defRef = comp.getDefinitionReference();
    if (defRef != null) {
      comp.setDefinitionReference(null);
      final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
          "extends", n.f1);
      def.setExtends(extend);
      extend.addDefinitionReference(defRef);
    }

    // process annotations
    n.f0.accept(this, def);

    // process composite definition elements
    n.f3.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final CompositeAnonymousExtension n, final Node argu) {
    assert argu != null;
    final Component comp = (Component) argu;

    final MindDefinition def = (MindDefinition) newNode("anonymousComposite", n);
    castNodeError(comp, AnonymousDefinitionContainer.class)
        .setAnonymousDefinition(def);

    final DefinitionReference defRef = comp.getDefinitionReference();
    if (defRef != null) {
      comp.setDefinitionReference(null);
      final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
          "extends", n.f1);
      def.setExtends(extend);
      extend.addDefinitionReference(defRef);
    }

    // process annotations
    n.f0.accept(this, def);

    // process composite definition elements
    n.f2.accept(this, def);

    return def;
  }

  @Override
  public Node visit(final PrimitiveAnonymousExtension n, final Node argu) {
    assert argu != null;
    final Component comp = (Component) argu;

    final MindDefinition def = (MindDefinition) newNode("anonymousPrimitive", n);
    castNodeError(comp, AnonymousDefinitionContainer.class)
        .setAnonymousDefinition(def);

    final DefinitionReference defRef = comp.getDefinitionReference();
    if (defRef != null) {
      comp.setDefinitionReference(null);
      final DefinitionReferenceContainer extend = (DefinitionReferenceContainer) newNode(
          "extends", n.f1);
      def.setExtends(extend);
      extend.addDefinitionReference(defRef);
    }

    // process annotations
    n.f0.accept(this, def);

    // process composite definition elements
    n.f2.accept(this, def);

    return def;
  }

  // ---------------------------------------------------------------------------
  // Annotation grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final org.ow2.mind.adl.jtb.syntaxtree.Annotation n,
      final Node argu) {
    assert argu != null;

    final AnnotationNode annotation = (AnnotationNode) newNode("annotation", n);

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
            "annotationArgument", n);
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
        "annotationArgument", n);
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

    final AnnotationNode value = (AnnotationNode) newNode("annotationValue", n);

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

    final Array value = (Array) newNode("array", n);

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
  // Value grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final StringValue n, final Node argu) {
    assert argu != null;

    final StringLiteral value = (StringLiteral) newNode("string", n);
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

    final NumberLiteral value = (NumberLiteral) newNode("integer", n);
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

    final BooleanLiteral value = (BooleanLiteral) newNode("boolean", n);
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
  public Node visit(final ReferenceValue n, final Node argu) {
    assert argu != null;

    final Reference value = (Reference) newNode("reference", n);
    value.setRef(n.f0.tokenImage);

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

    final NullLiteral value = (NullLiteral) newNode("null", n);

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
    for (final org.ow2.mind.adl.jtb.syntaxtree.Node node : n.f1.nodes) {
      name += "." + ((NodeToken) ((NodeSequence) node).elementAt(1)).tokenImage;
    }
    return name;
  }

  private String path(final Path n) {
    String s;
    if (n.f0.present()) {
      s = "/";
    } else {
      s = "";
    }
    if (n.f1.present()) {
      s += "./";
    }

    for (int i = 0; i < n.f2.size(); i++)
      s += "../";

    s += n.f3.tokenImage;

    for (final org.ow2.mind.adl.jtb.syntaxtree.Node pathElem : n.f4.nodes) {
      s += "/"
          + ((NodeToken) ((NodeSequence) pathElem).elementAt(1)).tokenImage;
    }

    s += "." + n.f6.tokenImage;

    return s;
  }
}
