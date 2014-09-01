/**
 * Copyright (C) 2009 STMicroelectronics
 * Copyright (C) 2014 Schneider-Electric
 *
 * This file is part of "Mind Compiler".
 * "Mind Compiler" is a free software tool.
 * This file is licensed under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Ali-Erdem Ozcan, Michel Metzger, Matthieu Leclercq
 * Contributors: Stephane Seyvoz
 *
 * Original source in: mindoc / src / main / java / org / ow2 / mind / doc / adl /
 */

package org.ow2.mind.adl.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;

import org.objectweb.fractal.adl.Node;
import org.ow2.mind.NodeContainerDecoration;
import org.ow2.mind.adl.ast.DefinitionReference;

public class ExtendsDecoration implements NodeContainerDecoration {

  private final ArrayList<DefinitionReference> definitionReferences = new ArrayList<DefinitionReference>();

  public void add(final DefinitionReference reference) {
    definitionReferences.add(reference);
  }

  public Collection<Node> getNodes() {
    return new ArrayList<Node>(definitionReferences);
  }

  public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
    for (int i = 0; i < definitionReferences.size(); i++) {
      final Node ref = definitionReferences.get(i);
      if(replacements.containsKey(ref))
        definitionReferences.set(i, (DefinitionReference)replacements.get(ref));
    }
  }

  public Collection<DefinitionReference> getExtends() {
    return definitionReferences;
  }

}
