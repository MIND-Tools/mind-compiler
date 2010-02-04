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

package org.ow2.mind.idl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.idl.IncludeDecorationHelper;
import org.ow2.mind.idl.ast.ArrayOf;
import org.ow2.mind.idl.ast.ConstantDefinition;
import org.ow2.mind.idl.ast.EnumDefinition;
import org.ow2.mind.idl.ast.EnumMember;
import org.ow2.mind.idl.ast.EnumReference;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Member;
import org.ow2.mind.idl.ast.MemberContainer;
import org.ow2.mind.idl.ast.Method;
import org.ow2.mind.idl.ast.Parameter;
import org.ow2.mind.idl.ast.PointerOf;
import org.ow2.mind.idl.ast.PrimitiveType;
import org.ow2.mind.idl.ast.StructDefinition;
import org.ow2.mind.idl.ast.StructReference;
import org.ow2.mind.idl.ast.Type;
import org.ow2.mind.idl.ast.TypeCollectionContainer;
import org.ow2.mind.idl.ast.TypeDefReference;
import org.ow2.mind.idl.ast.TypeDefinition;
import org.ow2.mind.idl.ast.UnionDefinition;
import org.ow2.mind.idl.ast.UnionReference;

public class ASTChecker {

  protected final Map<Node, Object> checkers = new IdentityHashMap<Node, Object>();

  // ---------------------------------------------------------------------------
  // IDL checking
  // ---------------------------------------------------------------------------

  protected void checkIDL(final IDL idl) {
    assertNotNull("IDL is null", idl);
    assertNotNull("idl name is null", idl.getName());

    if (idl instanceof IncludeContainer)
      checkIncludeContainer((IncludeContainer) idl);

    if (idl instanceof TypeCollectionContainer) {
      for (final Type type : ((TypeCollectionContainer) idl).getTypes()) {
        checkType(type);
      }
    }

    if (idl instanceof InterfaceDefinition)
      checkInterface((InterfaceDefinition) idl);
  }

  protected void checkInterface(final InterfaceDefinition itf) {
    for (final Method method : itf.getMethods()) {
      checkMethod(method);
    }
  }

  public IDLChecker assertIDL(final IDL idl) {
    IDLChecker checker = (IDLChecker) checkers.get(idl);
    if (checker == null) {
      checker = createIDLChecker(idl);
      checkers.put(idl, checker);
    }
    return checker;
  }

  protected IDLChecker createIDLChecker(final IDL idl) {
    return new IDLChecker(idl);
  }

  public class IDLChecker {
    public final IDL idl;

    public IDLChecker(final IDL idl) {
      this.idl = idl;
      checkIDL(idl);
    }

    public IDLChecker that() {
      return this;
    }

    public IDLChecker and() {
      return this;
    }

    public IDLChecker extendsInterface(final String name) {
      if (!(idl instanceof InterfaceDefinition))
        fail("idl do not implements InterfaceDefinition");

      final InterfaceDefinition container = (InterfaceDefinition) idl;
      assertEquals("IDL " + idl.getName() + " does not extends \"" + name
          + "\".", name, container.getExtends());
      return this;
    }

    public MethodChecker containsMethod(final String name) {
      if (!(idl instanceof InterfaceDefinition))
        fail("idl do not implements InterfaceDefinition");

      final InterfaceDefinition container = (InterfaceDefinition) idl;
      for (final Method meth : container.getMethods()) {
        if (name.equals(meth.getName())) return assertMethod(meth, container);
      }
      fail("IDL " + idl.getName() + " does not contains a \"" + name
          + "\" method.");
      // unreachable
      return null;

    }

