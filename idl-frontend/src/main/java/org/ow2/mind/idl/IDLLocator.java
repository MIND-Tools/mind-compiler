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

package org.ow2.mind.idl;

import java.net.URL;
import java.util.Map;

import org.ow2.mind.GenericResourceLocator;
import org.ow2.mind.InputResource;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.SharedTypeDefinition;

/**
 * IDL location interface.
 */
public interface IDLLocator extends GenericResourceLocator {

  /** Default name of this interface. */
  String ITF_NAME          = "idl-locator";

  /**
   * The value of {@link InputResource#getKind() input-resource's kind} that is
   * used to reference an {@link InterfaceDefinition}.
   */
  String ITF_RESOURCE_KIND = "itf";

  /**
   * The value of {@link InputResource#getKind() input-resource's kind} that is
   * used to reference an {@link SharedTypeDefinition}.
   */
  String IDT_RESOURCE_KIND = "idt";

  /**
   * Locate the interface definition source file for the given name.
   * 
   * @param name an interface name.
   * @param context additional parameters.
   * @return the {@link URL} of the IDL source file or <code>null</code> if no
   *         source file can be found for the given name.
   */
  URL findSourceItf(String name, Map<Object, Object> context);

  /**
   * Locate the interface definition binary file for the given name.
   * 
   * @param name an interface name.
   * @param context additional parameters.
   * @return the {@link URL} of the IDL binary file or <code>null</code> if no
   *         binary file can be found for the given name.
   */
  URL findBinaryItf(String name, Map<Object, Object> context);

  /**
   * Locate the header source file for the given path
   * 
   * @param path a path to an IDL file.
   * @param context additional parameters.
   * @return the {@link URL} of the header file or <code>null</code> if no
   *         header file can be found for the given path.
   */
  URL findHeader(String path, Map<Object, Object> context);

  /**
   * Returns an {@link InputResource} corresponding to the
   * {@link InterfaceDefinition} of the given name.
   * 
   * @param name an IDL file name.
   * @return an {@link InputResource} corresponding to the
   *         {@link InterfaceDefinition} of the given name.
   */
  InputResource toInterfaceInputResource(String name);

  /**
   * Returns an {@link InputResource} corresponding to the
   * {@link SharedTypeDefinition} of the given name.
   * 
   * @param name an IDL file name.
   * @return an {@link InputResource} corresponding to the
   *         {@link SharedTypeDefinition} of the given name.
   */
  InputResource toSharedTypeInputResource(String name);
}
