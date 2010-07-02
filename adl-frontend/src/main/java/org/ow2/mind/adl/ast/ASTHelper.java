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

package org.ow2.mind.adl.ast;

import static java.lang.Integer.parseInt;
import static org.ow2.mind.CommonASTHelper.newNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.control.BindingController;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.PathHelper;
import org.ow2.mind.adl.annotation.predefined.Singleton;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;
import org.ow2.mind.adl.parameter.ast.ArgumentContainer;
import org.ow2.mind.adl.parameter.ast.FormalParameterContainer;
import org.ow2.mind.annotation.AnnotationHelper;

/**
 * Helper methods for ADL AST nodes.
 */
public class ASTHelper {
  protected ASTHelper() {
  }

  // ---------------------------------------------------------------------------
  // Definition helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if the given definition is a type definition.
   * 
   * @param def a definition.
   * @return <code>true</code> if the given definition is a type definition.
   */
  public static boolean isType(final Definition def) {
    return !isPrimitive(def) && !isComposite(def);
  }

  /**
   * Returns <code>true</code> if the given definition is a primitive
   * definition.
   * 
   * @param def a definition.
   * @return <code>true</code> if the given definition is a primitive
   *         definition.
   */
  public static boolean isPrimitive(final Definition def) {
    return def instanceof ImplementationContainer;
  }

  /**
   * Returns <code>true</code> if the given definition is a composite
   * definition.
   * 
   * @param def a definition.
   * @return <code>true</code> if the given definition is a composite
   *         definition.
   */
  public static boolean isComposite(final Definition def) {
    return def instanceof ComponentContainer;
  }

  /**
   * The name of the decoration used to indicate if a definition is singleton.
   * This decoration should only be used by StringTemplate.
   */
  public static final String SINGLETON_DECORATION_NAME = "is-singleton";

  /**
   * Returns <code>true</code> if the given definition has the {@link Singleton}
   * decoration.
   * 
   * @param def a definition.
   * @return <code>true</code> if the given definition has the {@link Singleton}
   *         decoration.
   */
  public static boolean isSingleton(final Definition def) {
    return AnnotationHelper.getAnnotation(def, Singleton.class) != null;
  }

  /**
   * Sets the {@value #SINGLETON_DECORATION_NAME} decoration to
   * <code>true</code> on the given definition.
   * 
   * @param def a definition.
   */
  public static void setSingletonDecoration(final Definition def) {
    def.astSetDecoration(SINGLETON_DECORATION_NAME, Boolean.TRUE);
  }

  /**
   * Returns a new {@link Definition} that correspond to a definition of
   * primitive component.
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link Definition#getName() name} of the definition.
   * @param extended references to the extended definitions (may be
   *          <code>null</code>).
   * @return a new {@link Definition} that correspond to a definition of
   *         primitive component.
   */
  public static Definition newPrimitiveDefinitionNode(
      final NodeFactory nodeFactory, final String name,
      final DefinitionReference... extended) {
    final MindDefinition d = newNode(nodeFactory, "primitive",
        MindDefinition.class, AbstractDefinition.class,
        InterfaceContainer.class, AttributeContainer.class,
        ImplementationContainer.class, FormalParameterContainer.class);

    d.setName(name);
    if (extended != null && extended.length > 0) {
      final DefinitionReferenceContainer extendz = newNode(nodeFactory,
          "extends", DefinitionReferenceContainer.class);
      for (final DefinitionReference ext : extended) {
        extendz.addDefinitionReference(ext);
      }
      d.setExtends(extendz);
    }
    return d;
  }

