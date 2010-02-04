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

import static org.ow2.mind.adl.imports.ast.ImportASTHelper.isOnDemandImport;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.imports.ast.Import;

public abstract class AbstractDelegatingImportChecker
    implements
      ImportChecker,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_CHECKER_ITF_NAME = "client-validator";

  public ImportChecker       clientCheckerOptItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ImportChecker interface
  // ---------------------------------------------------------------------------

  public void checkImport(final Import imp, final Map<Object, Object> context)
      throws ADLException {
    boolean isValid;
    if (isOnDemandImport(imp)) {
      isValid = isValidPackage(imp.getPackageName(), context);
    } else {
      isValid = isValidName(imp.getPackageName(), imp.getSimpleName(), context);
    }

    if (!isValid) {
      if (clientCheckerOptItf != null) {
        clientCheckerOptItf.checkImport(imp, context);
      } else {
        throw new ADLException(ADLErrors.UNKNOWN_IMPORT, imp);
      }
    }
  }

  public void checkOnDemandImport(final Import imp, final String simpleName,
      final Map<Object, Object> context) throws ADLException {
    if (!isOnDemandImport(imp)) {
      throw new IllegalArgumentException("imp is not an on-demand import");
    }
    final boolean isValid = isValidName(imp.getPackageName(), imp
        .getSimpleName(), context);
    if (!isValid) {
      if (clientCheckerOptItf != null) {
        clientCheckerOptItf.checkImport(imp, context);
      } else {
        throw new ADLException(ADLErrors.UNKNOWN_IMPORT, imp);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Abstract methods
  // ---------------------------------------------------------------------------

  protected abstract boolean isValidName(String pckgName, String simpleName,
      Map<Object, Object> context);

  protected abstract boolean isValidPackage(String pckgName,
      Map<Object, Object> context);

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return new String[]{CLIENT_CHECKER_ITF_NAME};
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    if (s == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (CLIENT_CHECKER_ITF_NAME.equals(s)) {
      return clientCheckerOptItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    if (s == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (CLIENT_CHECKER_ITF_NAME.equals(s)) {
      clientCheckerOptItf = (ImportChecker) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    if (s == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (CLIENT_CHECKER_ITF_NAME.equals(s)) {
      clientCheckerOptItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

}
