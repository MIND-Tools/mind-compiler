/**
 * Copyright (C) 2010 France Telecom
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
 * Contributors: 
 */

package org.ow2.mind.preproc;

import org.antlr.runtime.Token;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class CPLChecker {
  protected final Definition   definition;
  protected final ErrorManager errorManager;

  public CPLChecker(final ErrorManager errorManager, final Definition definition) {
    this.errorManager = errorManager;
    this.definition = definition;
  }

  public void serverMethDef(final Token itfName, final Token methName,
      final String sourceFile, final int lineNumberShift) throws ADLException {

    // Add this condition so that the testNG will
    // not throw exceptions (stand-alone node)
    if (this.definition != null) {
      boolean foundItf = false;
      boolean isServer = false;
      boolean foundMeth = false;

      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (itf.getName().equals(itfName.getText())) {

          if (TypeInterfaceUtil.isServer(itf)) {
            isServer = true;

            InterfaceDefinition itfDef;
            itfDef = InterfaceDefinitionDecorationHelper
                .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
            for (final Method meth : itfDef.getMethods()) {
              if (meth.getName().equals(methName.getText())) {
                foundMeth = true;
                break;
              }
            }

          }
          foundItf = true;
          break;
        }
      }

      if (!foundItf) {
        errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
            new BasicErrorLocator(sourceFile, itfName.getLine()
                + lineNumberShift, itfName.getCharPositionInLine()),
            itfName.getText());
      }
      if (isServer) {
        if (!foundMeth) {
          errorManager.logError(MPPErrors.UNKNOWN_METHOD,
              new BasicErrorLocator(sourceFile, methName.getLine()
                  + lineNumberShift, methName.getCharPositionInLine()),
              itfName.getText(), methName.getText());
        }
      } else {
        errorManager.logError(MPPErrors.INVALID_CLIENT_INTERFACE,
            new BasicErrorLocator(sourceFile, itfName.getLine()
                + lineNumberShift, itfName.getCharPositionInLine()),
            itfName.getText(), methName.getText());
      }
    }
  }

  public void itfMethCall(final Token itfName, final Token methName,
      final String sourceFile, final int lineNumberShift) throws ADLException {

    // Add this condition so that the testNG will
    // not throw exceptions (stand-alone node)
    if (this.definition != null) {
      boolean foundItf = false;
      boolean foundMeth = false;

      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (itf.getName().equals(itfName.getText())) {

          final InterfaceDefinition itfDef = InterfaceDefinitionDecorationHelper
              .getResolvedInterfaceDefinition((TypeInterface) itf, null, null);
          for (final Method meth : itfDef.getMethods()) {
            if (meth.getName().equals(methName.getText())) {
              foundMeth = true;
              break;
            }
          }
          foundItf = true;
          break;
        }
      }

      if (!foundItf) {
        errorManager.logError(MPPErrors.UNKNOWN_INTERFACE,
            new BasicErrorLocator(sourceFile, itfName.getLine()
                + lineNumberShift, itfName.getCharPositionInLine()),
            itfName.getText());
      }
      if (!foundMeth) {
        errorManager.logError(MPPErrors.UNKNOWN_METHOD,
            new BasicErrorLocator(sourceFile, methName.getLine()
                + lineNumberShift, methName.getCharPositionInLine()),
            itfName.getText(), methName.getText());
      }
    }

  }

  public void attAccess(final Token attributeName, final String sourceFile,
      final int lineNumberShift) throws ADLException {
    if (this.definition != null) { // add this condition so that the testNG will
      // not throw exceptions
      boolean foundAtt = false;

      for (final Attribute att : ((AttributeContainer) definition)
          .getAttributes()) {
        if (att.getName().equals(attributeName.getText())) {
          foundAtt = true;
          break;
        }
      }

      if (!foundAtt) {
        errorManager.logError(MPPErrors.UNKNOWN_ATTRIBUTE,
            new BasicErrorLocator(sourceFile, attributeName.getLine()
                + lineNumberShift, attributeName.getCharPositionInLine()),
            attributeName.getText());
      }
    }
  }
}