    public MethodCheckerIterator containsMethods(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<MethodChecker> list = new ArrayList<MethodChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsMethod(name));
      }
      assertEquals("IDL contains more methods than expected.", names.length,
          ((InterfaceDefinition) idl).getMethods().length);
      return new MethodCheckerIterator(list);
    }

    public IncludeChecker containsInclude(final String name) {
      if (!(idl instanceof IncludeContainer))
        fail("idl do not implements IncludeContainer");

      final IncludeContainer container = (IncludeContainer) idl;
      for (final Include include : container.getIncludes()) {
        if (name.equals(include.getPath()))
          return assertInclude(include, container);
      }
      fail("IDL " + idl.getName() + " does not include \"" + name + "\".");
      // unreachable
      return null;
    }

    public IncludeCheckerIterator containsIncludes(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<IncludeChecker> list = new ArrayList<IncludeChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsInclude(name));
      }
      assertEquals("IDL contains more includes than expected.", names.length,
          ((IncludeContainer) idl).getIncludes().length);
      return new IncludeCheckerIterator(list);
    }

    public TypeCheckerIterator definesType() {
      if (!(idl instanceof TypeCollectionContainer))
        fail("idl do not implements TypeCollectionContainer");

      final TypeCollectionContainer container = (TypeCollectionContainer) idl;
      final List<TypeChecker> checkers = new ArrayList<TypeChecker>();
      for (final Type type : container.getTypes()) {
        checkers.add(assertType(type));
      }
      return new TypeCheckerIterator(checkers);
    }
  }

  // ---------------------------------------------------------------------------
  // Include checking
  // ---------------------------------------------------------------------------

  protected void checkInclude(final Include include) {
    assertNotNull("Include node is null", include);
    assertNotNull("Include path is null", include.getPath());
  }

  protected void checkIncludeContainer(final IncludeContainer container) {
    for (final Include include : container.getIncludes()) {
      checkInclude(include);
    }
  }

  protected IncludeChecker assertInclude(final Include include,
      final IncludeContainer container) {
    IncludeChecker checker = (IncludeChecker) checkers.get(include);
    if (checker == null) {
      checker = createIncludeChecker(include, container);
      checkers.put(include, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected IncludeChecker createIncludeChecker(final Include include,
      final IncludeContainer container) {
    return new IncludeChecker(include, container);
  }

  public class IncludeChecker {
    public final Include          include;
    public final IncludeContainer container;

    protected IncludeChecker(final Include include,
        final IncludeContainer container) {
      this.include = include;
      this.container = container;
      checkInclude(include);
    }

    public IncludeChecker pathIs(final String path) {
      assertEquals("Unexpected include path \"" + include.getPath() + "\".",
          path, include.getPath());
      return this;
    }

    public IDLChecker includes() {
      try {
        return assertIDL(IncludeDecorationHelper.getIncludedIDL(include, null,
            null));
      } catch (final ADLException e) {
        // never append
        return null;
      }
    }
  }

  public class IncludeCheckerIterator
      extends
        CheckerIterator<IncludeCheckerIterator, IncludeChecker> {

    public IncludeCheckerIterator(final List<IncludeChecker> list) {
      super(list);
    }

    @Override
    protected IncludeCheckerIterator getThis() {
      return this;
    }

    public IncludeCheckerIterator pathIs(final String path) {
      element.pathIs(path);
      return this;
    }

    public IDLChecker includes() {
      return element.includes();
    }
  }

  // ---------------------------------------------------------------------------
  // Method checking
  // ---------------------------------------------------------------------------

  protected void checkMethod(final Method method) {
    assertNotNull("Method node is null", method);
    assertNotNull("Method name is null", method.getName());
    checkType(method.getType());
    final Set<String> names = new HashSet<String>();
    for (final Parameter param : method.getParameters()) {
      assertTrue("Duplicated parameter name.", names.add(param.getName()));
      checkParameter(param);
    }
  }

  protected MethodChecker assertMethod(final Method method,
      final InterfaceDefinition container) {
    MethodChecker checker = (MethodChecker) checkers.get(method);
    if (checker == null) {
      checker = createMethodChecker(method, container);
      checkers.put(method, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected MethodChecker createMethodChecker(final Method method,
      final InterfaceDefinition container) {
    return new MethodChecker(method, container);
  }

  public class MethodChecker {
    public final Method              method;
    public final InterfaceDefinition container;

    protected MethodChecker(final Method method,
        final InterfaceDefinition container) {
      this.method = method;
      this.container = container;
      checkMethod(method);
    }

    public TypeChecker returnsType() {
      return assertType(method.getType());
    }

    public ParameterChecker hasParameter(final String name) {
      for (final Parameter param : method.getParameters()) {
        if (name.equals(param.getName())) {
          return assertParameter(param, method);
        }
      }
      fail("method \"" + method.getName() + "\"does not contains a \"" + name
          + "\" parameter.");
      // unreachable
      return null;
    }

    public ParameterCheckerIterator hasParameters(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<ParameterChecker> list = new ArrayList<ParameterChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(hasParameter(name));
      }
      assertEquals("Method contains more parameter than expected.",
          names.length, method.getParameters().length);

      return new ParameterCheckerIterator(list);
    }

  }

  public class MethodCheckerIterator
      extends
        CheckerIterator<MethodCheckerIterator, MethodChecker> {

    public MethodCheckerIterator(final List<MethodChecker> list) {
      super(list);
    }

    @Override
    protected MethodCheckerIterator getThis() {
      return this;
    }

    public TypeChecker returnsType() {
      return element.returnsType();
    }

    public ParameterChecker hasParameter(final String name) {
      return element.hasParameter(name);
    }

    public ParameterCheckerIterator hasParameters(final String... names) {
      return element.hasParameters(names);
    }
  }

  // ---------------------------------------------------------------------------
  // Parameter checking
  // ---------------------------------------------------------------------------

  protected void checkParameter(final Parameter parameter) {
    assertNotNull("Parameter node is null", parameter);
    assertNotNull("Parameter name is null", parameter.getName());
    checkType(parameter.getType());
  }

  protected ParameterChecker assertParameter(final Parameter parameter,
      final Method container) {
    ParameterChecker checker = (ParameterChecker) checkers.get(parameter);
    if (checker == null) {
      checker = createParameterChecker(parameter, container);
      checkers.put(parameter, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected ParameterChecker createParameterChecker(final Parameter parameter,
      final Method container) {
    return new ParameterChecker(parameter, container);
  }

  public class ParameterChecker {
    public final Parameter parameter;
    public final Method    container;

    protected ParameterChecker(final Parameter parameter, final Method container) {
      this.parameter = parameter;
      this.container = container;
      checkParameter(parameter);
    }

    public TypeChecker hasType() {
      return assertType(parameter.getType());
    }

  }

  public class ParameterCheckerIterator
      extends
        CheckerIterator<ParameterCheckerIterator, ParameterChecker> {

    public ParameterCheckerIterator(final List<ParameterChecker> list) {
      super(list);
    }

    @Override
    protected ParameterCheckerIterator getThis() {
      return this;
    }

    public TypeChecker hasType() {
      return element.hasType();
    }
  }

  // ---------------------------------------------------------------------------
  // Type checking
  // ---------------------------------------------------------------------------

  protected void checkType(final Type type) {
    assertNotNull("Type node is null", type);
    if (type instanceof EnumDefinition) {
      checkType((EnumDefinition) type);
    } else if (type instanceof EnumReference) {
      checkType((EnumReference) type);
    } else if (type instanceof StructDefinition) {
      checkType((StructDefinition) type);
    } else if (type instanceof StructReference) {
      checkType((StructReference) type);
    } else if (type instanceof UnionDefinition) {
      checkType((UnionDefinition) type);
    } else if (type instanceof UnionReference) {
      checkType((UnionReference) type);
    } else if (type instanceof TypeDefinition) {
      checkType((TypeDefinition) type);
    } else if (type instanceof TypeDefReference) {
      checkType((TypeDefReference) type);
    } else if (type instanceof ConstantDefinition) {
      checkType((ConstantDefinition) type);
    } else if (type instanceof PrimitiveType) {
      checkType((PrimitiveType) type);
    } else if (type instanceof ArrayOf) {
      checkType((ArrayOf) type);
    } else if (type instanceof PointerOf) {
      checkType((PointerOf) type);
    }
  }

  protected void checkType(final EnumDefinition type) {
    final Set<String> names = new HashSet<String>();
    for (final EnumMember member : type.getEnumMembers()) {
      assertTrue("Duplicated member name.", names.add(member.getName()));
      checkEnumMember(member);
    }
  }

  protected void checkType(final EnumReference type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final StructDefinition type) {
    final Set<String> names = new HashSet<String>();
    for (final Member member : type.getMembers()) {
      assertTrue("Duplicated member name.", names.add(member.getName()));
      checkMember(member);
    }
  }

  protected void checkType(final StructReference type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final UnionDefinition type) {
    final Set<String> names = new HashSet<String>();
    for (final Member member : type.getMembers()) {
      assertTrue("Duplicated member name.", names.add(member.getName()));
      checkMember(member);
    }
  }

  protected void checkType(final UnionReference type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final TypeDefinition type) {
    assertNotNull("Type name is null", type.getName());
    checkType(type.getType());
  }

  protected void checkType(final TypeDefReference type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final ConstantDefinition type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final PrimitiveType type) {
    assertNotNull("Type name is null", type.getName());
  }

  protected void checkType(final ArrayOf type) {
    checkType(type.getType());
  }

  protected void checkType(final PointerOf type) {
    checkType(type.getType());
  }

  protected TypeChecker assertType(final Type type) {
    TypeChecker checker = (TypeChecker) checkers.get(type);
    if (checker == null) {
      checker = createTypeChecker(type);
      checkers.put(type, checker);
    }
    return checker;
  }

  protected TypeChecker createTypeChecker(final Type type) {
    return new TypeChecker(type);
  }

  public class TypeChecker {
    public final Type type;

    protected TypeChecker(final Type type) {
      this.type = type;
      checkType(type);
    }

    public TypeChecker isPrimitiveType(final String name) {
      assertTrue("Type node is not a PrimitiveType",
          type instanceof PrimitiveType);
      assertEquals(name, ((PrimitiveType) type).getName());
      return this;
    }

    public TypeChecker isPointerOf() {
      assertTrue("Type node is not a PointerOf", type instanceof PointerOf);
      return assertType(((PointerOf) type).getType());
    }

    public TypeChecker isArrayOf() {
      assertTrue("Type node is not a ArrayOf", type instanceof ArrayOf);
      return assertType(((ArrayOf) type).getType());
    }

    public TypeChecker isTypedefOf(final String name) {
      assertTrue("Type node is not a TypeDefinition",
          type instanceof TypeDefinition);
      assertEquals(name, ((TypeDefinition) type).getName());
      return assertType(((TypeDefinition) type).getType());
    }

    public void isTypedefRefOf(final String name) {
      assertTrue("Type node is not a TypeDefReference",
          type instanceof TypeDefReference);
      assertEquals(name, ((TypeDefReference) type).getName());
    }

    public TypeChecker isEnumRef(final String name) {
      assertTrue("Type node is not a EnumReference",
          type instanceof EnumReference);
      assertEquals(name, ((EnumReference) type).getName());
      return this;
    }

    public TypeChecker isStructRef(final String name) {
      assertTrue("Type node is not a StructReference",
          type instanceof StructReference);
      assertEquals(name, ((StructReference) type).getName());
      return this;
    }

    public TypeChecker isUnionRef(final String name) {
      assertTrue("Type node is not a UnionReference",
          type instanceof UnionReference);
      assertEquals(name, ((UnionReference) type).getName());
      return this;
    }

    public EnumChecker isEnumDef(final String name) {
      assertTrue("Type node is not a EnumDefinition",
          type instanceof EnumDefinition);
      assertEquals(name, ((EnumDefinition) type).getName());
      return assertEnum((EnumDefinition) type);
    }

    public StructUnionChecker isStructDef(final String name) {
      assertTrue("Type node is not a StructDefinition",
          type instanceof StructDefinition);
      assertEquals(name, ((StructDefinition) type).getName());
      return assertStructUnion((StructDefinition) type);
    }

    public StructUnionChecker isUnionDef(final String name) {
      assertTrue("Type node is not a UnionDefinition",
          type instanceof UnionDefinition);
      assertEquals(name, ((UnionDefinition) type).getName());
      return assertStructUnion((UnionDefinition) type);
    }
  }

  public class TypeCheckerIterator
      extends
        CheckerIterator<TypeCheckerIterator, TypeChecker> {

    public TypeCheckerIterator(final List<TypeChecker> list) {
      super(list);
    }

    @Override
    protected TypeCheckerIterator getThis() {
      return this;
    }

    public TypeChecker isPrimitiveType(final String name) {
      return element.isPrimitiveType(name);
    }

    public TypeChecker isPointerOf() {
      return element.isPointerOf();
    }

    public TypeChecker isArrayOf() {
      return element.isArrayOf();
    }

    public TypeChecker isTypedefOf(final String name) {
      return element.isTypedefOf(name);
    }

    public TypeCheckerIterator isTypedefRefOf(final String name) {
      element.isTypedefRefOf(name);
      return this;
    }

    public TypeChecker isEnumRef(final String name) {
      return element.isEnumRef(name);
    }

    public TypeChecker isStructRef(final String name) {
      return element.isStructRef(name);
    }

    public TypeChecker isUnionRef(final String name) {
      return element.isUnionRef(name);
    }

    public EnumChecker isEnumDef(final String name) {
      return element.isEnumDef(name);
    }

    public StructUnionChecker isStructDef(final String name) {
      return element.isStructDef(name);
    }

    public StructUnionChecker isUnionDef(final String name) {
      return element.isUnionDef(name);
    }
  }

  public EnumChecker assertEnum(final EnumDefinition enumDef) {
    return createEnumChecker(enumDef);
  }

  protected EnumChecker createEnumChecker(final EnumDefinition enumDef) {
    return new EnumChecker(enumDef);
  }

  public class EnumChecker {
    EnumDefinition enumDef;

    EnumChecker(final EnumDefinition enumDef) {
      this.enumDef = enumDef;
      checkType(enumDef);
    }

    public EnumMemberChecker hasMember(final String name) {
      for (final EnumMember member : enumDef.getEnumMembers()) {
        if (name.equals(member.getName())) {
          return assertEnumMember(member);
        }
      }
      fail("Enum does not contains a \"" + name + "\" member.");
      // unreachable
      return null;
    }

    public EnumMemberCheckerIterator hasMembers(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<EnumMemberChecker> list = new ArrayList<EnumMemberChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(hasMember(name));
      }
      assertEquals("Enum contains more members than expected.", names.length,
          enumDef.getEnumMembers().length);

      return new EnumMemberCheckerIterator(list);
    }
  }

  protected void checkEnumMember(final EnumMember member) {
    assertNotNull("EnumMember is null", member);
    assertNotNull("EnumMember name is null", member.getName());
  }

  public EnumMemberChecker assertEnumMember(final EnumMember member) {
    EnumMemberChecker checker = (EnumMemberChecker) checkers.get(member);
    if (checker == null) {
      checker = createEnumMemberChecker(member);
      checkers.put(member, checker);
    }
    return checker;
  }

  protected EnumMemberChecker createEnumMemberChecker(final EnumMember member) {
    return new EnumMemberChecker(member);
  }

  public class EnumMemberChecker {
    public final EnumMember member;

    public EnumMemberChecker(final EnumMember member) {
      this.member = member;
      checkEnumMember(member);
    }

    public EnumMemberChecker hasValue() {
      assertNotNull(member.getConstantExpression());
      return this;
    }
  }

  public class EnumMemberCheckerIterator
      extends
        CheckerIterator<EnumMemberCheckerIterator, EnumMemberChecker> {

    public EnumMemberCheckerIterator(final List<EnumMemberChecker> list) {
      super(list);
    }

    @Override
    protected EnumMemberCheckerIterator getThis() {
      return this;
    }

    public EnumMemberCheckerIterator hasValue() {
      element.hasValue();
      return this;
    }
  }

  public StructUnionChecker assertStructUnion(final MemberContainer structUnion) {
    return createStructUnionChecker(structUnion);
  }

  protected StructUnionChecker createStructUnionChecker(
      final MemberContainer structUnion) {
    return new StructUnionChecker(structUnion);
  }

  public class StructUnionChecker {
    MemberContainer structUnion;

    StructUnionChecker(final MemberContainer structUnion) {
      this.structUnion = structUnion;
      checkType((Type) structUnion);
    }

    public StructUnionMemberChecker hasMember(final String name) {
      for (final Member member : structUnion.getMembers()) {
        if (name.equals(member.getName())) {
          return assertStructUnionMember(member);
        }
      }
      fail("Enum does not contains a \"" + name + "\" member.");
      // unreachable
      return null;
    }

    public StructUnionMemberCheckerIterator hasMembers(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<StructUnionMemberChecker> list = new ArrayList<StructUnionMemberChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(hasMember(name));
      }
      assertEquals("Struct/Union contains more members than expected.",
          names.length, structUnion.getMembers().length);

      return new StructUnionMemberCheckerIterator(list);
    }
  }

  protected void checkMember(final Member member) {
    assertNotNull("Member is null", member);
    assertNotNull("Member name is null", member.getName());
    checkType(member.getType());
  }

  public StructUnionMemberChecker assertStructUnionMember(final Member member) {
    StructUnionMemberChecker checker = (StructUnionMemberChecker) checkers
        .get(member);
    if (checker == null) {
      checker = createStructUnionMemberChecker(member);
      checkers.put(member, checker);
    }
    return checker;
  }

  protected StructUnionMemberChecker createStructUnionMemberChecker(
      final Member member) {
    return new StructUnionMemberChecker(member);
  }

  public class StructUnionMemberChecker {
    public final Member member;

    public StructUnionMemberChecker(final Member member) {
      this.member = member;
      checkMember(member);
    }

    public StructUnionMemberChecker hasSize() {
      assertNotNull(member.getConstantExpression());
      return this;
    }

    public TypeChecker hasType() {
      return assertType(member.getType());
    }
  }

  public class StructUnionMemberCheckerIterator
      extends
        CheckerIterator<StructUnionMemberCheckerIterator, StructUnionMemberChecker> {

    public StructUnionMemberCheckerIterator(
        final List<StructUnionMemberChecker> list) {
      super(list);
    }

    @Override
    protected StructUnionMemberCheckerIterator getThis() {
      return this;
    }

    public StructUnionMemberCheckerIterator hasSize() {
      element.hasSize();
      return this;
    }

    public TypeChecker hasType() {
      return element.hasType();
    }
  }

  // ---------------------------------------------------------------------------
  // Utility
  // ---------------------------------------------------------------------------

  public static abstract class CheckerIterator<T extends CheckerIterator<?, ?>, U> {
    protected final List<U> list;
    protected Iterator<U>   iterator;
    protected U             element;

    public CheckerIterator(final List<U> list) {
      this.list = list;
    }

    protected abstract T getThis();

    public T whereFirst() {
      iterator = list.iterator();
      if (!iterator.hasNext()) fail("List is empty");
      element = iterator.next();
      return getThis();
    }

    public T andNext() {
      if (!iterator.hasNext()) fail("List has no more element");
      element = iterator.next();
      return getThis();
    }

    public U that() {
      return element;
    }
  }
}
