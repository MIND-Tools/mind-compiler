/**
 * Copyright (C) 2009 STMicroelectronics
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
 * Contributors:
 */

package org.ow2.mind.adl.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.NodeContainerDecoration;

public class SubDefinitionsDecoration implements NodeContainerDecoration {

  private final ArrayList<Definition> definitions = new ArrayList<Definition>();

  public void add(final Definition reference) {
    definitions.add(reference);
  }

  public Collection<Node> getNodes() {
    return new ArrayList<Node>(definitions);
  }

  public void replaceNodes(final IdentityHashMap<Node, Node> replacements) {
    for (int i = 0; i < definitions.size(); i++) {
      final Node ref = definitions.get(i);
      if (replacements.containsKey(ref))
        definitions.set(i, (Definition) replacements.get(ref));
    }
  }

  public Collection<Definition> getSubDefinitions() {
    return definitions;
  }

}
