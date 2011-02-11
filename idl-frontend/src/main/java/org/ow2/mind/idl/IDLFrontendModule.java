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

import org.ow2.mind.idl.annotation.AnnotationLoader;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.parser.IDLFileLoader;

public class IDLFrontendModule extends AbstractIDLFrontendModule {

  protected void configureIDLLoader() {
    bind(IDLLoader.class)
        .toChainStartingWith(CacheIDLLoader.class)
        .followedBy(HeaderLoader.class)
        .followedBy(BinaryIDLLoader.class)
        .followedBy(
            new AnnotationProcessorProvider(IDLLoaderPhase.AFTER_CHECKING))
        .followedBy(KindDecorationLoader.class)
        .followedBy(IDLTypeCheckerLoader.class)
        .followedBy(ExtendsInterfaceLoader.class)
        .followedBy(IncludeLoader.class)
        .followedBy(
            new AnnotationProcessorProvider(IDLLoaderPhase.AFTER_PARSING))
        .followedBy(AnnotationLoader.class).endingWith(IDLFileLoader.class);
  }

  protected void configureIDLCache() {
    bind(IDLCache.class).to(CacheIDLLoader.class);
  }

  protected void configureIncludeResolver() {
    bind(IncludeResolver.class)
        .toChainStartingWith(CachingIncludeResolver.class)
        .followedBy(InputResourcesIncludeResolver.class)
        .followedBy(IncludeHeaderResolver.class)
        .endingWith(BasicIncludeResolver.class);
  }

  public void configureInterfaceReferenceResolver() {
    bind(InterfaceReferenceResolver.class)
        .toChainStartingWith(ReferencedInterfaceResolver.class)
        .followedBy(InputResourcesInterfaceReferenceResolver.class)
        .endingWith(BasicInterfaceReferenceResolver.class);
  }

  protected void configureRecursiveIDLLoader() {
    bind(RecursiveIDLLoader.class).to(RecursiveIDLLoaderImpl.class);
  }

}
