/**
 * Copyright (C) 2013 Schneider-Electric
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
 * Authors: Stephane SEYVOZ (Assystem)
 * Contributors: 
 */

package org.ow2.mind.preproc;

import static org.ow2.mind.adl.ast.ASTHelper.getNumberOfElement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class ImplementedMethodsHelper {

  // Inject would not work... FIXME
  // Any suggestion/contribution is welcome. Stephane.
  private static final ImplementationLocator implLocatorItf = new BasicImplementationLocator();

  public static Source getDefinitionSourceFromPath(
      final ImplementationContainer container, final String sourceFileName,
      final Map<Object, Object> context) {

    for (final Source currSource : container.getSources()) {
      try {
        // inlined C code ? TODO: handle this case !!
        if (currSource.getPath() == null) return null;

        // FIXME: Inject instead of hand-made instance
        final URL sourceFileURL = implLocatorItf.findSource(
            currSource.getPath(), context);

        final File sourceFile = new File(sourceFileName);

        if (sourceFileURL.getPath()
            .equals(sourceFile.toURI().toURL().getPath())) return currSource;
      } catch (final MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return null;

  }

  /**
   * 
   */
  public static final String IMPLEMENTED_METHODS = "implemented-methods";

  /**
   * @param itf
   * @return
   */
  public static List<String> getImplementedMethods(final Interface itf) {
    final List<String> methNamesList = (List<String>) itf
        .astGetDecoration(IMPLEMENTED_METHODS);
    if (methNamesList == null) return new ArrayList<String>();

    return methNamesList;
  }

  /**
   * @param itf
   * @return
   */
  public static boolean allInterfaceMethodsAreImplemented(final Interface itf) {

    final List<String> unimplementedMethods = getInterfaceUnimplementedMethods(itf);
    if (unimplementedMethods.isEmpty()) return true;

    return false;
  }

  /**
   * @param itf
   * @return
   */
  public static List<String> getInterfaceUnimplementedMethods(
      final Interface itf) {

    final List<String> implMeths = getImplementedMethods(itf);

    final List<String> result = new ArrayList<String>();

    InterfaceDefinition itfDef;

    // assume that itfDef is already loaded
    try {
      itfDef = InterfaceDefinitionDecorationHelper
          .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    } catch (final ADLException e) {
      return result;
    }

    if (itfDef == null) return result;

    final Method[] methods = itfDef.getMethods();

    for (final Method currMethod : methods) {
      if (!implMeths.contains(currMethod.getName()))
        result.add(currMethod.getName());
    }

    return result;
  }

  /**
   * @param itf
   * @return
   */
  public static Map<Integer, List<String>> getCollectionInterfaceUnimplementedMethods(
      final Interface itf) {

    assert TypeInterfaceUtil.isCollection(itf);

    final int nbElement = getNumberOfElement(itf);
    final Map<Integer, List<String>> implMeths = getCollectionImplementedMethods(itf);
    final Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();

    InterfaceDefinition itfDef;

    // assume that itfDef is already loaded
    try {
      itfDef = InterfaceDefinitionDecorationHelper
          .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    } catch (final ADLException e) {
      return result;
    }

    if (itfDef == null) return result;

    final Method[] methods = itfDef.getMethods();

    for (Integer i = 0; i < nbElement; i++) {
      for (final Method currMethod : methods) {

        List<String> implMethsByIdx = implMeths.get(i);
        if (implMethsByIdx == null) {
          implMethsByIdx = new ArrayList<String>();
          implMeths.put(i, implMethsByIdx);
        }

        if (!implMethsByIdx.contains(currMethod.getName())) {
          List<String> resultMethsByIdx = result.get(i);
          if (resultMethsByIdx == null)
            resultMethsByIdx = new ArrayList<String>();

          resultMethsByIdx.add(currMethod.getName());
          result.put(i, resultMethsByIdx);
        }
      }
    }

    return result;
  }

  public static void addImplementedMethod(final Interface itf,
      final String methName) {
    final List<String> values = getImplementedMethods(itf);
    values.add(methName);
    setImplementedMethods(itf, values);
  }

  public static void addCollectionImplementedMethod(final Interface itf,
      final Integer idxInt, final String methName) {
    final Map<Integer, List<String>> values = getCollectionImplementedMethods(itf);

    assert TypeInterfaceUtil.isCollection(itf);

    List<String> implMethNamesForIdx = values.get(idxInt);
    if (implMethNamesForIdx == null)
      implMethNamesForIdx = new ArrayList<String>();

    implMethNamesForIdx.add(methName);

    values.put(idxInt, implMethNamesForIdx);
    setCollectionImplementedMethods(itf, values);
  }

  private static void setCollectionImplementedMethods(final Interface itf,
      final Map<Integer, List<String>> values) {

    assert TypeInterfaceUtil.isCollection(itf);

    itf.astSetDecoration(IMPLEMENTED_METHODS, values);
  }

  public static Map<Integer, List<String>> getCollectionImplementedMethods(
      final Interface itf) {

    assert TypeInterfaceUtil.isCollection(itf);

    final Map<Integer, List<String>> idxMethNamesMap = (Map<Integer, List<String>>) itf
        .astGetDecoration(IMPLEMENTED_METHODS);
    if (idxMethNamesMap == null) return new HashMap<Integer, List<String>>();

    return idxMethNamesMap;
  }

  /**
   * @param itf
   * @param methNamesList
   */
  public static void setImplementedMethods(final Interface itf,
      final List<String> methNamesList) {

    itf.astSetDecoration(IMPLEMENTED_METHODS, methNamesList);
  }

  /**
   * 
   */
  public static final String SOURCE_VISITED = "source-visited";

  /**
   * @param source
   */
  public static void setSourceVisited(final Source source) {
    source.astSetDecoration(SOURCE_VISITED, Boolean.TRUE);
  }

  /**
   * @param source
   * @return
   */
  public static boolean hasSourceBeenVisited(final Source source) {
    final Boolean value = (Boolean) source.astGetDecoration(SOURCE_VISITED);
    if (value == null) return false;

    return value;
  }

  /**
   * @param container
   * @return
   */
  public static boolean haveAllSourcesBeenVisited(
      final ImplementationContainer container) {

    for (final Source currSource : container.getSources())
      if (!hasSourceBeenVisited(currSource)) return false;

    return true;
  }

}
