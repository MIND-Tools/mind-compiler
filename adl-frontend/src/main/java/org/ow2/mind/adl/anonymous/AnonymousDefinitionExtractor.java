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

package org.ow2.mind.adl.anonymous;

import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.inject.InjectDelegate;

/**
 * Interface used to extract a {@link Definition} from a sub-component
 * containing an anonymous definition.
 */
public interface AnonymousDefinitionExtractor {

  /** Default name of this interface. */
  String ITF_NAME = "anonymous-definition-extractor";

  /**
   * Extracts the anonymous definition that is contained if the given
   * sub-component.
   * 
   * @param component a sub-component node that contains an anonymous definition
   *          (i.e.
   *          <code>((AnonymousDefinitionContainer) component).getAnonymousDefinition() != null</code>
   *          ).
   * @param encapsulatingDefinition the top-level definition into which the
   *          given component is defined. If the given component is itself
   *          defined in an anonymous definition, then this
   *          <code>encapsulatingDefinition</code> is the definition that
   *          contains (directly or transitively) the anonymous definition.
   * @param context additional parameters.
   * @return The "context-freed" anonymous definition.
   */
  Definition extractAnonymousDefinition(Component component,
      Definition encapsulatingDefinition, Map<Object, Object> context);

  /**
   * An abstract delegating {@link AnonymousDefinitionExtractor} component.
   */
  public abstract class AbstractDelegatingAnonymousDefinitionExtractor
      implements
        AnonymousDefinitionExtractor {

    /**
     * The client {@link AnonymousDefinitionExtractor} used by this component.
     */
    @InjectDelegate
    protected AnonymousDefinitionExtractor clientExtractorItf;
  }
}