  /**
   * Returns a new {@link Definition} that correspond to a definition of
   * composite component.
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link Definition#getName() name} of the definition.
   * @param extended references to the extended definitions (may be
   *          <code>null</code>).
   * @return a new {@link Definition} that correspond to a definition of
   *         primitive component.
   */
  public static Definition newCompositeDefinitionNode(
      final NodeFactory nodeFactory, final String name,
      final DefinitionReference... extended) {
    final MindDefinition d = newNode(nodeFactory, "primitive",
        MindDefinition.class, InterfaceContainer.class,
        ComponentContainer.class, BindingController.class,
        FormalParameterContainer.class, FormalTypeParameterContainer.class);

    d.setName(name);
    if (extended != null && extended.length > 0) {
      final DefinitionReferenceContainer extendz = newNode(nodeFactory,
          "extends", DefinitionReferenceContainer.class);
      for (final DefinitionReference ext : extended) {
        extendz.addDefinitionReference(ext);
      }
      d.setExtends(extendz);
    }
    return d;
  }

  // ---------------------------------------------------------------------------
  // Interface helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns the integer value of the {@link MindInterface#getNumberOfElement()
   * numberOfElement} field of the given interface node.
   * 
   * @param itf an {@link Interface} node.
   * @return the integer value of the {@link MindInterface#getNumberOfElement()
   *         numberOfElement} field of the given interface node, or
   *         <code>-1</code> if the given interface does not have a
   *         numberOfElement field.
   */
  public static int getNumberOfElement(final Interface itf) {
    if (!(itf instanceof MindInterface)) return -1;

    final String noe = ((MindInterface) itf).getNumberOfElement();
    if (noe == null)
      return -1;
    else
      return parseInt(noe);
  }

  /**
   * Returns the interface node contained by the given container and having the
   * given name.
   * 
   * @param itfContainer a container.
   * @param name the name of the interface to return.
   * @return the interface node contained by the given container and having the
   *         given name, or <code>null</code> if the given container is not an
   *         {@link InterfaceContainer} or does not contain an interface with
   *         the given name.
   */
  public static Interface getInterface(final Node itfContainer,
      final String name) {
    if (!(itfContainer instanceof InterfaceContainer)) return null;
    for (final Interface itf : ((InterfaceContainer) itfContainer)
        .getInterfaces()) {
      if (name.equals(itf.getName())) return itf;
    }
    return null;
  }

  /**
   * Create a new {@link MindInterface} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link MindInterface} node.
   */
  public static MindInterface newInterfaceNode(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "interface", MindInterface.class);
  }

  /**
   * Create a new client {@link MindInterface} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the created interface.
   * @param signature the signature of the created interface.
   * @return a new {@link MindInterface} node.
   */
  public static MindInterface newClientInterfaceNode(
      final NodeFactory nodeFactory, final String name, final String signature) {
    final MindInterface itf = newInterfaceNode(nodeFactory);
    itf.setRole(TypeInterface.CLIENT_ROLE);
    itf.setName(name);
    itf.setSignature(signature);
    return itf;
  }

  /**
   * Create a new server {@link MindInterface} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the name of the created interface.
   * @param signature the signature of the created interface.
   * @return a new {@link MindInterface} node.
   */
  public static MindInterface newServerInterfaceNode(
      final NodeFactory nodeFactory, final String name, final String signature) {
    final MindInterface itf = newInterfaceNode(nodeFactory);
    itf.setRole(TypeInterface.SERVER_ROLE);
    itf.setName(name);
    itf.setSignature(signature);
    return itf;
  }

  // ---------------------------------------------------------------------------
  // DefinitionReference helper methods
  // ---------------------------------------------------------------------------

  /**
   * The name of the {@link Node#astSetDecoration(String, Object) decoration}
   * used to attach the resolved {@link Definition} to a
   * {@link DefinitionReference}.
   * 
   * @see #setResolvedDefinition(DefinitionReference, Definition)
   * @see #getResolvedDefinition(DefinitionReference, Loader, Map)
   */
  public static final String RESOLVED_DEFINITION_DECORATION_NAME = "resolved-definition";

  /**
   * Sets the resolved {@link Definition} corresponding to the given
   * {@link DefinitionReference}. The decoration that is actually attached to
   * the given <code>defRef</code> node is an instance of
   * {@link DefinitionDecoration}. This imply that, if the <code>defRef</code>
   * is serialized, only the {@link Definition#getName() name} of the definition
   * is serialized (and not the definition AST).
   * 
   * @param defRef a definition reference.
   * @param resolvedDef the corresponding definition.
   * @see #getResolvedDefinition(DefinitionReference, Loader, Map)
   */
  public static void setResolvedDefinition(final DefinitionReference defRef,
      final Definition resolvedDef) {
    defRef.astSetDecoration(RESOLVED_DEFINITION_DECORATION_NAME,
        new DefinitionDecoration(resolvedDef));
  }

  /**
   * Removes the resolved {@link Definition} on the given
   * {@link DefinitionReference}.
   * 
   * @param defRef a definition reference.
   */
  public static void unsetResolvedDefinition(final DefinitionReference defRef) {
    defRef.astSetDecoration(RESOLVED_DEFINITION_DECORATION_NAME, null);
  }

  /**
   * Retrieve the {@link Definition} corresponding to the given
   * {@link DefinitionReference}. Returns <code>null</code>, if the given
   * <code>defRef</code> node has no
   * {@link #RESOLVED_DEFINITION_DECORATION_NAME} decoration. If the decoration
   * attached to the given <code>defRef</code> node contains only the definition
   * name and not the definition AST (i.e. the <code>defRef</code> node has been
   * de-serialized), the given <code>loaderItf</code> is used to load the
   * corresponding definition.
   * 
   * @param defRef a definition reference.
   * @param loaderItf a {@link Loader} interface that is used if only the name
   *          of the definition is attached (and not the definition AST). May be
   *          <code>null</code>.
   * @param context additional parameters. Used only if only the name of the
   *          definition is attached (and not the definition AST). May be
   *          <code>null</code>.
   * @return the {@link Definition} corresponding to the given
   *         {@link DefinitionReference} or <code>null</code>, if the given
   *         <code>defRef</code> node has no
   *         {@link #RESOLVED_DEFINITION_DECORATION_NAME} decoration or if only
   *         the name of the definition is attached and <code>loaderItf</code>
   *         is <code>null</code>.
   * @throws ADLException if an error occurs while
   *           {@link Loader#load(String, Map) loading} the definition using the
   *           given <code>loaderItf</code>.
   */
  public static Definition getResolvedDefinition(
      final DefinitionReference defRef, final Loader loaderItf,
      final Map<Object, Object> context) throws ADLException {
    final DefinitionDecoration definitionDecoration = (DefinitionDecoration) defRef
        .astGetDecoration(RESOLVED_DEFINITION_DECORATION_NAME);
    return getDefinition(definitionDecoration, loaderItf, context);
  }

  /**
   * Create a new {@link DefinitionReference} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link DefinitionReference#getName() name} attribute of the
   *          created node.
   * @return a new {@link DefinitionReference} node.
   */
  public static DefinitionReference newDefinitionReference(
      final NodeFactory nodeFactory, final String name) {
    final DefinitionReference defRef = newNode(nodeFactory,
        "definitionReference", DefinitionReference.class,
        ArgumentContainer.class, TypeArgumentContainer.class);
    defRef.setName(name);
    return defRef;
  }

  // ---------------------------------------------------------------------------
  // Component helper methods
  // ---------------------------------------------------------------------------

  /**
   * The name of the {@link Node#astSetDecoration(String, Object) decoration}
   * used to attach the resolved {@link Definition} to a {@link Component}.
   * 
   * @see #setResolvedComponentDefinition(Component, Definition)
   * @see #getResolvedComponentDefinition(Component, Loader, Map)
   */
  public static final String RESOLVED_COMPONENT_DEFINITION_DECORATION_NAME = "resolved-component-definition";

  /**
   * Sets the resolved {@link Definition} corresponding to the given
   * {@link Component}. The decoration that is actually attached to the given
   * <code>comp</code> node is an instance of {@link DefinitionDecoration}. This
   * imply that, if the <code>comp</code> is serialized, only the
   * {@link Definition#getName() name} of the definition is serialized (and not
   * the definition AST).
   * 
   * @param comp a component node.
   * @param resolvedDef the corresponding definition.
   * @see #getResolvedComponentDefinition(Component, Loader, Map)
   */
  public static void setResolvedComponentDefinition(final Component comp,
      final Definition resolvedDef) {
    comp.astSetDecoration(RESOLVED_COMPONENT_DEFINITION_DECORATION_NAME,
        new DefinitionDecoration(resolvedDef));
  }

  /**
   * Retrieve the {@link Definition} corresponding to the given
   * {@link Component}. Returns <code>null</code>, if the given
   * <code>comp</code> node has no {@link #RESOLVED_DEFINITION_DECORATION_NAME}
   * decoration. If the decoration attached to the given <code>comp</code> node
   * contains only the definition name and not the definition AST (i.e. the
   * <code>comp</code> node has been de-serialized), the given
   * <code>loaderItf</code> is used to load the corresponding definition.
   * 
   * @param comp a component node.
   * @param loaderItf a {@link Loader} interface that is used if only the name
   *          of the definition is attached (and not the definition AST). May be
   *          <code>null</code>.
   * @param context additional parameters. Used only if only the name of the
   *          definition is attached (and not the definition AST). May be
   *          <code>null</code>.
   * @return the {@link Definition} corresponding to the given
   *         {@link DefinitionReference} or <code>null</code>, if the given
   *         <code>defRef</code> node has no
   *         {@link #RESOLVED_DEFINITION_DECORATION_NAME} decoration or if only
   *         the name of the definition is attached and <code>loaderItf</code>
   *         is <code>null</code>.
   * @throws ADLException if an error occurs while
   *           {@link Loader#load(String, Map) loading} the definition using the
   *           given <code>loaderItf</code>.
   */
  public static Definition getResolvedComponentDefinition(final Component comp,
      final Loader loaderItf, final Map<Object, Object> context)
      throws ADLException {
    final DefinitionDecoration definitionDecoration = (DefinitionDecoration) comp
        .astGetDecoration(RESOLVED_COMPONENT_DEFINITION_DECORATION_NAME);
    return getDefinition(definitionDecoration, loaderItf, context);
  }

  /**
   * Returns the component node contained by the given container and having the
   * given name.
   * 
   * @param componentContainer a container.
   * @param name the name of the interface to return.
   * @return the component node contained by the given container and having the
   *         given name, or <code>null</code> if the given container is not an
   *         {@link ComponentContainer} or does not contain a component with the
   *         given name.
   */

  public static Component getComponent(final Node componentContainer,
      final String name) {
    if (!(componentContainer instanceof ComponentContainer)) return null;
    for (final Component subComp : ((ComponentContainer) componentContainer)
        .getComponents()) {
      if (name.equals(subComp.getName())) {
        return subComp;
      }
    }
    return null;
  }

  /**
   * Create a new {@link Component} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link Component#getName() name} attribute of the created
   *          node.
   * @param defRef the reference to the definition that the created component
   *          node is an instance.
   * @return a new {@link Component} node.
   */
  public static Component newComponent(final NodeFactory nodeFactory,
      final String name, final DefinitionReference defRef) {
    final Component comp = newNode(nodeFactory, "component", Component.class);
    comp.setName(name);
    comp.setDefinitionReference(defRef);
    return comp;
  }

  /**
   * Create a new {@link Component} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param name the {@link Component#getName() name} attribute of the created
   *          node.
   * @param definitionName the name of the definition that the created component
   *          node is an instance.
   * @return a new {@link Component} node.
   */
  public static Component newComponent(final NodeFactory nodeFactory,
      final String name, final String definitionName) {
    return newComponent(nodeFactory, name,
        newDefinitionReference(nodeFactory, definitionName));
  }

  // ---------------------------------------------------------------------------
  // Binding helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns the integer value of the {@link Binding#getFromInterfaceNumber()
   * fromInterfaceNumber} field of the given binding node.
   * 
   * @param binding an {@link Binding} node.
   * @return the integer value of the {@link Binding#getFromInterfaceNumber()
   *         fromInterfaceNumber} field of the given binding node, or
   *         <code>-1</code> if the given binding does not have a
   *         fromInterfaceNumber field.
   */

  public static int getFromInterfaceNumber(final Binding binding) {
    final String s = binding.getFromInterfaceNumber();
    if (s == null)
      return -1;
    else
      return parseInt(s);
  }

  /**
   * Returns the integer value of the {@link Binding#getToInterfaceNumber()
   * toInterfaceNumber} field of the given binding node.
   * 
   * @param binding an {@link Binding} node.
   * @return the integer value of the {@link Binding#getToInterfaceNumber()
   *         toInterfaceNumber} field of the given binding node, or
   *         <code>-1</code> if the given binding does not have a
   *         toInterfaceNumber field.
   */

  public static int getToInterfaceNumber(final Binding binding) {
    final String s = binding.getToInterfaceNumber();
    if (s == null)
      return -1;
    else
      return parseInt(s);
  }

  /**
   * Create a new {@link Binding} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link Binding} node.
   */
  public static Binding newBinding(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "binding", Binding.class);
  }

  public static final String FROM_COMPOSITE_CONTROLLER_DECORATION = "from-composite-controller";

  public static void setFromCompositeControllerDecoration(
      final Binding binding, final boolean b) {
    binding.astSetDecoration(FROM_COMPOSITE_CONTROLLER_DECORATION, b);
  }

  public static boolean isFromCompositeControllerDecoration(
      final Binding binding) {
    final Boolean b = (Boolean) binding
        .astGetDecoration(FROM_COMPOSITE_CONTROLLER_DECORATION);
    return b != null && b;
  }

  public static final String TO_COMPOSITE_CONTROLLER_DECORATION = "to-composite-controller";

  public static void setToCompositeControllerDecoration(final Binding binding,
      final boolean b) {
    binding.astSetDecoration(TO_COMPOSITE_CONTROLLER_DECORATION, b);
  }

  public static boolean isToCompositeControllerDecoration(final Binding binding) {
    final Boolean b = (Boolean) binding
        .astGetDecoration(TO_COMPOSITE_CONTROLLER_DECORATION);
    return b != null && b;
  }

  // ---------------------------------------------------------------------------
  // Implementation helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if the given source node refers to a pre-compiled
   * file (i.e. it refers to a file that ends with <code>.o</code>,
   * <code>.a</code>, <code>.so</code> or <code>.dll</code>.
   * 
   * @param src a source node.
   * @return code>true</code> if the given source node refers to a pre-compiled
   *         file.
   */
  public static boolean isPreCompiled(final Source src) {
    final String srcPath = src.getPath();
    if (srcPath == null) return false;
    final String srcExt = PathHelper.getExtension(srcPath);
    return srcExt != null
        && (srcExt.equals("o") || srcExt.equals("a") || srcExt.equals("so") || srcExt
            .equals("dll"));
  }

  /**
   * Create a new {@link Source} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link Source} node.
   */
  public static Source newSource(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "source", Source.class);
  }

  /**
   * Create a new {@link Data} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @return a new {@link Data} node.
   */
  public static Data newData(final NodeFactory nodeFactory) {
    return newNode(nodeFactory, "data", Data.class);
  }

  // ---------------------------------------------------------------------------
  // Factory helper methods
  // ---------------------------------------------------------------------------

  public static final String FACTORY_INSTANTIATED_DEFINITION_DECORATION_NAME = "factory-definition";

  public static void setFactoryInstantiatedDefinition(
      final Definition factoryDefinition,
      final Definition instantiatedDefinition) {
    factoryDefinition.astSetDecoration(
        FACTORY_INSTANTIATED_DEFINITION_DECORATION_NAME,
        new DefinitionDecoration(instantiatedDefinition));
  }

  public static Definition getFactoryInstantiatedDefinition(
      final Definition factoryDefinition, final Loader loaderItf,
      final Map<Object, Object> context) throws ADLException {
    final DefinitionDecoration definitionDecoration = (DefinitionDecoration) factoryDefinition
        .astGetDecoration(FACTORY_INSTANTIATED_DEFINITION_DECORATION_NAME);
    return getDefinition(definitionDecoration, loaderItf, context);
  }

  // ---------------------------------------------------------------------------
  // DefinitionQualifiers helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if the given definition has the
   * {@link AbstractDefinition#getIsAbstract() abstract} qualifier.
   * 
   * @param definition a definition.
   * @return <code>true</code> if the given definition implements the
   *         {@link AbstractDefinition} interface and has the
   *         {@link AbstractDefinition#getIsAbstract() abstract} qualifier.
   */
  public static boolean isAbstract(final Definition definition) {
    return (definition instanceof AbstractDefinition)
        && AbstractDefinition.TRUE
            .equalsIgnoreCase(((AbstractDefinition) definition).getIsAbstract());
  }

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  public static Definition getDefinition(
      final DefinitionDecoration definitionDecoration, final Loader loaderItf,
      final Map<Object, Object> context) throws ADLException {
    if (definitionDecoration == null) {
      return null;
    } else {
      Definition definition = definitionDecoration.getDefinition();
      if (definition == null && loaderItf != null) {
        definition = loaderItf.load(definitionDecoration.getDefinitionName(),
            context);
        definitionDecoration.setDefinition(definition);
      }
      return definition;
    }
  }

  /**
   * Instances of this class are used as decoration by
   * {@link ASTHelper#setResolvedDefinition(DefinitionReference, Definition)}
   * and {@link ASTHelper#setResolvedComponentDefinition(Component, Definition)}
   * . This class contains the attached {@link Definition} AST and its name.
   * When instances of this class are serialized, only the name of the
   * definition is actually serialized (i.e. the definition AST is transient).
   * This implies that de-serialized instance of this class will only contains
   * the name of the definition. This is why
   * {@link ASTHelper#getResolvedDefinition} and
   * {@link ASTHelper#getResolvedComponentDefinition} methods have a
   * <code>loaderItf</code> parameter that is used in that case to re-load the
   * corresponding definition AST.
   */
  public static final class DefinitionDecoration
      implements
        NodeContainerDecoration,
        Serializable {

    private transient Definition definition;
    private final String         definitionName;

    /**
     * Default constructor.
     * 
     * @param definition the attached definition.
     */
    public DefinitionDecoration(final Definition definition) {
      if (definition == null)
        throw new IllegalArgumentException("definition can't be null");
      if (definition.getName() == null)
        throw new IllegalArgumentException("definition's name can't be null");

      this.definition = definition;
      definitionName = definition.getName();
    }

    /**
     * @return the attached definition or <code>null</code> if this object do
     *         not contains a definition.
     */
    public Definition getDefinition() {
      return definition;
    }

    /**
     * Sets the definition AST corresponding to this decoration.
     * 
     * @param definition the definition AST corresponding to this decoration.
     * @throws IllegalArgumentException if the name of the given definition do
     *           not match the name contained by this decoration.
     */
    public void setDefinition(final Definition definition) {
      if (definition == null)
        throw new IllegalArgumentException("definition can't be null");
      if (definition.getName() == null)
        throw new IllegalArgumentException("definition's name can't be null");
      if (!definition.getName().equals(definitionName))
        throw new IllegalArgumentException("Wrong definition name \""
            + definitionName + "\" expected instead of \""
            + definition.getName() + "\".");

      this.definition = definition;
    }

    /**
     * @return the name of the definition attached with this decoration.
     */
    public String getDefinitionName() {
      return definitionName;
    }

    // -------------------------------------------------------------------------
    // Implementation of the NodeContainerDecoration interface
    // -------------------------------------------------------------------------

    public Collection<Node> getNodes() {
      if (definition == null) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        l.add(definition);
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      if (replacements.containsKey(definition)) {
        definition = (Definition) replacements.get(definition);
      }
    }
  }
}
