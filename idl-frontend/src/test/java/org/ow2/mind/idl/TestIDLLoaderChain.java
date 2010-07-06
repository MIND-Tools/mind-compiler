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

package org.ow2.mind.idl;

import java.util.Collection;

import junit.framework.TestCase;

import org.ow2.mind.idl.ASTChecker.IDLChecker;
import org.ow2.mind.idl.ASTChecker.IncludeCheckerIterator;
import org.ow2.mind.idl.ASTChecker.TypeCheckerIterator;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class TestIDLLoaderChain extends TestCase {

  protected IDLLoader  idlLoader;
  protected ASTChecker checker;

  @Override
  protected void setUp() throws Exception {
    idlLoader = IDLLoaderChainFactory.newLoader().loader;
    checker = new ASTChecker();
  }

  public void test1() throws Exception {
    final IDL idl = idlLoader.load("test1", null);
    final IDLChecker idlChecker = checker.assertIDL(idl);
    final TypeCheckerIterator definesType = idlChecker.definesType();
    definesType.whereFirst().isTypedefOf("myint").isPrimitiveType("int");
    definesType.andNext().isTypedefOf("mypointer").isPointerOf().isArrayOf()
        .isPrimitiveType("int");
    definesType.andNext().isStructDef("s").hasMembers("a", "b");

    idlChecker.containsMethods("m1", "m2", "m3", "m4", "m5");
  }

  public void test2() throws Exception {
    final IDL idl = idlLoader.load("/test2.idt", null);
    final IDLChecker idlChecker = checker.assertIDL(idl);
    final IncludeCheckerIterator containedIncludes = idlChecker
        .containsIncludes("\"/test3.idt\"", "\"/foo/bar.idt\"");
    containedIncludes.whereFirst().includes().definesType().whereFirst()
        .isStructDef("s");
    containedIncludes.andNext().includes().containsIncludes("\"/test3.idt\"");
  }

  public void test3() throws Exception {
    final IDL idl = idlLoader.load("/test3.idt", null);
    checker.assertIDL(idl).definesType().whereFirst().isStructDef("s")
        .hasMember("fooItf").hasType().isTypedefRefOf("foo.foo");
    final Collection<InterfaceDefinition> referencedInterfaces = IDLASTHelper
        .getReferencedInterfaces(idl, null, null);
    assertNotNull(referencedInterfaces);
    assertEquals(1, referencedInterfaces.size());
    final InterfaceDefinition foo_foo = referencedInterfaces.iterator().next();
    assertEquals("foo.foo", foo_foo.getName());
    final IDLChecker foo_foo_checker = checker.assertIDL(foo_foo);
    final IDLChecker test2_checker = foo_foo_checker.containsInclude(
        "\"/test2.idt\"").includes();
    final IDLChecker foo_bar_checker = foo_foo_checker.containsInclude(
        "\"/foo/bar.idt\"").includes();

    assertSame(foo_bar_checker.idl,
        test2_checker.containsInclude("\"/foo/bar.idt\"").includes().idl);
    assertSame(idl,
        test2_checker.containsInclude("\"/test3.idt\"").includes().idl);
  }
}
