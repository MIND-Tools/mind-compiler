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

package org.ow2.mind.io;

import java.io.File;
import java.util.Map;

public interface OutputFileLocator {

  String ITF_NAME = "output-file-locator";

  File getCSourceOutputFile(String path, Map<Object, Object> context);

  File getCSourceOutputDir(Map<Object, Object> context);

  File getCSourceTemporaryOutputFile(String path, Map<Object, Object> context);

  File getCSourceTemporaryOutputDir(Map<Object, Object> context);

  File getCCompiledOutputFile(String path, Map<Object, Object> context);

  File getCExecutableOutputFile(String path, Map<Object, Object> context);

  File getCCompiledOutputDir(Map<Object, Object> context);

  File getCCompiledTemporaryOutputFile(String path, Map<Object, Object> context);

  File getCCompiledTemporaryOutputDir(Map<Object, Object> context);

  File getMetadataOutputFile(String path, Map<Object, Object> context);
}
