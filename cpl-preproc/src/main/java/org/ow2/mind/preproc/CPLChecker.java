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

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class CPLChecker {
  protected final Definition definition;

  public CPLChecker(final Definition definition) {
    this.definition = definition;
  }

  public void serverMethDef(final String itfName, final String methName)
      throws Exception {

    // Add this condition so that the testNG will
    // not throw exceptions (stand-alone node)
    if (this.definition != null) {
      boolean foundItf = false;
      boolean isServer = false;
      boolean foundMeth = false;

      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (itf.getName().equals(itfName)) {

          if (((TypeInterface) itf).getRole() == "server") {
            isServer = true;

            final InterfaceDefinition itfDef = ((InterfaceDefinitionDecorationHelper.InterfaceDefinitionDecoration) itf
                .astGetDecoration("resolved-interface-definition"))
                .getInterfaceDefinition();
            for (final Method meth : itfDef.getMethods()) {
              if (meth.getName().equals(methName)) {
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

        throw new Exception(" Unknown interface \"" + itfName + "\" ");
      }
      if (isServer) {
        if (!foundMeth) {
          throw new Exception(" In interface \"" + itfName
              + "\" unknown method \"" + methName + "\" ");
        }
      } else {
        throw new Exception(" Interface \"" + itfName
            + "\" is a client interface, method \"" + methName
            + "\" cannot be defined here");
      }
    }
  }

  public void attAccess(final String attributeName) throws Exception {
    if (this.definition != null) { // add this condition so that the testNG will
      // not throw exceptions
      boolean foundAtt = false;

      for (final Attribute att : ((AttributeContainer) definition)
          .getAttributes()) {
        if (att.getName().equals(attributeName)) {
          foundAtt = true;
          break;
        }
      }

      if (!foundAtt) {
        throw new Exception(" Unknown attribute \"" + attributeName + "\" ");
      }
    }
  }
}