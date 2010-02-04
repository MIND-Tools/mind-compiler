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

import static org.ow2.mind.NameHelper.getPackageName;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.isOnDemandImport;
import static org.ow2.mind.adl.imports.ast.ImportASTHelper.setUsedImport;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.AbstractDefinitionReferenceResolver;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.imports.ast.Import;
import org.ow2.mind.adl.imports.ast.ImportContainer;

/**
 * Delegating {@link DefinitionReferenceResolver} that uses {@link Import} nodes
 * of the <code>encapsulatingDefinition</code> to complete the name contained in
 * the definition reference to resolve.
 */
public class ImportDefinitionReferenceResolver
    extends
      AbstractDefinitionReferenceResolver {

  // ---------------------------------------------------------------------------
  // Client interface
  // ---------------------------------------------------------------------------

  /** Client interface used to resolve on-demand import. */
  public ADLLocator adlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionReferenceResolver interface
  // ---------------------------------------------------------------------------

  public Definition resolve(final DefinitionReference reference,
      final Definition encapsulatingDefinition,
      final Map<Object, Object> context) throws ADLException {
    reference.setName(resolveName(reference.getName(), encapsulatingDefinition,
        context));
    return clientResolverItf.resolve(reference, encapsulatingDefinition,
        context);
  }

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
          if (adlLocatorItf.findBinaryADL(fullyQualifiedName, context) != null
              || adlLocatorItf.findSourceADL(fullyQualifiedName, context) != null) {
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
        if (adlLocatorItf.findBinaryADL(fullyQualifiedName, context) != null
            || adlLocatorItf.findSourceADL(fullyQualifiedName, context) != null) {
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

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = (ADLLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    final String[] superList = super.listFc();
    final String[] list = new String[superList.length + 1];
    list[0] = ADLLocator.ITF_NAME;
    System.arraycopy(superList, 0, list, 1, superList.length);
    return list;
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      return adlLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }

}
