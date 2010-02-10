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

package org.ow2.mind.adl.imports.ast;

import static org.ow2.mind.CommonASTHelper.newNode;
import static org.ow2.mind.CommonASTHelper.turnsTo;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;

/**
 * Helper methods for import AST nodes.
 */
public final class ImportASTHelper {

  private ImportASTHelper() {
  }

  // ---------------------------------------------------------------------------
  // Import helper methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if the given import is an "on demand import"
   * (i.e. {@link Import#getSimpleName() simpleName} is <code>null</code> or
   * equals to {@value Import#ON_DEMAND_IMPORT}.
   * 
   * @param imp an import node.
   * @return <code>true</code> if the given import is an "on demand import"
   *         (i.e. {@link Import#getSimpleName() simpleName} is
   *         <code>null</code> or equals to {@value Import#ON_DEMAND_IMPORT}.
   */
  public static boolean isOnDemandImport(final Import imp) {
    return imp.getSimpleName() == null
        || Import.ON_DEMAND_IMPORT.equals(imp.getSimpleName());
  }

  /**
   * The name of the decoration used to mark used Import nodes. This decoration
   * can be used to check if a import node is actually used by a definition.
   * 
   * @see #setUsedImport(Import)
   * @see #isUsedImport(Import)
   */
  public static final String USED_IMPORT_DECORATION_NAME = "used-import";

  /**
   * Mark the given import as used.
   * 
   * @param imp an import node.
   * @see #USED_IMPORT_DECORATION_NAME
   * @see #isUsedImport(Import)
   */
  public static void setUsedImport(final Import imp) {
    imp.astSetDecoration(USED_IMPORT_DECORATION_NAME, Boolean.TRUE);
  }

  /**
   * Returns <code>true</code> if the given import is marked as used.
   * 
   * @param imp an import node.
   * @return <code>true</code> if the given import is marked as used.
   * @see #USED_IMPORT_DECORATION_NAME
   * @see #setUsedImport(Import)
   */
  public static boolean isUsedImport(final Import imp) {
    final Boolean b = (Boolean) imp
        .astGetDecoration(USED_IMPORT_DECORATION_NAME);
    return (b != null) && b;
  }

  /**
   * Create a new {@link Import} node using the given {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param packageName the value of the {@link Import#getPackageName()
   *          packageName}.
   * @param simpleName the value of the {@link Import#getSimpleName()
   *          simpleName}.
   * @return a new {@link Import} node.
   */
  public static Import newImport(final NodeFactory nodeFactory,
      final String packageName, final String simpleName) {
    final Import imp = newNode(nodeFactory, "import", Import.class);
    imp.setPackageName(packageName);
    imp.setSimpleName(simpleName);
    return imp;
  }

  /**
   * Create a new on demand {@link Import} node using the given
   * {@link NodeFactory}
   * 
   * @param nodeFactory the {@link NodeFactory} to use to create the node.
   * @param packageName the value of the {@link Import#getPackageName()
   *          packageName}.
   * @return a new {@link Import} node.
   */
  public static Import newOnDemandImport(final NodeFactory nodeFactory,
      final String packageName) {
    final Import imp = newNode(nodeFactory, "import", Import.class);
    imp.setPackageName(packageName);
    imp.setSimpleName(Import.ON_DEMAND_IMPORT);
    return imp;
  }

  /**
   * Transforms the given node to an {@link ImportContainer}. If the node
   * already implements the {@link ImportContainer} interface, this method
   * simply cast it. Otherwise this method use the given node factory and node
   * merger to create a copy of the given node that implements the
   * {@link ImportContainer} interface.
   * 
   * @param node the node to transform.
   * @param nodeFactory the {@link NodeFactory} to use.
   * @param nodeMerger the {@link NodeMerger} to use.
   * @return either the given node casted as {@link ImportContainer}, or a copy
   *         of the given node that implements {@link ImportContainer}.
   */
  public static ImportContainer turnsToImportContainer(final Node node,
      final NodeFactory nodeFactory, final NodeMerger nodeMerger) {
    return turnsTo(node, ImportContainer.class, nodeFactory, nodeMerger);
  }
}
