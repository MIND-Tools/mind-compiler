/**
 * Copyright (C) 2011 STMicroelectronics
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

package org.ow2.mind.target.ast;

import org.objectweb.fractal.adl.Node;

public interface Target extends Node {

  String getName();

  void setName(String name);

  void addExtends(Extends extend);

  void removeExtends(Extends extend);

  Extends[] getExtendss();

  ADLMapping getAdlMapping();

  void setAdlMapping(ADLMapping mapping);

  Compiler getCompiler();

  void setCompiler(Compiler compiler);

  Assembler getAssembler();

  void setAssembler(Assembler assembler);

  Linker getLinker();

  void setLinker(Linker linker);

  LinkerScript getLinkerScript();

  void setLinkerScript(LinkerScript linkerScript);

  Archiver getArchiver();

  void setArchiver(Archiver archiver);

  void addCppFlag(Flag flag);

  void removeCppFlag(Flag flag);

  Flag[] getCppFlags();

  void addCFlag(Flag flag);

  void removeCFlag(Flag flag);

  Flag[] getCFlags();

  void addLdFlag(Flag flag);

  void removeLdFlag(Flag flag);

  Flag[] getLdFlags();

  void addAsFlag(Flag flag);

  void removeAsFlag(Flag flag);

  Flag[] getAsFlags();

}
