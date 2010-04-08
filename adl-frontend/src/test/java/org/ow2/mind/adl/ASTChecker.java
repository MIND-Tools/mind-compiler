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

package org.ow2.mind.adl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.ow2.mind.adl.ast.ASTHelper.RESOLVED_DEFINITION_DECORATION_NAME;
import static org.ow2.mind.adl.ast.ASTHelper.getResolvedComponentDefinition;
import static org.ow2.mind.adl.parameter.ast.ParameterASTHelper.getInferredParameterType;
import static org.ow2.mind.value.ast.ValueASTHelper.getValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.ast.MindDefinition;
import org.ow2.mind.adl.ast.ASTHelper.DefinitionDecoration;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.parameter.ast.Argument;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameter;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.adl.parameter.ast.ParameterASTHelper.ParameterType;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

public class ASTChecker {

  protected final Map<Node, Object> checkers = new IdentityHashMap<Node, Object>();

  // ---------------------------------------------------------------------------
  // Definition checking
  // ---------------------------------------------------------------------------

  protected void checkDefinition(final Definition definition) {
    assertNotNull("Definition is null", definition);
    assertNotNull("Definition name is null", definition.getName());

    if (definition instanceof MindDefinition
        && ((MindDefinition) definition).getExtends() != null) {
      // definition contains 'extends' sub nodes
      final DefinitionReference[] extendedDefs = ((MindDefinition) definition)
          .getExtends().getDefinitionReferences();
      for (final DefinitionReference extendedDef : extendedDefs) {
        checkDefinitionReference(extendedDef);
      }
    }

    if (definition instanceof InterfaceContainer)
      checkInterfaceContainer((InterfaceContainer) definition);

    if (definition instanceof ComponentContainer)
      checkComponentContainer((ComponentContainer) definition);

    if (definition instanceof FormalTypeParameterContainer)
      checkFormalTypeParameterContainer((FormalTypeParameterContainer) definition);
  }

  public DefinitionChecker assertDefinition(final Definition def) {
    DefinitionChecker checker = (DefinitionChecker) checkers.get(def);
    if (checker == null) {
      checker = createDefinitionChecker(def);
      checkers.put(def, checker);
    }
    return checker;
  }

  protected DefinitionChecker createDefinitionChecker(final Definition def) {
    return new DefinitionChecker(def);
  }

  public class DefinitionChecker {
    public final Definition def;

    public DefinitionChecker(final Definition def) {
      this.def = def;
      checkDefinition(def);
    }

    public DefinitionChecker that() {
      return this;
    }

    public InterfaceChecker containsInterface(final String name) {
      if (!(def instanceof InterfaceContainer))
        fail("definition do not implements InterfaceContainer");

      final InterfaceContainer container = (InterfaceContainer) def;
      for (final Interface itf : container.getInterfaces()) {
        if (name.equals(itf.getName())) return assertInterface(itf, container);
      }
      fail("definition " + def.getName() + " does not contains a \"" + name
          + "\" interface.");
      // unreachable
      return null;

    }

