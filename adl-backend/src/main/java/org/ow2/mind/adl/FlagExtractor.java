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
 * Contributors: Julien Tous
 */

package org.ow2.mind.adl;

import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.inject.InjectDelegate;

public interface FlagExtractor {

  Collection<String> getCPPFlags(Definition definition,
      final Map<Object, Object> context) throws ADLException;

  Collection<String> getCPPFlags(Source source,
      final Map<Object, Object> context) throws ADLException;

  Collection<String> getCFlags(Definition definition,
      final Map<Object, Object> context) throws ADLException;

  Collection<String> getCFlags(Source source, final Map<Object, Object> context)
      throws ADLException;

  Collection<String> getLDFlags(Definition definition,
      final Map<Object, Object> context) throws ADLException;

  Collection<String> getLDFlags(Source source, final Map<Object, Object> context)
      throws ADLException;

  Collection<String> getASFlags(Definition definition,
      final Map<Object, Object> context) throws ADLException;

  Collection<String> getASFlags(Source source, final Map<Object, Object> context)
      throws ADLException;

  /**
   * An abstract delegating {@link FlagExtractor} component.
   */
  public abstract class AbstractDelegatingFlagExtractor
      implements
        FlagExtractor {

    /**
     * The client {@link FlagExtractor} used by this component.
     */
    @InjectDelegate
    protected FlagExtractor clientExtractorItf;
  }
}
