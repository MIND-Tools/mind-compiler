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

package org.ow2.mind.adl;

import java.net.URL;
import java.util.Map;

import org.ow2.mind.GenericResourceLocator;
import org.ow2.mind.InputResource;

/**
 * ADL location interface.
 */
public interface ADLLocator extends GenericResourceLocator {

  /** Default name of this interface. */
  String ITF_NAME          = "adl-locator";

  /**
   * The value of {@link InputResource#getKind() input-resource's kind} that is
   * used to reference an ADL {@link InputResource}.
   */
  String ADL_RESOURCE_KIND = "adl";

  /**
   * Locate the ADL source for the given name.
   * 
   * @param name an ADL name.
   * @param context additional parameters.
   * @return the {@link URL} of the ADL source file or <code>null</code> if no
   *         source file can be found for the given name.
   */
  URL findSourceADL(String name, Map<Object, Object> context);

  /**
   * Locate the ADL binary for the given name.
   * 
   * @param name an ADL name.
   * @param context additional parameters.
   * @return the {@link URL} of the ADL binary file or <code>null</code> if no
   *         binary file can be found for the given name.
   */
  URL findBinaryADL(String name, Map<Object, Object> context);

  /**
   * Returns an {@link InputResource} corresponding to the ADL of the given
   * name.
   * 
   * @param name an ADL name
   * @return an {@link InputResource} corresponding to the ADL of the given
   *         name.
   */
  InputResource toInputResource(String name);
}
