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

package org.ow2.mind.adl.imports;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.NameHelper.getPackageName;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.isOnDemandImport;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.setUsedImport;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.idl.AbstractInterfaceSignatureResolver;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class ImportInterfaceSignatureResolver
    extends
      AbstractInterfaceSignatureResolver {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public IDLLocator idlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the InterfaceSignatureResolver interface
  // ---------------------------------------------------------------------------

  public InterfaceDefinition resolve(final TypeInterface itf,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    itf.setSignature(resolveName(itf.getSignature(), encapsulatingDefinition,
        context));
    return clientResolverItf.resolve(itf, encapsulatingDefinition, context);
  }

  // ---------------------------------------------------------------------------
  // Utility method
  // ---------------------------------------------------------------------------

  protected String resolveName(final String name,
      final Definition encapsilatingDefinition,
      final Map<Object, Object> context) {
    if (name.contains(".")) {
      return name;
    }

    final Import[] imports = (encapsilatingDefinition instanceof ImportContainer)
        ? ((ImportContainer) encapsilatingDefinition).getImports()
        : null;

    if (imports != null) {
      for (final Import imp : imports) {
        if (isOnDemandImport(imp)) {
          // on-demand import.

          final String fullyQualifiedName = imp.getPackageName() + '.' + name;
          if (idlLocatorItf.findBinaryItf(fullyQualifiedName, context) != null
              || idlLocatorItf.findSourceItf(fullyQualifiedName, context) != null) {
            return fullyQualifiedName;
          }
        } else {
          if (imp.getSimpleName().equals(name)) {
            // import simple name matches
            setUsedImport(imp);
            return imp.getPackageName() + '.' + name;
          }
        }
      }
    }

    if (encapsilatingDefinition != null) {
      final String packageName = getPackageName(encapsilatingDefinition
          .getName());
      // try in current package.
      if (packageName != null) {
        final String fullyQualifiedName = packageName + '.' + name;
        if (idlLocatorItf.findBinaryItf(fullyQualifiedName, context) != null
            || idlLocatorItf.findSourceItf(fullyQualifiedName, context) != null) {
          return fullyQualifiedName;
        }
      }
    }

    // no import match, return the name as it is (i.e. assume that it refers to
    // an ADL in the 'default package').
    return name;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = (IDLLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), IDLLocator.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      return idlLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
