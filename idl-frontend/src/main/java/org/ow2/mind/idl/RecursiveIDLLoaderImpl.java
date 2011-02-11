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

package org.ow2.mind.idl;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.idl.ast.IDL;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RecursiveIDLLoaderImpl implements RecursiveIDLLoader {

  protected ThreadLocal<Map<String, IDL>> loadingIDLs = new ThreadLocal<Map<String, IDL>>();

  @Inject
  protected IDLLoader                     clientIDLLoaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoader interface
  // ---------------------------------------------------------------------------

  public IDL load(final IDL encapsulatingIDL, final String name,
      final Map<Object, Object> context) throws ADLException {
    Map<String, IDL> loadingIDLs = this.loadingIDLs.get();
    if (loadingIDLs == null) {
      loadingIDLs = new HashMap<String, IDL>();
      this.loadingIDLs.set(loadingIDLs);
    }

    final IDL idl = loadingIDLs.get(name);
    if (idl != null) {
      // The IDL to resolve is already loading (i.e. cycle in included files)
      return idl;
    }

    assert !loadingIDLs.containsKey(encapsulatingIDL.getName());
    loadingIDLs.put(encapsulatingIDL.getName(), encapsulatingIDL);
    try {
      return clientIDLLoaderItf.load(name, context);
    } finally {
      loadingIDLs.remove(encapsulatingIDL.getName());
    }
  }
}
