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

package org.ow2.mind.adl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.objectweb.fractal.adl.Definition;

public final class AdditionalCompilationUnitDecoration implements Serializable {

  protected String           path;
  protected boolean          generatedFile;
  protected Collection<File> dependencies;

  public AdditionalCompilationUnitDecoration(final String path,
      final boolean generatedFile, final File... dependencies) {
    this.path = path;
    this.generatedFile = generatedFile;
    if (dependencies != null && dependencies.length > 0)
      this.dependencies = Arrays.asList(dependencies);
  }

  public AdditionalCompilationUnitDecoration(final String path,
      final boolean generatedFile, final Collection<File> dependencies) {
    this.path = path;
    this.generatedFile = generatedFile;
    this.dependencies = dependencies;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the dependencies
   */
  public Collection<File> getDependencies() {
    if (dependencies == null)
      return Collections.emptyList();
    else
      return dependencies;
  }

  /**
   * @return the generatedFile
   */
  public boolean isGeneratedFile() {
    return generatedFile;
  }

  public static final String ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME = "additional-compilation-units";

  public static void addAdditionalCompilationUnit(final Definition def,
      final AdditionalCompilationUnitDecoration decoration) {
    List<AdditionalCompilationUnitDecoration> list = getDecoration(def);
    if (list == null) {
      list = new ArrayList<AdditionalCompilationUnitDecoration>();
      def.astSetDecoration(ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME, list);
    }
    list.add(decoration);
  }

  public static List<AdditionalCompilationUnitDecoration> getAdditionalCompilationUnit(
      final Definition def) {
    final List<AdditionalCompilationUnitDecoration> list = getDecoration(def);
    if (list != null)
      return list;
    else
      return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  private static List<AdditionalCompilationUnitDecoration> getDecoration(
      final Definition def) {
    return (List<AdditionalCompilationUnitDecoration>) def
        .astGetDecoration(ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME);
  }

}
