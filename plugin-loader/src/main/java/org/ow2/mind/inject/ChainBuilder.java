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

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * A ChainBuilder is used to configure a delegation chain.
 * 
 * @param <T> The type of the delegation chain.
 * @see AbstractMindModule
 */
public interface ChainBuilder<T> extends ScopedBindingBuilder {

  /**
   * Append the class of an element in the delegation chain.
   * 
   * @param elemClass the class of an element in the delegation chain.
   * @return this object.
   * @see AbstractMindModule
   */
  ChainBuilder<T> followedBy(Class<? extends T> elemClass);

  /**
   * Append the provider of an element in the delegation chain.
   * 
   * @param elemProvider the provider of an element in the delegation chain.
   * @return this object.
   * @see AbstractMindModule
   */
  ChainBuilder<T> followedBy(Provider<? extends T> elemProvider);

  /**
   * Append the element in the delegation chain.
   * 
   * @param elem an element in the delegation chain.
   * @return this object.
   * @see AbstractMindModule
   */
  ChainBuilder<T> followedBy(T elem);

  /**
   * Append the key of an element in the delegation chain.
   * 
   * @param elemKey the key of an element in the delegation chain.
   * @return this object.
   * @see AbstractMindModule
   */
  ChainBuilder<T> followedBy(Key<T> elemKey);

  /**
   * Append the class of an element as the last element in the delegation chain.
   * 
   * @param elemClass the class of an element in the delegation chain.
   * @return a {@link ScopedBindingBuilder} that can be used to specify a scope.
   * @see AbstractMindModule
   */
  ScopedBindingBuilder endingWith(Class<? extends T> elemClass);

  /**
   * Append the provider of an element as the last element in the delegation
   * chain.
   * 
   * @param elemProvider the provider of an element in the delegation chain.
   * @return a {@link ScopedBindingBuilder} that can be used to specify a scope.
   * @see AbstractMindModule
   */
  ScopedBindingBuilder endingWith(Provider<T> elemProvider);

  /**
   * Append the element as the last element in the delegation chain.
   * 
   * @param elem an element in the delegation chain.
   * @return a {@link ScopedBindingBuilder} that can be used to specify a scope.
   * @see AbstractMindModule
   */
  ScopedBindingBuilder endingWith(T elem);

  /**
   * Append the key of an element as the last element in the delegation chain.
   * 
   * @param elemKey the key of an element in the delegation chain.
   * @return a {@link ScopedBindingBuilder} that can be used to specify a scope.
   * @see AbstractMindModule
   */
  ScopedBindingBuilder endingWith(Key<T> elemKey);
}