    public InterfaceCheckerIterator containsInterfaces(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<InterfaceChecker> list = new ArrayList<InterfaceChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsInterface(name));
      }
      assertEquals("Definition contains more interfaces than expected.",
          names.length, ((InterfaceContainer) def).getInterfaces().length);
      return new InterfaceCheckerIterator(list);
    }

    public ComponentChecker containsComponent(final String name) {
      if (!(def instanceof ComponentContainer))
        fail("definition do not implements ComponentContainer");

      final ComponentContainer container = (ComponentContainer) def;
      for (final Component subComp : container.getComponents()) {
        if (name.equals(subComp.getName()))
          return assertComponent(subComp, container);
      }
      fail("definition " + def.getName() + " does not contains a \"" + name
          + "\" sub component.");
      // unreachable
      return null;
    }

    public ComponentCheckerIterator containsComponents(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<ComponentChecker> list = new ArrayList<ComponentChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsComponent(name));
      }
      assertEquals("Definition contains more sub-components than expected.",
          names.length, ((ComponentContainer) def).getComponents().length);
      return new ComponentCheckerIterator(list);
    }

    public FormalTypeParameterChecker containsFormalTypeParameter(
        final String name) {
      if (!(def instanceof FormalTypeParameterContainer))
        fail("definition do not implements FormalTypeParameterContainer");

      final FormalTypeParameterContainer container = (FormalTypeParameterContainer) def;
      for (final FormalTypeParameter formalTypeParameter : container
          .getFormalTypeParameters()) {
        if (name.equals(formalTypeParameter.getName()))
          return assertFormalTypeParameter(formalTypeParameter, container);
      }
      fail("definition " + def.getName() + " does not contains a \"" + name
          + "\" formal type parameter.");
      // unreachable
      return null;
    }

    public FormalTypeParameterCheckerIterator containsFormalTypeParameters(
        final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<FormalTypeParameterChecker> list = new ArrayList<FormalTypeParameterChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsFormalTypeParameter(name));
      }
      assertEquals(
          "Definition contains more FormalTypeParameter than expected.",
          names.length, ((FormalTypeParameterContainer) def)
              .getFormalTypeParameters().length);

      return new FormalTypeParameterCheckerIterator(list);
    }

    public FormalParameterChecker containsFormalParameter(final String name) {
      if (!(def instanceof FormalParameterContainer))
        fail("definition do not implements FormalParameterContainer");

      final FormalParameterContainer container = (FormalParameterContainer) def;
      for (final FormalParameter formalParameter : container
          .getFormalParameters()) {
        if (name.equals(formalParameter.getName()))
          return assertFormalParameter(formalParameter, container);
      }
      fail("definition " + def.getName() + " does not contains a \"" + name
          + "\" formal parameter.");
      // unreachable
      return null;
    }

    public FormalParameterCheckerIterator containsFormalParameters(
        final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<FormalParameterChecker> list = new ArrayList<FormalParameterChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsFormalParameter(name));
      }
      assertEquals("Definition contains more FormalParameter than expected.",
          names.length,
          ((FormalParameterContainer) def).getFormalParameters().length);

      return new FormalParameterCheckerIterator(list);
    }

    public AttributeChecker containsAttribute(final String name) {
      if (!(def instanceof AttributeContainer))
        fail("definition do not implements AttributeContainer");

      final AttributeContainer container = (AttributeContainer) def;
      for (final Attribute attribute : container.getAttributes()) {
        if (name.equals(attribute.getName()))
          return assertAttribute(attribute, container);
      }
      fail("definition " + def.getName() + " does not contains a \"" + name
          + "\" attribute.");
      // unreachable
      return null;
    }

    public AttributeCheckerIterator containsAttributes(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<AttributeChecker> list = new ArrayList<AttributeChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsAttribute(name));
      }
      assertEquals("Definition contains more attribute than expected.",
          names.length, ((AttributeContainer) def).getAttributes().length);

      return new AttributeCheckerIterator(list);
    }

    public DefinitionChecker isSingleton() {
      assertTrue("Definition is not singleton", ASTHelper.isSingleton(def));
      assertTrue("Definition does not contain the is-singleton decoration", def
          .astGetDecoration(ASTHelper.SINGLETON_DECORATION_NAME) != null
          && (Boolean) def
              .astGetDecoration(ASTHelper.SINGLETON_DECORATION_NAME));
      return this;
    }

    public DefinitionChecker isMultiton() {
      assertFalse("Definition is singleton", ASTHelper.isSingleton(def));
      assertTrue("Definition contains the is-singleton decoration", def
          .astGetDecoration(ASTHelper.SINGLETON_DECORATION_NAME) == null);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Definition Reference checking
  // ---------------------------------------------------------------------------

  protected Definition checkDefinitionReference(final DefinitionReference defRef) {
    assertNotNull("Given definition reference is null", defRef);
    assertNotNull("definition reference name is null", defRef.getName());

    final DefinitionDecoration deco = (DefinitionDecoration) defRef
        .astGetDecoration(RESOLVED_DEFINITION_DECORATION_NAME);
    assertNotNull(RESOLVED_DEFINITION_DECORATION_NAME + " decoration is null.",
        deco);
    final Definition resolvedDefinition = deco.getDefinition();
    assertNotNull(RESOLVED_DEFINITION_DECORATION_NAME + " definition is null.",
        resolvedDefinition);

    assertEquals(
        "Name of resolved definition is not coherent with decoration.", deco
            .getDefinitionName(), resolvedDefinition.getName());

    assertEquals(
        "Name of resolved definition does not match definition reference name.",
        defRef.getName(), resolvedDefinition.getName());

    return resolvedDefinition;
  }

  DefinitionReferenceChecker assertDefinitionReference(
      final DefinitionReference defRef) {
    DefinitionReferenceChecker checker = (DefinitionReferenceChecker) checkers
        .get(defRef);
    if (checker == null) {
      checker = createDefinitionReferenceChecker(defRef);
      checkers.put(defRef, checker);
    }
    return checker;
  }

  protected DefinitionReferenceChecker createDefinitionReferenceChecker(
      final DefinitionReference defRef) {
    return new DefinitionReferenceChecker(defRef);
  }

  public class DefinitionReferenceChecker {
    public final DefinitionReference defRef;

    protected DefinitionReferenceChecker(final DefinitionReference defRef) {
      this.defRef = defRef;
      checkDefinitionReference(defRef);
    }

    public DefinitionReferenceChecker that() {
      return this;
    }

    public DefinitionReferenceChecker references(final String name) {
      assertEquals("Unexpected referenced definition", name, defRef.getName());
      return this;
    }

    public ArgumentChecker containsArgument(final String name) {
      if (!(defRef instanceof ArgumentContainer))
        fail("definition reference do not implements ArgumentContainer");

      final ArgumentContainer container = (ArgumentContainer) defRef;
      for (final Argument argument : container.getArguments()) {
        if (name.equals(argument.getName()))
          return assertArgument(argument, container);
      }
      fail("definition reference does not contains a \"" + name
          + "\" argument.");
      // unreachable
      return null;
    }

    public ArgumentCheckerIterator containsArguments(final String... names) {
      final Set<String> nameSet = new HashSet<String>();
      final List<ArgumentChecker> list = new ArrayList<ArgumentChecker>(
          names.length);
      for (final String name : names) {
        assertTrue("Duplucated string in given names " + names, nameSet
            .add(name));
        list.add(containsArgument(name));
      }
      assertEquals("Definition contains more FormalParameter than expected.",
          names.length, ((ArgumentContainer) defRef).getArguments().length);

      return new ArgumentCheckerIterator(list);
    }
  }

  // ---------------------------------------------------------------------------
  // Interface checking
  // ---------------------------------------------------------------------------

  protected void checkInterface(final Interface itf) {
    assertNotNull("Given interface is null", itf);
    assertNotNull("interface name is null", itf.getName());
  }

  protected void checkInterfaceContainer(final InterfaceContainer container) {
    final Set<String> names = new HashSet<String>();
    for (final Interface itf : container.getInterfaces()) {
      assertTrue("Duplicated interface name.", names.add(itf.getName()));
      checkInterface(itf);
    }
  }

  public InterfaceChecker assertInterface(final Interface itf,
      final InterfaceContainer container) {
    InterfaceChecker checker = (InterfaceChecker) checkers.get(itf);
    if (checker == null) {
      checker = createInterfaceChecker(itf, container);
      checkers.put(itf, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected InterfaceChecker createInterfaceChecker(final Interface itf,
      final InterfaceContainer container) {
    return new InterfaceChecker(itf, container);
  }

  public class InterfaceChecker {
    public final Interface          itf;
    public final InterfaceContainer container;

    protected InterfaceChecker(final Interface itf,
        final InterfaceContainer container) {
      this.itf = itf;
      this.container = container;
      checkInterface(itf);
    }

    public InterfaceChecker isClient() {
      assertTrue("Interface is not a TypeInterface",
          itf instanceof TypeInterface);
      assertTrue("Interface is not a client interface", TypeInterfaceUtil
          .isClient(itf));
      return this;
    }

    public InterfaceChecker isServer() {
      assertTrue("Interface is not a TypeInterface",
          itf instanceof TypeInterface);
      assertTrue("Interface is not a client interface", TypeInterfaceUtil
          .isServer(itf));
      return this;
    }

    public InterfaceChecker isMandatory() {
      assertTrue("Interface is not a TypeInterface",
          itf instanceof TypeInterface);
      assertTrue("Interface is not a client interface", TypeInterfaceUtil
          .isMandatory(itf));
      return this;
    }

    public InterfaceChecker isOptional() {
      assertTrue("Interface is not a TypeInterface",
          itf instanceof TypeInterface);
      assertTrue("Interface is not a client interface", TypeInterfaceUtil
          .isOptional(itf));
      return this;
    }

    public InterfaceChecker hasSignature(final String signature) {
      assertTrue("Interface is not a TypeInterface",
          itf instanceof TypeInterface);
      assertEquals("Unexpected interface signature", signature,
          ((TypeInterface) itf).getSignature());
      return this;
    }
  }

  public static class InterfaceCheckerIterator
      extends
        CheckerIterator<InterfaceCheckerIterator, InterfaceChecker> {
    public InterfaceCheckerIterator(final List<InterfaceChecker> list) {
      super(list);
    }

    @Override
    protected InterfaceCheckerIterator getThis() {
      return this;
    }

    public InterfaceCheckerIterator isClient() {
      element.isClient();
      return this;
    }

    public InterfaceCheckerIterator isServer() {
      element.isServer();
      return this;
    }

    public InterfaceCheckerIterator isMandatory() {
      element.isMandatory();
      return this;
    }

    public InterfaceCheckerIterator isOptional() {
      element.isOptional();
      return this;
    }

    public InterfaceCheckerIterator hasSignature(final String signature) {
      element.hasSignature(signature);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Component checking
  // ---------------------------------------------------------------------------

  protected void checkComponent(final Component component) {
    assertNotNull("Given component is null", component);
    assertNotNull("component name is null", component.getName());
    if (component.getDefinitionReference() != null)
      checkDefinitionReference(component.getDefinitionReference());
  }

  protected void checkComponentContainer(final ComponentContainer container) {
    final Set<String> names = new HashSet<String>();
    for (final Component comp : container.getComponents()) {
      assertTrue("Duplicated sub-component name.", names.add(comp.getName()));
      checkComponent(comp);
    }
  }

  public ComponentChecker assertComponent(final Component comp,
      final ComponentContainer container) {
    ComponentChecker checker = (ComponentChecker) checkers.get(comp);
    if (checker == null) {
      checker = createComponentChecker(comp, container);
      checkers.put(comp, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected ComponentChecker createComponentChecker(final Component comp,
      final ComponentContainer container) {
    return new ComponentChecker(comp, container);
  }

  public class ComponentChecker {
    public final Component          comp;
    public final ComponentContainer container;

    protected ComponentChecker(final Component comp,
        final ComponentContainer container) {
      this.comp = comp;
      this.container = container;
      assertTrue("Unexpected container", container instanceof Definition);
      checkComponent(comp);
    }

    public ComponentChecker that() {
      return this;
    }

    public DefinitionReferenceChecker isReferencing(final String name) {
      return assertDefinitionReference(comp.getDefinitionReference())
          .references(name);
    }

    public DefinitionChecker isAnInstanceOf(final String defName) {
      final Definition compDef;
      try {
        compDef = getResolvedComponentDefinition(comp, null, null);
      } catch (final ADLException e) {
        // never happen
        return null;
      }
      assertNotNull("Component does not contains a definition reference",
          compDef);
      if (defName != null)
        assertEquals("Unexpected definition for component " + comp.getName(),
            defName, compDef.getName());

      return new DefinitionChecker(compDef);
    }

    public ComponentChecker isAnInstanceOfSingleton() {
      isAnInstanceOf(null).isSingleton();
      return this;
    }

    public ComponentChecker isAnInstanceOfMultiton() {
      isAnInstanceOf(null).isMultiton();
      return this;
    }

    public FormalTypeParameterChecker referencesFormalTypeParameter(
        final String name) {
      assertTrue("Compoenent does not implement FormalTypeParameterReference",
          comp instanceof FormalTypeParameterReference);

      final String typeParameterReference = ((FormalTypeParameterReference) comp)
          .getTypeParameterReference();
      assertNotNull("Compoenent does not reference a formal type parameter",
          typeParameterReference);
      assertEquals(
          "Compoenent does not reference the expected formal type parameter",
          name, typeParameterReference);

      return assertDefinition((Definition) container)
          .containsFormalTypeParameter(name);
    }
  }

  public static class ComponentCheckerIterator
      extends
        CheckerIterator<ComponentCheckerIterator, ComponentChecker> {

    public ComponentCheckerIterator(final List<ComponentChecker> list) {
      super(list);
    }

    @Override
    protected ComponentCheckerIterator getThis() {
      return this;
    }

    public DefinitionReferenceChecker isReferencing(final String name) {
      return element.isReferencing(name);
    }

    public ComponentCheckerIterator isAnInstanceOf(final String defName) {
      element.isAnInstanceOf(defName);
      return this;
    }

    public ComponentCheckerIterator isAnInstanceOfSingleton() {
      element.isAnInstanceOfSingleton();
      return this;
    }

    public ComponentCheckerIterator isAnInstanceOfMultiton() {
      element.isAnInstanceOfMultiton();
      return this;
    }

    public ComponentCheckerIterator referencesFormalTypeParameter(
        final String name) {
      element.referencesFormalTypeParameter(name);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Attribute checking
  // ---------------------------------------------------------------------------

  protected void checkAttribute(final Attribute attribute) {
    assertNotNull("Given attribute is null", attribute);
    assertNotNull("Attribute name is null", attribute.getName());
  }

  protected void checkAttributeContainer(final AttributeContainer container) {
    final Set<String> names = new HashSet<String>();
    for (final Attribute attribute : container.getAttributes()) {
      assertTrue("Duplicated FormalTypeParameter name.", names.add(attribute
          .getName()));
      checkAttribute(attribute);
    }
  }

  public AttributeChecker assertAttribute(final Attribute attribute,
      final AttributeContainer container) {
    AttributeChecker checker = (AttributeChecker) checkers.get(attribute);
    if (checker == null) {
      checker = createAttributeChecker(attribute, container);
      checkers.put(attribute, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected AttributeChecker createAttributeChecker(final Attribute attribute,
      final AttributeContainer container) {
    return new AttributeChecker(attribute, container);
  }

  public class AttributeChecker {

    public final Attribute          attribute;
    public final AttributeContainer container;

    protected AttributeChecker(final Attribute attribute,
        final AttributeContainer container) {
      this.attribute = attribute;
      this.container = container;
      assertTrue("Unexpected container", container instanceof Definition);
      checkAttribute(attribute);
    }

    public AttributeChecker hasType(final String type) {
      assertEquals("Unexpected attribute type ", type, attribute.getType());
      return this;
    }

    public AttributeChecker and() {
      return this;
    }

    public ValueChecker value() {
      assertNotNull(attribute.getValue());
      return assertValue(attribute.getValue());
    }

    public void hasNoValue() {
      assertNull(attribute.getValue());
    }
  }

  public static class AttributeCheckerIterator
      extends
        CheckerIterator<AttributeCheckerIterator, AttributeChecker> {

    public AttributeCheckerIterator(final List<AttributeChecker> list) {
      super(list);
    }

    @Override
    protected AttributeCheckerIterator getThis() {
      return this;
    }

    public AttributeCheckerIterator hasType(final String type) {
      element.hasType(type);
      return this;
    }

    public AttributeCheckerIterator and() {
      return this;
    }

    public AttributeCheckerIterator valueIs(final int v) {
      element.value().is(v);
      return this;
    }

    public AttributeCheckerIterator valueIs(final String v) {
      element.value().is(v);
      return this;
    }

    public AttributeCheckerIterator valueReferences(final String ref) {
      element.value().references(ref);
      return this;
    }

    public AttributeCheckerIterator hasNoValue() {
      element.hasNoValue();
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // FormalTypeParameter checking
  // ---------------------------------------------------------------------------

  protected void checkFormalTypeParameter(
      final FormalTypeParameter formalTypeParameter) {
    assertNotNull("Given FormalTypeParameter is null", formalTypeParameter);
    assertNotNull("FormalTypeParameter name is null", formalTypeParameter
        .getName());
    if (formalTypeParameter.getDefinitionReference() != null)
      checkDefinitionReference(formalTypeParameter.getDefinitionReference());
  }

  protected void checkFormalTypeParameterContainer(
      final FormalTypeParameterContainer container) {
    final Set<String> names = new HashSet<String>();
    for (final FormalTypeParameter formalTypeParameter : container
        .getFormalTypeParameters()) {
      assertTrue("Duplicated FormalTypeParameter name.", names
          .add(formalTypeParameter.getName()));
      checkFormalTypeParameter(formalTypeParameter);
    }
  }

  public FormalTypeParameterChecker assertFormalTypeParameter(
      final FormalTypeParameter formalTypeParameter,
      final FormalTypeParameterContainer container) {
    FormalTypeParameterChecker checker = (FormalTypeParameterChecker) checkers
        .get(formalTypeParameter);
    if (checker == null) {
      checker = createFormalTypeParameterChecker(formalTypeParameter, container);
      checkers.put(formalTypeParameter, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected FormalTypeParameterChecker createFormalTypeParameterChecker(
      final FormalTypeParameter formalTypeParameter,
      final FormalTypeParameterContainer container) {
    return new FormalTypeParameterChecker(formalTypeParameter, container);
  }

  public class FormalTypeParameterChecker {
    public final FormalTypeParameter          formalTypeParameter;
    public final FormalTypeParameterContainer container;

    protected FormalTypeParameterChecker(
        final FormalTypeParameter formalTypeParameter,
        final FormalTypeParameterContainer container) {
      this.formalTypeParameter = formalTypeParameter;
      this.container = container;
      assertTrue("Unexpected container", container instanceof Definition);
      checkFormalTypeParameter(formalTypeParameter);
    }

    public DefinitionChecker conformsTo(final String defName) {
      assertEquals("Unexpected type for formalTypeParameter "
          + formalTypeParameter.getName(), defName, formalTypeParameter
          .getDefinitionReference().getName());

      return assertDefinition(checkDefinitionReference(formalTypeParameter
          .getDefinitionReference()));
    }
  }

  public static class FormalTypeParameterCheckerIterator
      extends
        CheckerIterator<FormalTypeParameterCheckerIterator, FormalTypeParameterChecker> {

    public FormalTypeParameterCheckerIterator(
        final List<FormalTypeParameterChecker> list) {
      super(list);
    }

    @Override
    protected FormalTypeParameterCheckerIterator getThis() {
      return this;
    }

    public FormalTypeParameterCheckerIterator conformsTo(final String defName) {
      element.conformsTo(defName);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // FormalParameter checking
  // ---------------------------------------------------------------------------

  protected void checkFormalParameter(final FormalParameter formalParameter) {
    assertNotNull("Given FormalParameter is null", formalParameter);
    assertNotNull("FormalParameter name is null", formalParameter.getName());
  }

  protected void checkFormalParameterContainer(
      final FormalParameterContainer container) {
    final Set<String> names = new HashSet<String>();
    for (final FormalParameter formalParameter : container
        .getFormalParameters()) {
      assertTrue("Duplicated FormalTypeParameter name.", names
          .add(formalParameter.getName()));
      checkFormalParameter(formalParameter);
    }
  }

  public FormalParameterChecker assertFormalParameter(
      final FormalParameter formalParameter,
      final FormalParameterContainer container) {
    FormalParameterChecker checker = (FormalParameterChecker) checkers
        .get(formalParameter);
    if (checker == null) {
      checker = createFormalParameterChecker(formalParameter, container);
      checkers.put(formalParameter, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected FormalParameterChecker createFormalParameterChecker(
      final FormalParameter formalParameter,
      final FormalParameterContainer container) {
    return new FormalParameterChecker(formalParameter, container);
  }

  public class FormalParameterChecker {
    public final FormalParameter          formalParameter;
    public final FormalParameterContainer container;

    protected FormalParameterChecker(final FormalParameter formalParameter,
        final FormalParameterContainer container) {
      this.formalParameter = formalParameter;
      this.container = container;
      assertTrue("Unexpected container", container instanceof Definition);
      checkFormalParameter(formalParameter);
    }

    public void hasType(final ParameterType type) {
      final ParameterType inferredParameterType = getInferredParameterType(formalParameter);
      assertNotNull("FormalParamter has no inferred type",
          inferredParameterType);
      assertEquals("Unexpected type for formalParameter "
          + formalParameter.getName(), type, inferredParameterType);
    }
  }

  public static class FormalParameterCheckerIterator
      extends
        CheckerIterator<FormalParameterCheckerIterator, FormalParameterChecker> {

    public FormalParameterCheckerIterator(
        final List<FormalParameterChecker> list) {
      super(list);
    }

    @Override
    protected FormalParameterCheckerIterator getThis() {
      return this;
    }

    public FormalParameterCheckerIterator hasType(final ParameterType type) {
      element.hasType(type);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Argument checking
  // ---------------------------------------------------------------------------

  protected void checkArgument(final Argument argument) {
    assertNotNull("Given Argument is null", argument);
    assertNotNull("Argument name is null", argument.getName());
    assertNotNull("Argument value is null", argument.getValue());
  }

  public ArgumentChecker assertArgument(final Argument argument,
      final ArgumentContainer container) {

    ArgumentChecker checker = (ArgumentChecker) checkers.get(argument);
    if (checker == null) {
      checker = createArgumentChecker(argument, container);
      checkers.put(argument, checker);
    } else {
      assertSame("Unexpected container", container, checker.container);
    }
    return checker;
  }

  protected ArgumentChecker createArgumentChecker(final Argument argument,
      final ArgumentContainer container) {
    return new ArgumentChecker(argument, container);
  }

  public class ArgumentChecker {
    public final Argument          argument;
    public final ArgumentContainer container;

    protected ArgumentChecker(final Argument argument,
        final ArgumentContainer container) {
      this.argument = argument;
      this.container = container;
      checkArgument(argument);
    }

    public ValueChecker value() {
      return assertValue(argument.getValue());
    }

    public ArgumentChecker valueIs(final int v) {
      assertValue(argument.getValue()).is(v);
      return this;
    }

    public ArgumentChecker valueIs(final String v) {
      assertValue(argument.getValue()).is(v);
      return this;
    }

    public ArgumentChecker valueReferences(final String ref) {
      assertValue(argument.getValue()).references(ref);
      return this;
    }
  }

  public static class ArgumentCheckerIterator
      extends
        CheckerIterator<ArgumentCheckerIterator, ArgumentChecker> {

    public ArgumentCheckerIterator(final List<ArgumentChecker> list) {
      super(list);
    }

    @Override
    protected ArgumentCheckerIterator getThis() {
      return this;
    }

    public ValueChecker value() {
      return element.value();
    }

    public ArgumentCheckerIterator valueIs(final int v) {
      element.valueIs(v);
      return this;
    }

    public ArgumentCheckerIterator valueIs(final String v) {
      element.valueIs(v);
      return this;
    }

    public ArgumentCheckerIterator valueReferences(final String ref) {
      element.valueReferences(ref);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Value checking
  // ---------------------------------------------------------------------------

  protected void checkValue(final Value value) {
    assertNotNull("Given value is null", value);
    assertTrue("Unknown value type " + value.astGetType(),
        (value instanceof NumberLiteral) || (value instanceof Reference)
            || (value instanceof StringLiteral));
  }

  public ValueChecker assertValue(final Value value) {

    ValueChecker checker = (ValueChecker) checkers.get(value);
    if (checker == null) {
      checker = createValueChecker(value);
      checkers.put(value, checker);
    }
    return checker;
  }

  protected ValueChecker createValueChecker(final Value value) {
    return new ValueChecker(value);
  }

  public class ValueChecker {
    public final Value value;

    public ValueChecker(final Value value) {
      this.value = value;
      checkValue(value);
    }

    public void is(final int v) {
      assertTrue("Value is not an IntegerLiteral",
          value instanceof NumberLiteral);
      assertEquals("Unexpected integer value", v,
          getValue((NumberLiteral) value));
    }

    public void is(final String v) {
      assertTrue("Value is not an StringLiteral",
          value instanceof StringLiteral);
      assertEquals("Unexpected string value", v, ((StringLiteral) value)
          .getValue());
    }

    public void references(final String ref) {
      assertTrue("Value is not a reference", value instanceof Reference);
      assertEquals("Unexpected reference", ref, ((Reference) value).getRef());
    }
  }

  public static class ValueCheckerIterator
      extends
        CheckerIterator<ValueCheckerIterator, ValueChecker> {

    public ValueCheckerIterator(final List<ValueChecker> list) {
      super(list);
    }

    @Override
    protected ValueCheckerIterator getThis() {
      return this;
    }

    public ValueCheckerIterator is(final int v) {
      element.is(v);
      return this;
    }

    public ValueCheckerIterator is(final String v) {
      element.is(v);
      return this;
    }

    public ValueCheckerIterator references(final String ref) {
      element.references(ref);
      return this;
    }
  }

  // ---------------------------------------------------------------------------
  // Utility
  // ---------------------------------------------------------------------------

  public static abstract class CheckerIterator<T extends CheckerIterator, U> {
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
