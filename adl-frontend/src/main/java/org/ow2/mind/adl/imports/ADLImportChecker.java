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

import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLLocator;

public class ADLImportChecker extends AbstractDelegatingImportChecker {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public ADLLocator adlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of abstract methods of AbstractDelegatingImportChecker
  // ---------------------------------------------------------------------------

  @Override
  protected boolean isValidName(final String pckgName, final String simpleName,
      final Map<Object, Object> context) {
    final String name = pckgName + "." + simpleName;
    return adlLocatorItf.findBinaryADL(name, context) != null
        || adlLocatorItf.findSourceADL(name, context) != null;
  }

  @Override
  protected boolean isValidPackage(final String pckgName,
      final Map<Object, Object> context) {
    // Always return true since this import checker is not able to check package
    // name.
    return true;
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = (ADLLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), ADLLocator.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      return adlLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ADLLocator.ITF_NAME)) {
      adlLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
