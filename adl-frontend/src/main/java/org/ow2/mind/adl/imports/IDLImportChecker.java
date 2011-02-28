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

import java.util.Map;

import org.ow2.mind.idl.IDLLocator;

import com.google.inject.Inject;

public class IDLImportChecker extends AbstractDelegatingImportChecker {

  @Inject
  protected IDLLocator idlLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of abstract methods of AbstractDelegatingImportChecker
  // ---------------------------------------------------------------------------

  @Override
  protected boolean isValidName(final String pckgName, final String simpleName,
      final Map<Object, Object> context) {
    final String name = pckgName + "." + simpleName;
    return idlLocatorItf.findBinaryItf(name, context) != null
        || idlLocatorItf.findSourceItf(name, context) != null;
  }

  @Override
  protected boolean isValidPackage(final String pckgName,
      final Map<Object, Object> context) {
    // Always return true since this import checker is not able to check package
    // name.
    return true;
  }
}
