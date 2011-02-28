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

package org.ow2.mind.adl.compilation;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.inject.InjectDelegate;
import org.ow2.mind.preproc.MPPCommand;

public interface CompilationCommandFactory {

  PreprocessorCommand newPreprocessorCommand(Definition definition,
      Object source, File inputFile, Collection<File> dependencies,
      File depFile, File outputFile, Map<Object, Object> context)
      throws ADLException;

  MPPCommand newMPPCommand(Definition definition, Object source,
      File inputFile, File outputFile, File headerOutputFile,
      Map<Object, Object> context) throws ADLException;

  CompilerCommand newCompilerCommand(Definition definition, Object source,
      File inputFile, boolean preprocessedFile, Collection<File> dependencies,
      File depFile, File outputFile, Map<Object, Object> context)
      throws ADLException;

  CompilerCommand newAssemblyCompilerCommand(Definition definition,
      Object source, File inputFile, File outputFile,
      Map<Object, Object> context) throws ADLException;

  LinkerCommand newLinkerCommand(ComponentGraph graph, File outputFile,
      Map<Object, Object> context) throws ADLException;

  CompilerCommand newFileProviderCompilerCommand(File outputFile,
      Map<Object, Object> context);

  public abstract class AbstractDelegatingCompilationCommandFactory
      implements
        CompilationCommandFactory {

    @InjectDelegate
    protected CompilationCommandFactory factoryDelegate;
  }
}
