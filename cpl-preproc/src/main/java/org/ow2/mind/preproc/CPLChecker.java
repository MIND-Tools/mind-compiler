/**
 * Copyright (C) 2010 France Telecom
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
 * Authors: Matthieu ANNE
 * Contributors: Stephane SEYVOZ
 */

package org.ow2.mind.preproc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.antlr.runtime.Token;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Data;
import org.ow2.mind.adl.ast.DataField;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.membrane.ast.Controller;
import org.ow2.mind.adl.membrane.ast.ControllerContainer;
import org.ow2.mind.adl.membrane.ast.ControllerInterface;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.io.OutputFileLocator;

public class CPLChecker {
  protected final Definition            definition;
  protected final ErrorManager          errorManager;
  protected final ImplementationLocator implLocatorItf;
  protected final OutputFileLocator     outputFileLocatorItf;

  protected final Data                  data;
  protected boolean                     prvDeclared        = false;

  protected static Logger               logger             = FractalADLLogManager
                                                               .getLogger("MPP");

  protected Map<String, List<String>>   declaredItfMethMap = new HashMap<String, List<String>>();

  protected final Map<Object, Object>   context;

  public CPLChecker(final ErrorManager errorManager,
      final ImplementationLocator implLocatorItf,
      final OutputFileLocator outputFileLocatorItf,
      final Definition definition, final Map<Object, Object> context) {
    this.errorManager = errorManager;
    this.implLocatorItf = implLocatorItf;
    this.outputFileLocatorItf = outputFileLocatorItf;
    this.definition = definition;
    this.context = context;
    this.data = (definition instanceof ImplementationContainer)
        ? ((ImplementationContainer) definition).getData()
        : null;
  }

  public void prvDecl(final String structContent, final String sourceFile) {
    prvDeclared = true;
  }

