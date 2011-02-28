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

package org.ow2.mind.compilation;

import java.io.File;
import java.util.Collection;

public interface LinkerCommand extends CompilationCommand {

  LinkerCommand addDebugFlag();

  LinkerCommand addFlag(String flag);

  LinkerCommand addFlags(Collection<String> flags);

  LinkerCommand addFlags(String... flags);

  LinkerCommand setLinkerScript(String linkerScript);

  LinkerCommand setOptimizationLevel(String level);

  LinkerCommand setOutputFile(File outputFile);

  LinkerCommand addLib(String libName);

  LinkerCommand addInputFile(File inputFile);

  LinkerCommand addInputFiles(Collection<File> inputFiles);

  LinkerCommand addInputFiles(File... inputFiles);

}
