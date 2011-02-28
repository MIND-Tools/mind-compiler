/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.target.TargetDescriptorLoader.AbstractDelegatingTargetDescriptorLoader;
import org.ow2.mind.target.ast.Flag;
import org.ow2.mind.target.ast.Target;

import com.google.inject.Inject;

public class FlagSorterLoader extends AbstractDelegatingTargetDescriptorLoader {

  @Inject
  protected ErrorManager errorManager;

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Target target = clientLoader.load(name, context);

    for (final String nodeType : target.astGetNodeTypes()) {
      final Node[] subNodes = target.astGetNodes(nodeType);
      if (subNodes != null && subNodes.length > 0
          && subNodes[0] instanceof Flag) {
        sortFlags(target, subNodes);
      }
    }

    return target;
  }

  protected void sortFlags(final Target target, final Node[] flags)
      throws ADLException {
    final TreeMap<Integer, List<Node>> sortedFlags = new TreeMap<Integer, List<Node>>();
    // remove flags and use a tree-map to sort them
    for (final Node flag : flags) {
      target.astRemoveNode(flag);
      final int index = getIndex((Flag) flag);
      List<Node> l = sortedFlags.get(index);
      if (l == null) {
        l = new ArrayList<Node>();
        sortedFlags.put(index, l);
      }
      l.add(flag);
    }
    // re-add flags in the ascending order of the tree-map
    for (final List<Node> l : sortedFlags.values()) {
      for (final Node flag : l) {
        target.astAddNode(flag);
      }
    }
  }

  protected int getIndex(final Flag flag) throws ADLException {
    final String index = flag.getIndex();
    if (index == null) return Integer.MAX_VALUE;
    try {
      return Integer.parseInt(index);
    } catch (final NumberFormatException e) {
      errorManager.logError(TargetDescErrors.INVALID_INDEX, flag, index);
      return Integer.MAX_VALUE;
    }
  }

}
