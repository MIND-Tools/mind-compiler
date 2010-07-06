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

package org.ow2.mind.idl.parser;

import java.util.Map;

import org.ow2.mind.idl.ast.IDL;

/**
 * This helper class allows to register IDL to be returned by the IDLParser.
 * This is useful for Annotation processors that generate new IDL definition.
 * They can generate the IDL source code, or the IDL AST and register it in the
 * context. This way when the IDL will be loader (because it is referenced by a
 * sub-component, for instance), it will be parsed or simply returned by the
 * IDLParser.
 */
public final class IDLParserContextHelper {

  private IDLParserContextHelper() {
  }

  public static final String REGISTERED_IDL_SUFFIX_CONTEXT_KEY = "registered-idl";

  /**
   * Registers the sources of a generated IDL in the context map, so that it can
   * be parsed by the {@link IDLFileLoader} and loaded by the complete loader
   * chain.
   * 
   * @param idlName the name of the IDL
   * @param idlSource the source code of the IDL
   * @param context the context map in which the IDL will be registered.
   */
  public static void registerIDL(final String idlName, final String idlSource,
      final Map<Object, Object> context) {
    final String contextKey = getContextKey(idlName);
    final Object o = context.get(contextKey);
    if (o != null) {
      throw new IllegalArgumentException("Invalid idlName '" + idlName
          + "'. An IDL with the same name already exist in the context map");
    }
    context.put(contextKey, idlSource);
  }

  /**
   * Registers the AST of a generated IDL in the context map, so that it can be
   * returned by the {@link IDLFileLoader} and loaded by the complete loader
   * chain.
   * 
   * @param idl the AST of the IDL to register.
   * @param context the context map in which the IDL will be registered.
   */
  public static void registerIDL(final IDL idl,
      final Map<Object, Object> context) {
    if (idl.getName() == null) {
      throw new IllegalArgumentException("definition name must be set");
    }
    final String contextKey = getContextKey(idl.getName());
    final Object o = context.get(contextKey);
    if (o != null) {
      throw new IllegalArgumentException("Invalid idlName '" + idl.getName()
          + "'. An IDL with the same name already exist in the context map");
    }
    context.put(contextKey, idl);
  }

  /**
   * Returns and removes a registered IDL from the given context map.
   * 
   * @param idlName the name of the IDL to return.
   * @param context the context map
   * @return Returns a String, if the IDL has been registered using the
   *         {@link #registerIDL(String, String, Map)} method. Otherwise,
   *         returns an AST if the IDL has been registered using the
   *         {@link #registerIDL(IDL, Map)} method.
   */
  public static Object getRegisteredIDL(final String idlName,
      final Map<Object, Object> context) {
    if (context == null) return null;
    return context.remove(getContextKey(idlName));
  }

  /**
   * Returns <code>true</code> if a IDL for the given name is registered in the
   * given context map.
   * 
   * @param idlName the name of the IDL
   * @param context the context map
   * @return <code>true</code> if a IDL for the given name is registered in the
   *         given context map.
   */
  public static boolean isRegisteredIDL(final String idlName,
      final Map<Object, Object> context) {
    return context != null && context.containsKey(getContextKey(idlName));
  }

  private static String getContextKey(final String idlName) {
    return idlName + "-" + REGISTERED_IDL_SUFFIX_CONTEXT_KEY;
  }
}
