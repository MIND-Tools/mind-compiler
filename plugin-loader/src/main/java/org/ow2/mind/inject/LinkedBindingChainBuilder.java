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

package org.ow2.mind.inject;

import com.google.inject.Provider;
import com.google.inject.binder.LinkedBindingBuilder;

/**
 * Extends {@link LinkedBindingBuilder} to add Delegation Chain builder
 * capacities.
 * 
 * @param <T> the type of the builder
 * @see AbstractMindModule
 */
public interface LinkedBindingChainBuilder<T> extends LinkedBindingBuilder<T> {

  /**
   * Starts a delegation chain with the given class.
   * 
   * @param clazz the class of an element in the delegation chain.
   * @return a {@link ChainBuilder} that can be used to add other element in the
   *         chain.
   */
  ChainBuilder<T> toChainStartingWith(Class<? extends T> clazz);

  /**
   * Starts a delegation chain with the given provider.
   * 
   * @param provider the provider of an element in the delegation chain.
   * @return a {@link ChainBuilder} that can be used to add other element in the
   *         chain.
   */
  ChainBuilder<T> toChainStartingWith(Provider<? extends T> provider);

  /**
   * Starts a delegation chain with the given element.
   * 
   * @param instance an element in the delegation chain.
   * @return a {@link ChainBuilder} that can be used to add other element in the
   *         chain.
   */
  ChainBuilder<T> toChainStartingWith(T instance);
}