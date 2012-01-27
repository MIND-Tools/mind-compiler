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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.inject.InjectDelegate;

public interface BindingChecker {

  boolean checkFromCompositeToSubcomponentBinding(Interface compositeInterface,
      Interface subComponentInterface, Binding binding, Node locator)
      throws ADLException;

  boolean checkFromSubcomponentToCompositeBinding(
      Interface subComponentInterface, Interface compositeInterface,
      Binding binding, Node locator) throws ADLException;

  boolean checkBinding(Interface fromInterface, Interface toInterface,
      Binding binding, Node locator) throws ADLException;

  boolean checkCompatibility(Interface from, Interface to, Node locator)
      throws ADLException;

  /**
   * An abstract delegating {@link BindingChecker} component.
   */
  public abstract class AbstractDelegatingBindingChecker
      implements
        BindingChecker {

    /**
     * The client {@link BindingChecker} used by this component.
     */
    @InjectDelegate
    protected BindingChecker clientBindingCheckerItf;
  }
}
