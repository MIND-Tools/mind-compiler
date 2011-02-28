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

package org.ow2.mind;

import org.ow2.mind.adl.compilation.AnnotationFlagsCompilationCommandFactory;
import org.ow2.mind.adl.compilation.BasicCompilationCommandFactory;
import org.ow2.mind.adl.compilation.CompilationCommandFactory;
import org.ow2.mind.adl.compilation.ContextFlagsCompilationCommandFactory;
import org.ow2.mind.inject.AbstractMindModule;
import org.ow2.mind.target.BasicTargetDescriptorLoader;
import org.ow2.mind.target.CacheLoader;
import org.ow2.mind.target.ExtensionLoader;
import org.ow2.mind.target.InterpolationLoader;
import org.ow2.mind.target.LinkerScriptLoader;
import org.ow2.mind.target.TargetCompilationCommandFactory;
import org.ow2.mind.target.TargetDescriptorLoader;

public class MindcModule extends AbstractMindModule {

  protected void configureADLCompiler() {
    bind(ADLCompiler.class).to(BasicADLCompiler.class);
  }

  protected void configureTargetDescriptorLoader() {
    bind(TargetDescriptorLoader.class).toChainStartingWith(CacheLoader.class)
        .followedBy(LinkerScriptLoader.class)
        .followedBy(InterpolationLoader.class)
        .followedBy(ExtensionLoader.class)
        .endingWith(BasicTargetDescriptorLoader.class);
  }

  // override ADLBackend
  protected void configureCompilationCommandFactory() {
    bind(CompilationCommandFactory.class)
        .toChainStartingWith(AnnotationFlagsCompilationCommandFactory.class)
        .followedBy(ContextFlagsCompilationCommandFactory.class)
        .followedBy(TargetCompilationCommandFactory.class)
        .endingWith(BasicCompilationCommandFactory.class);
  }
}
