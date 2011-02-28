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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;

/**
 * Interface used to compile ADL.
 */
public interface ADLCompiler {

  /**
   * Enumeration of compilation stages
   */
  enum CompilationStage {
    CHECK_ADL, GENERATE_SRC, COMPILE_DEF, COMPILE_EXE
  }

  /**
   * Compiles the ADL called <code>adlName</code>.
   * 
   * @param adlName the name of the ADL to compile
   * @param execName the executable name of the compiled ADL. Required only for
   *          {@link CompilationStage#COMPILE_EXE} stage.
   * @param stage the compilation stage.
   * @param context the compilation context.
   * @return the result of the compilation. The content of the list depends on
   *         the compilation stage.
   *         <ul>
   *         <li>If stage is {@link CompilationStage#CHECK_ADL} or
   *         {@link CompilationStage#GENERATE_SRC}, the list contains the
   *         {@link Definition} node of the checked ADL.</li>
   *         <li>If stage is {@link CompilationStage#COMPILE_DEF}, the list
   *         contains the {@link File output files} of every compilation (i.e.
   *         the object files).</li>
   *         <li>If stage is {@link CompilationStage#COMPILE_EXE}, the list
   *         contains the {@link File output files} of linker (usually only one
   *         file).
   * @throws ADLException if an error occurs while loading ADL.
   * @throws InterruptedException of the compilation has been interrupted.
   */
  List<Object> compile(String adlName, String execName, CompilationStage stage,
      Map<Object, Object> context) throws ADLException, InterruptedException;
}
