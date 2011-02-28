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

package org.ow2.mind.target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.ow2.mind.target.TargetDescriptorLoader.AbstractDelegatingTargetDescriptorLoader;
import org.ow2.mind.target.ast.Extends;
import org.ow2.mind.target.ast.Target;

import com.google.inject.Inject;

public class ExtensionLoader extends AbstractDelegatingTargetDescriptorLoader {

  @Inject
  protected NodeMerger             nodeMergerItf;

  @Inject
  protected TargetDescriptorLoader recursiveLoader;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Target load(final String name, final Map<Object, Object> context)
      throws ADLException {
    Target target = clientLoader.load(name, context);

    final Extends[] extendss = target.getExtendss();
    if (extendss.length == 0) {
      return target;
    }

    final List<Target> extensions = new ArrayList<Target>(extendss.length);

    for (final Extends extend : extendss) {
      extensions.add(recursiveLoader.load(extend.getName(), context));
      target.removeExtends(extend);
    }

    final Map<String, String> nameAttribute = new HashMap<String, String>();
    addNameAttributes(target, nameAttribute);
    for (final Target superTarget : extensions) {
      addNameAttributes(superTarget, nameAttribute);
    }

    for (final Target superTarget : extensions) {
      try {
        target = (Target) nodeMergerItf.merge(target, superTarget,
            nameAttribute);
      } catch (final MergeException e) {
        throw new CompilerError(GenericErrors.INTERNAL_ERROR, e, "Merge error.");
      }
    }

    return target;
  }

  void addNameAttributes(final Target superTarget,
      final Map<String, String> nameAttribute) {
    for (final String nodeType : superTarget.astGetNodeTypes()) {
      final Node[] subNodes = superTarget.astGetNodes(nodeType);
      if (subNodes == null) continue;
      for (final Node subNode : subNodes) {
        if (subNode != null && subNode.astGetAttributes().containsKey("id")) {
          nameAttribute.put(nodeType, "id");
        }
      }
    }
  }
}
