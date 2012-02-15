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

package org.ow2.mind.adl.idl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class InterfaceDefinitionDecorationHelper {

  public static final String RESOLVED_INTERFACE_DEFINITION_DECORATION = "resolved-interface-definition";

  /**
   * Sets the resolved {@link InterfaceDefinition} corresponding to the
   * signature of the given {@link TypeInterface}. The decoration that is
   * actually attached to the given <code>itf</code> node is an instance of
   * {@link InterfaceDefinitionDecoration}. This imply that, if the
   * <code>itf</code> is serialized, only the
   * {@link InterfaceDefinition#getName() name} of the interface definition is
   * serialized (and not the interface definition AST itself).
   * 
   * @param itf a typed interface.
   * @param resolvedItfDef the interface definition corresponding to the
   *          signature of given interface.
   * @see #getResolvedInterfaceDefinition(TypeInterface, IDLLoader, Map)
   */
  public static void setResolvedInterfaceDefinition(final TypeInterface itf,
      final InterfaceDefinition resolvedItfDef) {
    itf.astSetDecoration(RESOLVED_INTERFACE_DEFINITION_DECORATION,
        new InterfaceDefinitionDecoration(resolvedItfDef));
  }

  /**
   * Retrieve the {@link InterfaceDefinition} corresponding to the signature of
   * the given {@link TypeInterface}. Returns <code>null</code>, if the given
   * <code>itf</code> node has no
   * {@link #RESOLVED_INTERFACE_DEFINITION_DECORATION} decoration. If the
   * decoration attached to the given <code>itf</code> node contains only the
   * interface definition name and not the definition AST (i.e. the
   * <code>itf</code> node has been de-serialized), the given
   * <code>loaderItf</code> is used to load the corresponding interface
   * definition.
   * 
   * @param itf a typed interface.
   * @param loaderItf a {@link IDLLoader} interface that is used if only the
   *          name of the interface definition is attached (and not the
   *          interface definition AST itself). May be <code>null</code>.
   * @param context additional parameters. Used only if only the name of the
   *          interface definition is attached (and not the interface definition
   *          AST itself). May be <code>null</code>.
   * @return the {@link InterfaceDefinition} corresponding to the signature of
   *         the given {@link TypeInterface} or <code>null</code>, if the given
   *         <code>itf</code> node has no
   *         {@link #RESOLVED_INTERFACE_DEFINITION_DECORATION} decoration or if
   *         only the name of the interface definition is attached and
   *         <code>loaderItf</code> is <code>null</code>.
   * @throws ADLException if an error occurs while
   *           {@link IDLLoader#load(String, Map) loading} the interface
   *           definition using the given <code>loaderItf</code>.
   */
  public static InterfaceDefinition getResolvedInterfaceDefinition(
      final TypeInterface itf, final IDLLoader loaderItf,
      final Map<Object, Object> context) throws ADLException {
    final InterfaceDefinitionDecoration definitionDecoration = (InterfaceDefinitionDecoration) itf
        .astGetDecoration(RESOLVED_INTERFACE_DEFINITION_DECORATION);
    return getDefinition(definitionDecoration, loaderItf, context);
  }

  public static final String USED_INTERFACE_DEFINITION_DECORATION = "used-interface-definition";

  public static void addUsedInterfaceDefinition(final Definition definition,
      final InterfaceDefinition usedInterfaceDefinition) {
    InterfaceDefinitionDecorationMap decoration = getUsedInterfaceDefinitionDecoration(definition);
    if (decoration == null) {
      decoration = new InterfaceDefinitionDecorationMap();
      definition.astSetDecoration(USED_INTERFACE_DEFINITION_DECORATION,
          decoration);
    }
    final String name = usedInterfaceDefinition.getName();
    if (!decoration.containsKey(name)) {
      decoration.put(name, new InterfaceDefinitionDecoration(
          usedInterfaceDefinition));
    }
  }

  public static Set<InterfaceDefinition> getUsedInterfaceDefinitions(
      final Definition definition, final IDLLoader loaderItf,
      final Map<Object, Object> context) throws ADLException {
    final InterfaceDefinitionDecorationMap decoration = getUsedInterfaceDefinitionDecoration(definition);
    final HashSet<InterfaceDefinition> result = new HashSet<InterfaceDefinition>();
    if (decoration != null) {
      for (final InterfaceDefinitionDecoration itfDefDeco : decoration.values()) {
        result.add(getDefinition(itfDefDeco, loaderItf, context));
      }
    }
    return result;
  }

  private static InterfaceDefinitionDecorationMap getUsedInterfaceDefinitionDecoration(
      final Definition definition) {
    return (InterfaceDefinitionDecorationMap) definition
        .astGetDecoration(USED_INTERFACE_DEFINITION_DECORATION);
  }

  protected static InterfaceDefinition getDefinition(
      final InterfaceDefinitionDecoration definitionDecoration,
      final IDLLoader loaderItf, final Map<Object, Object> context)
      throws ADLException {
    if (definitionDecoration == null) {
      return null;
    } else {
      InterfaceDefinition definition = definitionDecoration
          .getInterfaceDefinition();
      if (definition == null && loaderItf != null) {
        final IDL idl = loaderItf.load(definitionDecoration.getInterfaceName(),
            context);
        if (!(idl instanceof InterfaceDefinition)) {
          throw new CompilerError(GenericErrors.INTERNAL_ERROR,
              "Referenced IDL is not an interface definition");
        }
        definition = (InterfaceDefinition) idl;
        definitionDecoration.setDefinition(definition);
      }
      return definition;
    }
  }

  public static final class InterfaceDefinitionDecoration
      implements
        NodeContainerDecoration,
        Serializable {

    private transient InterfaceDefinition itfDefinition;
    private final String                  itfName;

    /**
     * Default constructor.
     * 
     * @param definition the attached definition.
     */
    public InterfaceDefinitionDecoration(final InterfaceDefinition definition) {
      if (definition == null)
        throw new IllegalArgumentException("definition can't be null");
      if (definition.getName() == null)
        throw new IllegalArgumentException("definition's name can't be null");

      this.itfDefinition = definition;
      itfName = definition.getName();
    }

    /**
     * @return the attached interface definition or <code>null</code> if this
     *         object do not contains an interface definition.
     */
    public InterfaceDefinition getInterfaceDefinition() {
      return itfDefinition;
    }

    /**
     * Sets the interface definition AST corresponding to this decoration.
     * 
     * @param itfDefinition the interface definition AST corresponding to this
     *          decoration.
     * @throws IllegalArgumentException if the name of the given definition do
     *           not match the name contained by this decoration.
     */
    public void setDefinition(final InterfaceDefinition itfDefinition) {
      if (itfDefinition == null)
        throw new IllegalArgumentException("definition can't be null");
      if (itfDefinition.getName() == null)
        throw new IllegalArgumentException("definition's name can't be null");
      if (!itfDefinition.getName().equals(itfName))
        throw new IllegalArgumentException("Wrong definition name \"" + itfName
            + "\" expected instead of \"" + itfDefinition.getName() + "\".");

      this.itfDefinition = itfDefinition;
    }

    /**
     * @return the name of the interface definition attached with this
     *         decoration.
     */
    public String getInterfaceName() {
      return itfName;
    }

    // -------------------------------------------------------------------------
    // Implementation of the NodeContainerDecoration interface
    // -------------------------------------------------------------------------

    public Collection<Node> getNodes() {
      if (itfDefinition == null) {
        return Collections.emptyList();
      } else {
        final List<Node> l = new ArrayList<Node>();
        l.add(itfDefinition);
        return l;
      }
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      if (replacements.containsKey(itfDefinition)) {
        itfDefinition = (InterfaceDefinition) replacements.get(itfDefinition);
      }
    }
  }

  // -------------------------------------------------------------------------
  // Implementation of the NodeContainerDecoration interface
  // -------------------------------------------------------------------------

  public static final class InterfaceDefinitionDecorationMap
      extends
        HashMap<String, InterfaceDefinitionDecoration>
      implements
        NodeContainerDecoration {

    public Collection<Node> getNodes() {
      final Collection<Node> nodes = new ArrayList<Node>(size());
      for (final InterfaceDefinitionDecoration deco : values()) {
        if (deco.itfDefinition != null) nodes.add(deco.itfDefinition);
      }
      return nodes;
    }

    public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
      for (final Node replacement : replacements.values()) {
        final InterfaceDefinition itfDef = (InterfaceDefinition) replacement;
        final InterfaceDefinitionDecoration deco = get(itfDef.getName());
        deco.itfDefinition = itfDef;
      }
    }
  }

}
