/**
 * Copyright (C) 2009 STMicroelectronics
 * Copyright (C) 2014 Schneider-Electric
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
 * Contributors: Stephane Seyvoz
 */

package org.ow2.mind.adl;

import static org.ow2.mind.adl.ast.ASTHelper.isComposite;
import static org.ow2.mind.adl.ast.ASTHelper.isPrimitive;
import static org.ow2.mind.adl.ast.ASTHelper.isType;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.AbstractDefinition;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.ast.ExtendsDecoration;
import org.ow2.mind.adl.ast.MindDefinition;
import org.ow2.mind.adl.ast.SubDefinitionsDecoration;
import org.ow2.mind.error.ErrorManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * This delegating loader merges a definitions with the definitions it extends.
 */
public class ExtendsLoader extends AbstractDelegatingLoader {

  @Inject
  protected ErrorManager                errorManagerItf;

  /**
   * The name of the {@link DefinitionReferenceResolver} to be injected in this
   * class.
   */
  public static final String            EXTENDS_DEFINITION_RESOLVER = "ExtendsDefinitionResolver";

  @Inject
  @Named(EXTENDS_DEFINITION_RESOLVER)
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

  /**
   * The name of the {@link NodeMerger} to be injected in this class.
   */
  public static final String            EXTENDS_NODE_MERGER         = "ExtendsNodeMerger";

  @Inject
  @Named(EXTENDS_NODE_MERGER)
  protected NodeMerger                  nodeMergerItf;

  /**
   * The name of the {@link #nameAttributes} to be injected in this class.
   */
  public static final String            ADL_ID_ATTRIBUTES           = "ADL_ID_Attributes";

  /**
   * The names of the "name" attribute for each AST node type. This map
   * associates the names of the "name" attribute, used to detect overridden
   * elements, to each AST node type that has such an attribute.
   */
  @Inject
  @Named(ADL_ID_ATTRIBUTES)
  protected Map<String, String>         nameAttributes;

  /**
   * Used to retrieve super types Definitions from DefinitionReferences.
   */
  @Inject
  protected Loader                      loaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    Definition d = clientLoader.load(name, context);
    if (d instanceof MindDefinition)
      d = resolveExtends((MindDefinition) d, context);

