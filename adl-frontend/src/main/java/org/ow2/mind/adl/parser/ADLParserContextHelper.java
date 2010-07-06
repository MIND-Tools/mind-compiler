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

package org.ow2.mind.adl.parser;

import java.util.Map;

import org.objectweb.fractal.adl.Definition;

/**
 * This helper class allows to register ADL to be returned by the ADLParser.
 * This is useful for Annotation processors that generate new ADL definition.
 * They can generate the ADL source code, or the ADL AST and register it in the
 * context. This way when the ADL will be loader (because it is referenced by a
 * sub-component, for instance), it will be parsed or simply returned by the
 * ADLParser.
 */
public final class ADLParserContextHelper {

  private ADLParserContextHelper() {
  }

  public static final String REGISTERED_ADL_SUFFIX_CONTEXT_KEY = "registered-adl";

  /**
   * Registers the sources of a generated ADL in the context map, so that it can
   * be parsed by the ADLParser and loaded by the complete loader chain.
   * 
   * @param adlName the name of the ADL
   * @param adlSource the source code of the ADL
   * @param context the context map in which the ADL will be registered.
   */
  public static void registerADL(final String adlName, final String adlSource,
      final Map<Object, Object> context) {
    final String contextKey = getContextKey(adlName);
    final Object o = context.get(contextKey);
    if (o != null) {
      throw new IllegalArgumentException("Invalid adlName '" + adlName
          + "'. An ADL with the same name already exist in the context map");
    }
    context.put(contextKey, adlSource);
  }

  /**
   * Registers the AST of a generated ADL in the context map, so that it can be
   * returned by the ADLParser and loaded by the complete loader chain.
   * 
   * @param definition the AST of the ADL to register.
   * @param context the context map in which the ADL will be registered.
   */
  public static void registerADL(final Definition definition,
      final Map<Object, Object> context) {
    if (definition.getName() == null) {
      throw new IllegalArgumentException("definition name must be set");
    }
    final String contextKey = getContextKey(definition.getName());
    final Object o = context.get(contextKey);
    if (o != null) {
      throw new IllegalArgumentException("Invalid adlName '"
          + definition.getName()
          + "'. An ADL with the same name already exist in the context map");
    }
    context.put(contextKey, definition);
  }

  /**
   * Returns and removes a registered ADL from the given context map.
   * 
   * @param adlName the name of the ADL to return.
   * @param context the context map
   * @return Returns a String, if the ADL has been registered using the
   *         {@link #registerADL(String, String, Map)} method. Otherwise,
   *         returns an AST if the ADL has been registered using the
   *         {@link #registerADL(Definition, Map)} method.
   */
  public static Object getRegisteredADL(final String adlName,
      final Map<Object, Object> context) {
    return context.remove(getContextKey(adlName));
  }

  /**
   * Returns <code>true</code> if a ADL for the given name is registered in the
   * given context map.
   * 
   * @param adlName the name of the ADL
   * @param context the context map
   * @return <code>true</code> if a ADL for the given name is registered in the
   *         given context map.
   */
  public static boolean isRegisteredADL(final String adlName,
      final Map<Object, Object> context) {
    return context.containsKey(getContextKey(adlName));
  }

  private static String getContextKey(final String adlName) {
    return adlName + "-" + REGISTERED_ADL_SUFFIX_CONTEXT_KEY;
  }
}
