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

import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import junit.framework.TestCase;

import org.ow2.mind.idl.BasicIncludeResolver;
import org.ow2.mind.idl.BasicInterfaceReferenceResolver;
import org.ow2.mind.idl.CacheIDLLoader;
import org.ow2.mind.idl.CachingIncludeResolver;
import org.ow2.mind.idl.ExtendsInterfaceLoader;
import org.ow2.mind.idl.IncludeHeaderResolver;
import org.ow2.mind.idl.IncludeLoader;
import org.ow2.mind.idl.InputResourcesIncludeResolver;
import org.ow2.mind.idl.InputResourcesInterfaceReferenceResolver;
import org.ow2.mind.idl.KindDecorationLoader;
import org.ow2.mind.idl.ReferencedInterfaceResolver;
import org.ow2.mind.idl.parser.IDLFileLoader;

public class TestLoader extends TestCase {

  public void testIDLFileLoaderBC() throws Exception {
    checkBCImplementation(new IDLFileLoader());
  }

  public void testIncludeLoaderBC() throws Exception {
    checkBCImplementation(new IncludeLoader());
  }

  public void testExtendsInterfaceLoaderBC() throws Exception {
    checkBCImplementation(new ExtendsInterfaceLoader());
  }

  public void testKindDecorationLoaderBC() throws Exception {
    checkBCImplementation(new KindDecorationLoader());
  }

  public void testCacheIDLLoaderBC() throws Exception {
    checkBCImplementation(new CacheIDLLoader());
  }

  public void testBasicIncludeResolverBC() throws Exception {
    checkBCImplementation(new BasicIncludeResolver());
  }

  public void testIncludeHeaderResolverBC() throws Exception {
    checkBCImplementation(new IncludeHeaderResolver());
  }

  public void testInputResourcesIncludeResolverBC() throws Exception {
    checkBCImplementation(new InputResourcesIncludeResolver());
  }

  public void testCachingIncludeResolverBC() throws Exception {
    checkBCImplementation(new CachingIncludeResolver());
  }

  public void testBasicInterfaceReferenceResolverBC() throws Exception {
    checkBCImplementation(new BasicInterfaceReferenceResolver());
  }

  public void testInputResourcesInterfaceReferenceResolverBC() throws Exception {
    checkBCImplementation(new InputResourcesInterfaceReferenceResolver());
  }

  public void testReferencedInterfaceResolverBC() throws Exception {
    checkBCImplementation(new ReferencedInterfaceResolver());
  }
}
