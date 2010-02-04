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

package org.ow2.mind.adl.binding;

import static java.lang.Math.min;
import static org.ow2.mind.adl.ast.ASTHelper.getComponent;
import static org.ow2.mind.adl.ast.ASTHelper.getInterface;
import static org.ow2.mind.adl.ast.ASTHelper.getNumberOfElement;
import static org.ow2.mind.adl.ast.ASTHelper.getResolvedComponentDefinition;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.bindings.BindingErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.AbstractNormalizerLoader;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;

public class BindingNormalizerLoader extends AbstractNormalizerLoader<Binding> {

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  @Override
  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    expandMultiBindings(d);
    normalize(d);

    return d;
  }

  protected void expandMultiBindings(final Definition d) throws ADLException {
    final Binding[] bindings = getSubNodes(d);
    if (bindings == null) return;

    for (final Binding binding : bindings) {
      if (binding.getFromInterfaceNumber() != null
          && binding.getToInterfaceNumber() != null) continue;
      final Interface fromItf = getBindingInterface(d, binding
          .getFromComponent(), binding.getFromInterface());
      if (fromItf == null) continue;
      final Interface toItf = getBindingInterface(d, binding.getToComponent(),
          binding.getToInterface());
      if (toItf == null) continue;

      final boolean multiFrom = TypeInterfaceUtil.isCollection(fromItf)
          && binding.getFromInterfaceNumber() == null;
      final boolean multiTo = TypeInterfaceUtil.isCollection(toItf)
          && binding.getToInterfaceNumber() == null;

      if (multiFrom && multiTo) {
        final int elem = min(getNumberOfElement(fromItf),
            getNumberOfElement(toItf));
        for (int i = 0; i < elem; i++) {
          final Binding b = NodeUtil.cloneNode(binding);
          b.setFromInterfaceNumber(Integer.toString(i));
          b.setToInterfaceNumber(Integer.toString(i));
          ((BindingContainer) d).addBinding(b);
        }

        ((BindingContainer) d).removeBinding(binding);
      }
    }
  }

  protected Interface getBindingInterface(final Definition d,
      final String compName, final String itfName) throws ADLException {
    final Definition compDef;
    if (Binding.THIS_COMPONENT.equals(compName)) {
      compDef = d;
    } else {
      final Component comp = getComponent(d, compName);
      if (comp == null) return null;
      compDef = getResolvedComponentDefinition(comp, null, null);
      assert compDef != null;
    }
    return getInterface(compDef, itfName);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the abstract methods of AbstractNormalizerLoader
  // ---------------------------------------------------------------------------

  @Override
  protected Binding[] getSubNodes(final Node node) {
    if (node instanceof BindingContainer)
      return ((BindingContainer) node).getBindings();
    return null;
  }

  @Override
  protected Object getId(final Binding node) {
    return node.getFromComponent() + "." + node.getFromInterface() + "."
        + node.getFromInterfaceNumber();
  }

  @Override
  protected void handleNameClash(final Binding previousDeclaration,
      final Binding node) throws ADLException {
    final String itfName;

    if (node.getFromInterfaceNumber() == null)
      itfName = node.getFromComponent() + "." + node.getFromInterface();
    else
      itfName = node.getFromComponent() + "." + node.getFromInterface() + "["
          + node.getFromInterfaceNumber() + "]";

    throw new ADLException(BindingErrors.DUPLICATED_BINDING, node, itfName,
        new NodeErrorLocator(previousDeclaration));
  }
}