    return d;
  }

  protected MindDefinition resolveExtends(MindDefinition d,
      final Map<Object, Object> context) throws ADLException {
    if (d.getExtends() == null) return d;

    // abstract attribute is not inherited. Store the "abstract" attribute
    // before merging
    final boolean isAbstract = ASTHelper.isAbstract(d);
    final DefinitionReference[] extendedDefs = d.getExtends()
        .getDefinitionReferences();

    // keep inheritance information as a decoration, since the direct "extends"
    // is removed afterwards
    final ExtendsDecoration list = new ExtendsDecoration();
    for (final DefinitionReference extend : extendedDefs) {
      // allow retrieving super-types from a definition
      list.add(extend);
    }
    d.astSetDecoration("extends", list);

    // save "sub-definitions" before merge
    final Object subDefsObject = d.astGetDecoration("sub-definitions");
    SubDefinitionsDecoration savedSubDefinitionsDecoration = null;
    if (subDefsObject instanceof SubDefinitionsDecoration)
      savedSubDefinitionsDecoration = (SubDefinitionsDecoration) subDefsObject;

    // cleanup
    d.setExtends(null);

    if (extendedDefs.length > 0) {
      final Kind kind = Kind.fromDefinition(d);

      // first resolve extended list
      Definition superDef = null;
      for (final DefinitionReference extend : extendedDefs) {
        final Definition extendedDefinition = definitionReferenceResolverItf
            .resolve(extend, d, context);

        // if the definition has not been resolved correctly, ignore it.
        if (ASTHelper.isUnresolvedDefinitionNode(extendedDefinition)) continue;

        final Kind extendedKind = Kind.fromDefinition(extendedDefinition);
        switch (kind) {
          case TYPE :
            if (extendedKind == Kind.PRIMITIVE) {
              errorManagerItf.logError(
                  ADLErrors.INVALID_EXTENDS_TYPE_EXTENDS_PRIMITIVE,
                  extend.getName());
              continue;
            } else if (extendedKind == Kind.COMPOSITE) {
              errorManagerItf.logError(
                  ADLErrors.INVALID_EXTENDS_TYPE_EXTENDS_COMPOSITE,
                  extend.getName());
              continue;
            }
            break;
          case PRIMITIVE :
            if (extendedKind == Kind.COMPOSITE) {
              errorManagerItf.logError(
                  ADLErrors.INVALID_EXTENDS_PRIMITIVE_EXTENDS_COMPOSITE,
                  extend.getName());
              continue;
            }
            break;
          case COMPOSITE :
            if (extendedKind == Kind.PRIMITIVE) {
              errorManagerItf.logError(
                  ADLErrors.INVALID_EXTENDS_COMPOSITE_EXTENDS_PRIMITIVE,
                  extend.getName());
              continue;
            }
            break;
        }

        if (superDef == null)
          superDef = extendedDefinition;
        else
          superDef = mergeDef(extendedDefinition, superDef, extend);
      }

      if (superDef != null) {
        // second merge d with superDef only if superDef has been correctly
        // resolved.
        d = (MindDefinition) mergeDef(d, superDef,
            extendedDefs[extendedDefs.length - 1]);
      }

      // restore original sub-definition decoration instead of the merge
      // result one
      d.astSetDecoration("sub-definitions", savedSubDefinitionsDecoration);

      for (final DefinitionReference extend : extendedDefs) {
        /*
         * allow retrieving sub-types from parent we do it post-merge for the
         * current node not to be decorated with its own parent sub-definition
         * info, meaning itself.
         */
        decorateParentDefinitionWithSubDefinition(extend, d, context);
      }

      // restore the "abstract" attribute
      if (d instanceof AbstractDefinition) {
        if (isAbstract)
          ((AbstractDefinition) d).setIsAbstract(AbstractDefinition.TRUE);
        else
          ((AbstractDefinition) d).setIsAbstract(null);
      }
    }
    return d;
  }

  private void decorateParentDefinitionWithSubDefinition(
      final DefinitionReference extend, final MindDefinition d,
      final Map<Object, Object> context) throws ADLException {

    SubDefinitionsDecoration subDefinitionsDecoration = null;

    final Definition extendedDefinition = definitionReferenceResolverItf
        .resolve(extend, d, context);

    // if the definition has not been resolved correctly, ignore it.
    if (ASTHelper.isUnresolvedDefinitionNode(extendedDefinition)) return;

    final Object subDefsDecorationObject = extendedDefinition
        .astGetDecoration("sub-definitions");

    if (subDefsDecorationObject != null) {
      if (subDefsDecorationObject instanceof SubDefinitionsDecoration) {
        subDefinitionsDecoration = (SubDefinitionsDecoration) subDefsDecorationObject;
        subDefinitionsDecoration.add(d);
      }
    } else {
      subDefinitionsDecoration = new SubDefinitionsDecoration();
      subDefinitionsDecoration.add(d);
      extendedDefinition.astSetDecoration("sub-definitions",
          subDefinitionsDecoration);
    }

  }

  private static enum Kind {
    TYPE, PRIMITIVE, COMPOSITE;

    static Kind fromDefinition(final Definition d) {
      if (isType(d)) {
        return Kind.TYPE;
      } else if (isPrimitive(d)) {
        return Kind.PRIMITIVE;
      } else if (isComposite(d)) {
        return Kind.COMPOSITE;
      } else {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR,
            new NodeErrorLocator(d), "Unknown definition type: "
                + d.astGetType());
      }
    }
  }

  protected Definition mergeDef(final Definition def, Definition superDef,
      final Node locator) throws ADLException, CompilerError {
    try {
      superDef = (Definition) nodeMergerItf
          .merge(def, superDef, nameAttributes);
    } catch (final MergeException e) {
      if (e instanceof InvalidMergeException) {
        errorManagerItf.logError(((InvalidMergeException) e).getError());
      } else {
        throw new CompilerError(ComponentErrors.MERGE_ERROR,
            new NodeErrorLocator(def), e, def.getName());
      }
    }
    return superDef;
  }
}
