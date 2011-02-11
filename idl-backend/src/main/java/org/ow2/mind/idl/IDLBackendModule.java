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

import org.ow2.mind.inject.AbstractMindModule;

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class IDLBackendModule extends AbstractMindModule {

  protected void configureIDLVisitor() {
    bind(IDLVisitor.class).toChainStartingWith(IncludeCompiler.class)
        .endingWith(IDLVisitorDispatcher.class);

    final Multibinder<IDLVisitor> setBinder = Multibinder.newSetBinder(
        binder(), IDLVisitor.class);
    setBinder.addBinding().to(IDLHeaderCompiler.class);
    setBinder.addBinding().to(BinaryIDLWriter.class);
  }

  protected void configureIDLHeaderCompilerGenerator() {
    bind(String.class).annotatedWith(
        Names.named(IDLHeaderCompiler.TEMPLATE_NAME)).toInstance(
        IDLHeaderCompiler.DEFAULT_TEMPLATE);
  }
}