  // returns 'true' is the accessed field is a DataField declared in definition.
  public boolean prvAccess(final Token fieldName, final String sourceFile)
      throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return false;
    }
    if (definition instanceof ImplementationContainer) {
      for (final DataField dataField : ((ImplementationContainer) definition)
          .getDataFields()) {
        if (fieldName.getText().equals(dataField.getName())) {
          return true;
        }
      }
      // field name not found in DataFields
      if (data != null) {
        // component also declare a PRIVATE structure. assumes that field is in
        // this structure
        return false;
      }
      // component does not have PRIVATE structure, the field name is invalid.
      errorManager.logError(MPPErrors.UNKNOWN_DATAFIELD,
          locator(fieldName, sourceFile), fieldName.getText());
    }
    return false;
  }

  public void serverMethDef(final Token itfName, final String itfIdx,
      final Token methName, final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    if (data != null && !prvDeclared) {
      errorManager.logError(MPPErrors.MISSING_PRIVATE_DECLARATION, (data
          .getPath() == null)
          ? new NodeErrorLocator(data)
          : new BasicErrorLocator(data.getPath(), -1, -1), data.getPath());
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }
    if (!TypeInterfaceUtil.isServer(itf)) {
      errorManager.logError(MPPErrors.INVALID_CLIENT_INTERFACE,
          locator(itfName, sourceFile), itfName.getText(), methName.getText());
      return;
    }

    // assume that itfDef is already loaded
    final InterfaceDefinition itfDef = InterfaceDefinitionDecorationHelper
        .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    if (IDLASTHelper.getMethod(itfDef, methName.getText()) == null) {
      errorManager.logError(MPPErrors.UNKNOWN_METHOD,
          locator(methName, sourceFile), itfName.getText(), methName.getText());
    }

    // to avoid rewriting the grammar
    StringBuilder idxSB = null;
    Integer idxInt = null;
    if (itfIdx != null) idxSB = new StringBuilder().append(itfIdx);

    checkIdx(itf, itfName, idxSB, sourceFile);

    if (itfIdx != null) {
      try {
        idxInt = Integer.parseInt(itfIdx);
      } catch (final NumberFormatException e) {
        // ignore, idx is not a number literal
      }
    }

    if (!TypeInterfaceUtil.isCollection(itf))
      ImplementedMethodsHelper.addImplementedMethod(itf, methName.getText());
    else
      ImplementedMethodsHelper.addCollectionImplementedMethod(itf, idxInt,
          methName.getText());

  }

  public void itfMethCall(final Token itfName, final Token methName,
      final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }

    // assume that itfDef is already loaded
    final InterfaceDefinition itfDef = InterfaceDefinitionDecorationHelper
        .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    if (IDLASTHelper.getMethod(itfDef, methName.getText()) == null) {
      errorManager.logError(MPPErrors.UNKNOWN_METHOD,
          locator(methName, sourceFile), itfName.getText(), methName.getText());
    }
  }

  public void attAccess(final Token attributeName, final String sourceFile)
      throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    if (ASTHelper.getAttribute(definition, attributeName.getText()) == null) {
      errorManager.logError(MPPErrors.UNKNOWN_ATTRIBUTE,
          locator(attributeName, sourceFile), attributeName.getText());
    }
  }

  public void collItfMethCall(final Token itfName, final Token methName,
      final StringBuilder idx, final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }

    // assume that itfDef is already loaded
    final InterfaceDefinition itfDef = InterfaceDefinitionDecorationHelper
        .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
    if (IDLASTHelper.getMethod(itfDef, methName.getText()) == null) {
      errorManager.logError(MPPErrors.UNKNOWN_METHOD,
          locator(methName, sourceFile), itfName.getText(), methName.getText());
    }

    checkIdx(itf, itfName, idx, sourceFile);
  }

  public void getMyItf(final Token itfName, final StringBuilder idx,
      final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }

    checkIdx(itf, itfName, idx, sourceFile);
  }

  public void bindMyItf(final Token itfName, final StringBuilder idx,
      final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }

    checkIdx(itf, itfName, idx, sourceFile);
  }

  public void isBound(final Token itfName, final StringBuilder idx,
      final String sourceFile) throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }

    checkIdx(itf, itfName, idx, sourceFile);
  }

  public void getCollectionSize(final Token itfName, final String sourceFile)
      throws ADLException {
    if (definition == null) {
      // Add this condition so that the testNG will not throw exceptions
      // (stand-alone node)
      return;
    }

    final Interface itf = ASTHelper.getInterface(definition, itfName.getText());
    if (itf == null) {
      errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
          locator(itfName, sourceFile), itfName.getText());
      return;
    }
  }

  protected void checkIdx(final Interface itf, final Token itfToken,
      final StringBuilder idx, final String sourceFile) throws ADLException {
    if (idx == null) {
      if (TypeInterfaceUtil.isCollection(itf)) {
        errorManager.logError(MPPErrors.INVALID_INTERFACE_MISSING_INDEX,
            locator(itfToken, sourceFile), itf.getName());
      }
    } else {
      if (!TypeInterfaceUtil.isCollection(itf)) {
        errorManager.logError(MPPErrors.INVALID_INTERFACE_NOT_A_COLLECTION,
            locator(itfToken, sourceFile), itf.getName());
      }

      if (idx.length() <= 2) {
        return;
      }
      final String index = idx.substring(1, idx.length() - 1).trim();
      try {
        final int i = Integer.parseInt(index);
        if (i < 0 || i >= ASTHelper.getNumberOfElement(itf)) {
          errorManager.logError(MPPErrors.INVALID_INDEX,
              locator(itfToken, sourceFile), itf.getName(), index);
        }
      } catch (final NumberFormatException e) {
        // ignore, idx is not a number literal
      }
    }
  }

  protected BasicErrorLocator locator(final Token token, final String sourceFile) {
    return new BasicErrorLocator(sourceFile, token.getLine(),
        token.getCharPositionInLine());
  }

  public void postParseChecks(final String sourceFile) throws ADLException {

    // this means we aren't in standard compilation
    // probably CPL-Preproc direct parser tests
    if (definition == null) return;

    // handling membrane of composites
    if (!ASTHelper.isPrimitive(definition)) return;

    // mark file as visited
    final Source source = ImplementedMethodsHelper.getDefinitionSourceFromPath(
        implLocatorItf, outputFileLocatorItf, definition, sourceFile, context);

    // should never happen, except maybe for generated code issued from
    // architecture transformations, and added in definition ?
    if (source == null) return;

    ImplementedMethodsHelper.setSourceVisited(source);

    // was it the last one ? if yes, we run a full methods check
    if (definition instanceof ImplementationContainer
        && definition instanceof InterfaceContainer)
      if (ImplementedMethodsHelper
          .haveAllSourcesBeenVisited((ImplementationContainer) definition)) {

        final Map<Interface, List<String>> allUnimplementedMethods = new HashMap<Interface, List<String>>();
        final Map<Interface, Map<Integer, List<String>>> allCollectionUnimplementedMethods = new HashMap<Interface, Map<Integer, List<String>>>();

        logger
            .fine("All of "
                + definition.getName()
                + " sources visited - Now checking if all provided methods were implemented...");

        for (final Interface currItf : ((InterfaceContainer) definition)
            .getInterfaces())
          if ((currItf instanceof TypeInterface)
              && ((TypeInterface) currItf).getRole().equals(
                  TypeInterface.SERVER_ROLE)
              && !isControllerInterface(currItf.getName())) {

            if (!TypeInterfaceUtil.isCollection(currItf)) {
              final List<String> unimplementedMethodsList = ImplementedMethodsHelper
                  .getInterfaceUnimplementedMethods(currItf);
              if (!unimplementedMethodsList.isEmpty())
                allUnimplementedMethods.put(currItf, unimplementedMethodsList);
            } else {
              final Map<Integer, List<String>> unimplementedMethodsMap = ImplementedMethodsHelper
                  .getCollectionInterfaceUnimplementedMethods(currItf);
              if (!unimplementedMethodsMap.isEmpty()) {
                allCollectionUnimplementedMethods.put(currItf,
                    unimplementedMethodsMap);
              }
            }

          }

        if (allUnimplementedMethods.isEmpty()
            && allCollectionUnimplementedMethods.isEmpty())
          logger.fine("All methods were correctly implemented.");
        else if (!allUnimplementedMethods.isEmpty()) {
          final Set<Interface> interfaces = allUnimplementedMethods.keySet();
          final Interface itf0 = (Interface) interfaces.toArray()[0];

          // Show missing methods from the first concerned interface
          errorManager.logError(MPPErrors.MISSING_METHOD_DECLARATION,
              definition.getName(), itf0.getName(),
              allUnimplementedMethods.get(itf0));
        } else if (!allCollectionUnimplementedMethods.isEmpty()) {
          final Set<Interface> interfaces = allCollectionUnimplementedMethods
              .keySet();
          final Interface itf0 = (Interface) interfaces.toArray()[0];

          final Map<Integer, List<String>> unimplMethsByIdxMap = allCollectionUnimplementedMethods
              .get(itf0);
          final Set<Integer> indexes = unimplMethsByIdxMap.keySet();
          final Integer idx0 = (Integer) indexes.toArray()[0];

          // Show missing methods from the first concerned interface
          errorManager.logError(MPPErrors.MISSING_COLL_METHOD_DECLARATION,
              definition.getName(), itf0.getName(), idx0.toString(),
              unimplMethsByIdxMap.get(idx0));
        }
      }
  }

  private boolean isControllerInterface(final String currItf) {
    /*
     * Check if we host controllers (METH-s will be generated so they can't be
     * found in Source-s) Inspired from
     * AbstractControllerADLLoaderAnnotationProcessor
     */
    if (definition instanceof ControllerContainer) {
      for (final Controller ctrl : ((ControllerContainer) definition)
          .getControllers()) {
        for (final ControllerInterface ctrlItf : ctrl.getControllerInterfaces()) {
          if (ctrlItf.getName().equals(currItf)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
