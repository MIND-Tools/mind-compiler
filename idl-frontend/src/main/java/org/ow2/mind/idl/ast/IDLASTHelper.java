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

package org.ow2.mind.idl.ast;

import static org.ow2.mind.CommonASTHelper.newNode;
import static org.ow2.mind.PathHelper.isRelative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.ow2.mind.CommonASTHelper;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.PrimitiveType.PrimitiveTypeEnum;

public final class IDLASTHelper {
  private IDLASTHelper() {
  }

  // ---------------------------------------------------------------------------
  // InterfaceDefinition helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link InterfaceDefinition} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link InterfaceDefinition}.
   * @return a new {@link InterfaceDefinition} node.
   */
  public static InterfaceDefinition newInterfaceDefinitionNode(
      final NodeFactory nodeFactory, final String name) {
    final InterfaceDefinition node = newNode(nodeFactory, "itf",
        InterfaceDefinition.class, TypeCollectionContainer.class,
        IncludeContainer.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link Method} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link Method}.
   * @return a new {@link Method} node.
   */
  public static Method newMethodNode(final NodeFactory nodeFactory,
      final String name) {
    final Method node = newNode(nodeFactory, "method", Method.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link Parameter} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link Parameter}.
   * @return a new {@link Parameter} node.
   */
  public static Parameter newParameterNode(final NodeFactory nodeFactory,
      final String name) {
    final Parameter node = newNode(nodeFactory, "parameter", Parameter.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Returns the method contained by the given interface definition with the
   * given name. Returns <code>null</code> if the given interface definition
   * does not contain a method with the given name.
   * 
   * @param itfDef an interface definition.
   * @param methodName the name of the method to return.
   * @return the method contained by the given interface definition with the
   *         given name or <code>null</code>.
   */
  public static Method getMethod(final InterfaceDefinition itfDef,
      final String methodName) {
    for (final Method meth : itfDef.getMethods()) {
      if (meth.getName().equals(methodName)) return meth;
    }
    return null;
  }

  /**
   * Returns the parameter contained by the given method with the given name.
   * Returns <code>null</code> if the given method does not contain a parameter
   * with the given name.
   * 
   * @param method a method.
   * @param parameterName the name of the parameter to return.
   * @return the parameter contained by the given method with the given name or
   *         <code>null</code>.
   */
  public static Parameter getParameter(final Method method,
      final String parameterName) {
    for (final Parameter param : method.getParameters()) {
      if (param.getName().equals(parameterName)) return param;
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the given method has a variable number of
   * argument.
   * 
   * @param method the method to test.
   * @return <code>true</code> if the given method has a variable number of
   *         argument.
   */
  public static boolean isVaArgs(final Method method) {
    return Method.TRUE.equals(method.getVaArgs());
  }

  /**
   * Returns <code>true</code> if the given parameter is a <code>out</code>
   * parameter.
   * 
   * @param parameter a parameter.
   * @return <code>true</code> if the given parameter is a <code>out</code>
   *         parameter.
   */
  public static boolean isOut(final Parameter parameter) {
    return Parameter.TRUE.equals(parameter.getIsOut())
        && !Parameter.TRUE.equals(parameter.getIsIn());
  }

  /**
   * Returns <code>true</code> if the given parameter is a <code>in out</code>
   * parameter.
   * 
   * @param parameter a parameter.
   * @return <code>true</code> if the given parameter is a <code>inout</code>
   *         parameter.
   */
  public static boolean isInOut(final Parameter parameter) {
    return Parameter.TRUE.equals(parameter.getIsOut())
        && Parameter.TRUE.equals(parameter.getIsIn());
  }

  /**
   * Returns <code>true</code> if the given parameter is a <code>in</code>
   * parameter.
   * 
   * @param parameter a parameter.
   * @return <code>true</code> if the given parameter is a <code>in</code>
   *         parameter.
   */
  public static boolean isIn(final Parameter parameter) {
    return !Parameter.TRUE.equals(parameter.getIsOut())
        && (Parameter.TRUE.equals(parameter.getIsIn()) || parameter.getIsIn() == null);
  }

  // ---------------------------------------------------------------------------
  // IDT helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link SharedTypeDefinition} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link SharedTypeDefinition}.
   * @return a new {@link SharedTypeDefinition} node.
   */
  public static SharedTypeDefinition newIDTNode(final NodeFactory nodeFactory,
      final String name) {
    final SharedTypeDefinition node = newNode(nodeFactory, "idt",
        SharedTypeDefinition.class, IncludeContainer.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  // ---------------------------------------------------------------------------
  // Enum helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link EnumDefinition} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link EnumDefinition}.
   * @return a new {@link EnumDefinition} node.
   */
  public static EnumDefinition newEnumDefinitionNode(
      final NodeFactory nodeFactory, final String name) {
    final EnumDefinition node = newNode(nodeFactory, "type",
        EnumDefinition.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link EnumReference} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link EnumReference}.
   * @return a new {@link EnumReference} node.
   */
  public static EnumReference newEnumReferenceNode(
      final NodeFactory nodeFactory, final String name) {
    final EnumReference node = newNode(nodeFactory, "type", EnumReference.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link EnumMember} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link EnumMember}.
   * @return a new {@link EnumMember} node.
   */
  public static EnumMember newEnumMemberNode(final NodeFactory nodeFactory,
      final String name) {
    final EnumMember node = newNode(nodeFactory, "enumMember", EnumMember.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  // ---------------------------------------------------------------------------
  // Struct/Union helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link StructDefinition} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link StructDefinition}.
   * @return a new {@link StructDefinition} node.
   */
  public static StructDefinition newStructDefinitionNode(
      final NodeFactory nodeFactory, final String name) {
    final StructDefinition node = newNode(nodeFactory, "type",
        StructDefinition.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link StructReference} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link StructReference}.
   * @return a new {@link StructReference} node.
   */
  public static StructReference newStructReferenceNode(
      final NodeFactory nodeFactory, final String name) {
    final StructReference node = newNode(nodeFactory, "type",
        StructReference.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link UnionDefinition} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link UnionDefinition}.
   * @return a new {@link UnionDefinition} node.
   */
  public static UnionDefinition newUnionDefinitionNode(
      final NodeFactory nodeFactory, final String name) {
    final UnionDefinition node = newNode(nodeFactory, "type",
        UnionDefinition.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link UnionReference} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link UnionDefinition}.
   * @return a new {@link UnionReference} node.
   */
  public static UnionReference newUnionReferenceNode(
      final NodeFactory nodeFactory, final String name) {
    final UnionReference node = newNode(nodeFactory, "type",
        UnionReference.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link Member} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link Member}.
   * @return a new {@link Member} node.
   */
  public static Member newMemberNode(final NodeFactory nodeFactory,
      final String name) {
    final Member node = newNode(nodeFactory, "member", Member.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  // ---------------------------------------------------------------------------
  // Type helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link TypeDefReference} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link TypeDefReference}.
   * @return a new {@link TypeDefReference} node.
   */
  public static TypeDefReference newTypeDefReferenceNode(
      final NodeFactory nodeFactory, final String name) {

    final TypeDefReference node = newNode(nodeFactory, "type",
        TypeDefReference.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link PrimitiveType} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param primitiveType the name of the {@link PrimitiveType}.
   * @return a new {@link PrimitiveType} node.
   */
  public static PrimitiveType newPrimitiveTypeNode(
      final NodeFactory nodeFactory, final PrimitiveTypeEnum primitiveType) {
    return newPrimitiveTypeNode(nodeFactory, primitiveType.getIdlTypeName());
  }

  /**
   * Create a new {@link PrimitiveType} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the {@link PrimitiveType}.
   * @return a new {@link PrimitiveType} node.
   */
  public static PrimitiveType newPrimitiveTypeNode(
      final NodeFactory nodeFactory, final String name) {
    final PrimitiveType node = newNode(nodeFactory, "type", PrimitiveType.class);
    setKindDecorations(node);
    node.setName(name);
    return node;
  }

  /**
   * Create a new {@link ArrayOf} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param size the size of the array. May be null;
   * @return a new {@link ArrayOf} node.
   */
  public static ArrayOf newArrayOfNode(final NodeFactory nodeFactory,
      final ConstantExpression size) {
    final ArrayOf node = newNode(nodeFactory, "type", ArrayOf.class);
    setKindDecorations(node);
    node.setConstantExpression(size);
    return node;
  }

  /**
   * Create a new {@link PointerOf} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link PointerOf} node.
   */
  public static PointerOf newPointerOfNode(final NodeFactory nodeFactory) {
    final PointerOf node = newNode(nodeFactory, "type", PointerOf.class);
    setKindDecorations(node);
    return node;
  }

  // ---------------------------------------------------------------------------
  // Constant Expression helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link BinaryOperation} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param operation the operation of the {@link BinaryOperation}.
   * @return a new {@link BinaryOperation} node.
   */
  public static BinaryOperation newBinaryOperationNode(
      final NodeFactory nodeFactory, final String operation) {
    final BinaryOperation node = newNode(nodeFactory, "constantExpression",
        BinaryOperation.class);
    setKindDecorations(node);
    node.setOperation(operation);
    return node;
  }

  /**
   * Create a new {@link UnaryOperation} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param operation the operation of the {@link UnaryOperation}.
   * @return a new {@link UnaryOperation} node.
   */
  public static UnaryOperation newUnaryOperationNode(
      final NodeFactory nodeFactory, final String operation) {
    final UnaryOperation node = newNode(nodeFactory, "constantExpression",
        UnaryOperation.class);
    setKindDecorations(node);
    node.setOperation(operation);
    return node;
  }

  /**
   * Create a new {@link CastOperation} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param type the type of the {@link CastOperation}.
   * @return a new {@link CastOperation} node.
   */
  public static CastOperation newCastOperationNode(
      final NodeFactory nodeFactory, final Type type) {
    final CastOperation node = newNode(nodeFactory, "constantExpression",
        CastOperation.class);
    setKindDecorations(node);
    node.setType(type);
    return node;
  }

  /**
   * Create a new {@link Literal} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param expr the expression of the {@link Literal}.
   * @return a new {@link Literal} node.
   */
  public static Literal newLiteralNode(final NodeFactory nodeFactory,
      final String expr) {
    final Literal node = newNode(nodeFactory, "constantExpression",
        Literal.class);
    setKindDecorations(node);
    node.setExpr(expr);
    return node;
  }

  // ---------------------------------------------------------------------------
  // Include helper methods
  // ---------------------------------------------------------------------------

  /**
   * Create a new {@link Include} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param path the path of the {@link Include}.
   * @return a new {@link Include} node.
   */
  public static Include newIncludeNode(final NodeFactory nodeFactory,
      final String path, final IncludeDelimiter delimiter) {
    final Include node = newNode(nodeFactory, "include", Include.class);
    setKindDecorations(node);
    setIncludePath(node, path, delimiter);
    return node;
  }

  public static enum IncludeDelimiter {
    QUOTE, LT_GT;
  }

  public static String getIncludedPath(final Include include) {
    return include.getPath().substring(1, include.getPath().length() - 1);
  }

  public static IncludeDelimiter getIncludeDelimiter(final Include include) {
    if (include.getPath().startsWith("\"")) {
      return IncludeDelimiter.QUOTE;
    } else {
      if (!include.getPath().startsWith("<")) {
        throw new IllegalArgumentException("Invalid include path: "
            + include.getPath() + " Must start with '\"' of '<'");
      }
      return IncludeDelimiter.LT_GT;
    }
  }

  public static void setIncludePath(final Include include, final String path,
      final IncludeDelimiter delimiter) {
    switch (delimiter) {
      case QUOTE :
        include.setPath("\"" + path + "\"");
        break;
      case LT_GT :
        include.setPath("<" + path + ">");
        break;
    }
  }

  public static void setIncludePathPreserveDelimiter(final Include include,
      final String path) {
    setIncludePath(include, path, getIncludeDelimiter(include));
  }

  public static final String INCLUDED_IDL_DECORATION_NAME = "included-idl";

  public static void setIncludedIDL(final Include include, final IDL idl) {
    ReferencedIDLDecoration decoration = getIncludedIDLDecoration(include);
    if (decoration == null) {
      decoration = new ReferencedIDLDecoration(idl);
      include.astSetDecoration(INCLUDED_IDL_DECORATION_NAME, decoration);
    } else {
      decoration.setIDL(idl);
    }
  }

  public static IDL getIncludedIDL(final Include include,
      final IDLLoader loaderItf, final Map<Object, Object> context)
      throws ADLException {
    final ReferencedIDLDecoration decoration = getIncludedIDLDecoration(include);
    if (decoration == null) return null;
    if (decoration.getIDL() == null && loaderItf != null) {
      decoration.setIDL(loaderItf.load(decoration.getPath(), context));
    }
    return decoration.getIDL();
  }

  // ---------------------------------------------------------------------------
  // Kind decoration helper methods
  // ---------------------------------------------------------------------------

  public static final String KIND_DECORATION = "kind";

  public static void setKindDecorations(final Node node) {
    if (node instanceof InterfaceDefinition)
      node.astSetDecoration(KIND_DECORATION, "interface");
    else if (node instanceof EnumDefinition)
      node.astSetDecoration(KIND_DECORATION, "enum");
    else if (node instanceof EnumReference)
      node.astSetDecoration(KIND_DECORATION, "enumRef");
    else if (node instanceof StructDefinition)
      node.astSetDecoration(KIND_DECORATION, "struct");
    else if (node instanceof StructReference)
      node.astSetDecoration(KIND_DECORATION, "structRef");
    else if (node instanceof UnionDefinition)
      node.astSetDecoration(KIND_DECORATION, "union");
    else if (node instanceof UnionReference)
      node.astSetDecoration(KIND_DECORATION, "unionRef");
    else if (node instanceof TypeDefinition)
      node.astSetDecoration(KIND_DECORATION, "typedef");
    else if (node instanceof TypeDefReference)
      node.astSetDecoration(KIND_DECORATION, "typedefRef");
    else if (node instanceof PrimitiveType)
      node.astSetDecoration(KIND_DECORATION, "primitiveType");
    else if (node instanceof ArrayOf)
      node.astSetDecoration(KIND_DECORATION, "arrayOf");
    else if (node instanceof PointerOf)
      node.astSetDecoration(KIND_DECORATION, "pointerOf");
    else if (node instanceof ConstantDefinition)
      node.astSetDecoration(KIND_DECORATION, "constDef");
    else if (node instanceof BinaryOperation)
      node.astSetDecoration(KIND_DECORATION, "binaryOperation");
    else if (node instanceof UnaryOperation)
      node.astSetDecoration(KIND_DECORATION, "unaryOperation");
    else if (node instanceof CastOperation)
      node.astSetDecoration(KIND_DECORATION, "castOperation");
    else if (node instanceof Literal)
      node.astSetDecoration(KIND_DECORATION, "literal");

    for (final String type : node.astGetNodeTypes()) {
      for (final Node n : node.astGetNodes(type)) {
        if (n != null) setKindDecorations(n);
      }
    }
  }

  private static ReferencedIDLDecoration getIncludedIDLDecoration(
      final Include include) {
    return (ReferencedIDLDecoration) include
        .astGetDecoration(INCLUDED_IDL_DECORATION_NAME);
  }

  public static final class ReferencedIDLDecoration
      implements
        NodeContainerDecoration,
        Serializable {
    private transient IDL idl;
    private final String  path;

    public ReferencedIDLDecoration(final IDL idl) {
      if (idl == null) throw new IllegalArgumentException("idl can't be null");
      if (idl.getName() == null)
        throw new IllegalArgumentException("idl's name can't be null");
      if (!(idl instanceof InterfaceDefinition) && !(idl instanceof Header)
          && isRelative(idl.getName()))
        throw new IllegalArgumentException("idl's name can't be relative");

      this.idl = idl;
      this.path = idl.getName();
    }

    public IDL getIDL() {
      return idl;
    }

    public void setIDL(final IDL idl) {
      if (idl == null) throw new IllegalArgumentException("idl can't be null");
      if (idl.getName() == null)
        throw new IllegalArgumentException("idl's name can't be null");
      if (!idl.getName().equals(path))
        throw new IllegalArgumentException("Wrong idl path \"" + path
            + "\" expected instead of \"" + idl.getName() + "\".");

      this.idl = idl;
    }

    public String getPath() {
      return path;
    }

    // -------------------------------------------------------------------------
    // Implementation of the NodeContainerDecoration interface
    // -------------------------------------------------------------------------

    public Collection<Node> getNodes() {
      if (idl == null) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        l.add(idl);
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      if (replacements.containsKey(idl)) {
        idl = (IDL) replacements.get(idl);
      }
    }
  }

  public static final String REFERENCED_INTERFACE_DECORATION = "referenced-interfaces";

  public static void addReferencedInterface(final IDL idl,
      final InterfaceDefinition itf) {
    getDecoration(idl).addReferencedInterface(new ReferencedIDLDecoration(itf));
  }

  public static Collection<InterfaceDefinition> getReferencedInterfaces(
      final IDL idl, final IDLLoader loaderItf,
      final Map<Object, Object> context) throws ADLException {

    final ReferencedInterfaceDecoration decoration = getDecoration(idl);
    if (decoration == null) return Collections.emptyList();
    final Collection<ReferencedIDLDecoration> referencedInterfaces = decoration
        .getReferencedInterfaces();
    final List<InterfaceDefinition> result = new ArrayList<InterfaceDefinition>(
        referencedInterfaces.size());
    for (final ReferencedIDLDecoration referencedInterface : referencedInterfaces) {
      if (referencedInterface.getIDL() == null && loaderItf != null) {
        referencedInterface.setIDL(loaderItf.load(
            referencedInterface.getPath(), context));
      }
      result.add((InterfaceDefinition) referencedInterface.getIDL());
    }
    return result;
  }

  private static ReferencedInterfaceDecoration getDecoration(final IDL idl) {
    ReferencedInterfaceDecoration decoration = (ReferencedInterfaceDecoration) idl
        .astGetDecoration(REFERENCED_INTERFACE_DECORATION);
    if (decoration == null) {
      decoration = new ReferencedInterfaceDecoration();
      idl.astSetDecoration(REFERENCED_INTERFACE_DECORATION, decoration);
    }
    return decoration;
  }

  public static class ReferencedInterfaceDecoration
      implements
        NodeContainerDecoration,
        Serializable {

    Map<String, ReferencedIDLDecoration> itfs = new HashMap<String, ReferencedIDLDecoration>();

    void addReferencedInterface(final ReferencedIDLDecoration itf) {
      itfs.put(itf.getPath(), itf);
    }

    public Collection<ReferencedIDLDecoration> getReferencedInterfaces() {
      return itfs.values();
    }

    // -------------------------------------------------------------------------
    // Implementation of the NodeContainerDecoration interface
    // -------------------------------------------------------------------------

    public Collection<Node> getNodes() {
      if (itfs.isEmpty()) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        for (final ReferencedIDLDecoration itfDef : itfs.values()) {
          l.addAll(itfDef.getNodes());
        }
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      for (final Map.Entry<Node, Node> replacement : replacements.entrySet()) {
        final ReferencedIDLDecoration deco = itfs
            .get(((InterfaceDefinition) replacement.getKey()).getName());
        deco.setIDL((IDL) replacement.getValue());
      }
    }
  }

  /**
   * Returns a new {@link IDL} that correspond to an unresolved IDL. This kind
   * of IDL node can be used as a return value of front-end components that must
   * return a IDL but was unable to load it.
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link IDL#getName() name} of the IDL.
   * @return a new {@link IDL} that correspond to an unresolved IDL.
   * @see #isUnresolvedIDLNode(IDL)
   */
  public static IDL newUnresolvedIDLNode(final NodeFactory nodeFactory,
      final String name) {
    final IDL idl = CommonASTHelper.newNode(nodeFactory, "unresolved",
        IDL.class);
    idl.setName(name);
    return idl;
  }

  /**
   * Returns <code>true</code> if the given {@link IDL} does not correspond to a
   * correct IDL, but has been created by
   * {@link #newUnresolvedIDLNode(NodeFactory, String)}.
   * 
   * @param idl an IDL node.
   * @return <code>true</code> if the given {@link IDL} correspond to an
   *         unresolved IDL.
   * @see #newUnresolvedIDLNode(NodeFactory, String)
   */
  public static boolean isUnresolvedIDLNode(final IDL idl) {
    return idl.astGetType().equals("unresolved");
  }

  /**
   * Returns a new {@link InterfaceDefinition} that correspond to an unresolved
   * IDL. This kind of IDL node can be used as a return value of front-end
   * components that must return a IDL but was unable to load it.
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link IDL#getName() name} of the IDL.
   * @return a new {@link InterfaceDefinition} that correspond to an unresolved
   *         IDL.
   * @see #isUnresolvedInterfaceDefinitionNode(InterfaceDefinition)
   */
  public static InterfaceDefinition newUnresolvedInterfaceDefinitionNode(
      final NodeFactory nodeFactory, final String name) {
    final InterfaceDefinition idl = CommonASTHelper.newNode(nodeFactory,
        "unresolved", InterfaceDefinition.class);
    idl.setName(name);
    return idl;
  }

  /**
   * Returns <code>true</code> if the given {@link InterfaceDefinition} does not
   * correspond to a correct IDL, but has been created by
   * {@link #newUnresolvedInterfaceDefinitionNode(NodeFactory, String)} or
   * {@link #newUnresolvedIDLNode(NodeFactory, String)}.
   * 
   * @param idl an InterfaceDefinition node.
   * @return <code>true</code> if the given {@link InterfaceDefinition}
   *         correspond to an unresolved IDL.
   * @see #newUnresolvedInterfaceDefinitionNode(NodeFactory, String)
   * @see #newUnresolvedIDLNode(NodeFactory, String)
   */
  public static boolean isUnresolvedInterfaceDefinitionNode(
      final InterfaceDefinition idl) {
    return idl.astGetType().equals("unresolved");
  }

}
