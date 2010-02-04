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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.adl.ast.ASTHelper.isComposite;
import static org.ow2.mind.adl.ast.ASTHelper.isPrimitive;
import static org.ow2.mind.adl.ast.ASTHelper.isType;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.components.ComponentLoaderAttributes;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.error.Error;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.AbstractDefinition;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.ast.MindDefinition;

/**
 * This delegating loader merges a definitions with the definitions it extends.
 */
public class ExtendsLoader extends AbstractLoader
    implements
      ComponentLoaderAttributes {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The interface used to resolve referenced definitions. */
  public DefinitionReferenceResolver  definitionReferenceResolverItf;

  /** The node merger component used to merge AST. */
  public NodeMerger                   nodeMergerItf;

  // ---------------------------------------------------------------------------
  // Attributes
  // ---------------------------------------------------------------------------

  /**
   * The names of the "name" attribute for each AST node type. This map
   * associates the names of the "name" attribute, used to detect overridden
   * elements, to each AST node type that has such an attribute.
   */
  protected final Map<String, String> nameAttributes;

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  /**
   * Default constructor.
   */
  public ExtendsLoader() {
    nameAttributes = new HashMap<String, String>();
    nameAttributes.put("component", "name");
    nameAttributes.put("interface", "name");
    nameAttributes.put("attribute", "name");
    nameAttributes.put("annotation", "type");
    nameAttributes.put("argument", "name");
    nameAttributes.put("template", "name");
  }

  // ---------------------------------------------------------------------------
  // Implementation of the ComponentLoaderAttributes interface
  // ---------------------------------------------------------------------------

  public String getNameAttributes() {
    final StringBuffer b = new StringBuffer();
    for (final Map.Entry<String, String> e : nameAttributes.entrySet()) {
      b.append(e.getKey());
      b.append(' ');
      b.append(e.getValue());
      b.append(' ');
    }
    return b.toString();
  }

  public void setNameAttributes(String nameAttributes) {
    this.nameAttributes.clear();
    String key = null;
    int p = nameAttributes.indexOf(' ');
    while (p != -1) {
      final String s = nameAttributes.substring(0, p);
      if (key == null) {
        key = s;
      } else {
        this.nameAttributes.put(key, s);
        key = null;
      }
      nameAttributes = nameAttributes.substring(p + 1);
      p = nameAttributes.indexOf(' ');
    }
    if (key != null) {
      this.nameAttributes.put(key, nameAttributes);
    }
  }

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
    d.setExtends(null);

    if (extendedDefs.length > 0) {
      final Kind kind = Kind.fromDefinition(d);

      // first resolve extended list
      Definition superDef = null;
      for (final DefinitionReference extend : extendedDefs) {
        Definition extendedDefinition;
        try {
          extendedDefinition = definitionReferenceResolverItf.resolve(extend,
              d, context);
        } catch (final ADLException e) {
          ChainedErrorLocator.chainLocator(e, extend);
          throw e;
        }

        final Kind extendedKind = Kind.fromDefinition(extendedDefinition);
        switch (kind) {
          case TYPE :
            if (extendedKind == Kind.PRIMITIVE)
              throw new ADLException(
                  ADLErrors.INVALID_EXTENDS_TYPE_EXTENDS_PRIMITIVE, extend
                      .getName());
            else if (extendedKind == Kind.COMPOSITE)
              throw new ADLException(
                  ADLErrors.INVALID_EXTENDS_TYPE_EXTENDS_COMPOSITE, extend
                      .getName());
            break;
          case PRIMITIVE :
            if (extendedKind == Kind.COMPOSITE)
              throw new ADLException(
                  ADLErrors.INVALID_EXTENDS_PRIMITIVE_EXTENDS_COMPOSITE, extend
                      .getName());
            break;
          case COMPOSITE :
            if (extendedKind == Kind.PRIMITIVE)
              throw new ADLException(
                  ADLErrors.INVALID_EXTENDS_COMPOSITE_EXTENDS_PRIMITIVE, extend
                      .getName());
            break;
        }

        if (superDef == null)
          superDef = extendedDefinition;
        else
          superDef = mergeDef(extendedDefinition, superDef, extend);
      }

      // second merge d with superDef
      d = (MindDefinition) mergeDef(d, superDef,
          extendedDefs[extendedDefs.length - 1]);

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
        final Error error = ((InvalidMergeException) e).getError();
        ChainedErrorLocator.chainLocator(error, new NodeErrorLocator(locator));
        throw new ADLException(error);
      } else {
        throw new CompilerError(ComponentErrors.MERGE_ERROR,
            new NodeErrorLocator(def), e, def.getName());
      }
    }
    return superDef;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = (NodeMerger) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), DefinitionReferenceResolver.ITF_NAME,
        NodeMerger.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      return nodeMergerItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
