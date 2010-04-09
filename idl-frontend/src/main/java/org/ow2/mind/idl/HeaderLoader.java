/**
 * Copyright (C) 2010 STMicroelectronics
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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.PathHelper.getExtension;
import static org.ow2.mind.idl.ast.Include.HEADER_EXTENSION;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.CommonASTHelper;
import org.ow2.mind.idl.ast.Header;
import org.ow2.mind.idl.ast.IDL;

public class HeaderLoader extends AbstractIDLLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /**
   * The {@link NodeFactory} client interface.
   */
  public NodeFactory nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the UsedIDLResolver interface
  // ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {
    if (HEADER_EXTENSION.equals(getExtension(name))) {
      // load a header C file.
      // create a new Header AST node
      final Header header = CommonASTHelper.newNode(nodeFactoryItf, "header",
          Header.class);
      header.setName(name);
      return header;
    } else {
      return clientIDLLoaderItf.load(name, context);
    }
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
