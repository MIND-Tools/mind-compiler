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
 * Contributors: Julien Tous
 */

package org.ow2.mind.compilation;

import java.io.File;
import java.util.Collection;

public interface AssemblerCommand extends CompilationCommand {

  AssemblerCommand addDebugFlag();

  AssemblerCommand addFlag(String flag);

  AssemblerCommand addFlags(Collection<String> flags);

  AssemblerCommand addFlags(String... flags);

  AssemblerCommand addDefine(String name);

  AssemblerCommand addDefine(String name, String value);

  AssemblerCommand addIncludeDir(File includeDir);

  AssemblerCommand addIncludeFile(File includeFile);

  AssemblerCommand setOptimizationLevel(String level);

  AssemblerCommand setOutputFile(File outputFile);

  AssemblerCommand setInputFile(File inputFile);

  File getOutputFile();

  File getInputFile();

  AssemblerCommand addDependency(File dependency);

  AssemblerCommand setAllDependenciesManaged(boolean dependencyManaged);

  AssemblerCommand setDependencyOutputFile(File dependencyOutputFile);
}
