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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.graph.ComponentGraph;

public final class CompilationDecorationHelper {

  private CompilationDecorationHelper() {
  }

  // ---------------------------------------------------------------------------
  // Additional Compilation Units
  // ---------------------------------------------------------------------------

  public static final String ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME = "additional-compilation-units";

  public static void addAdditionalCompilationUnit(final Definition def,
      final AdditionalCompilationUnitDecoration decoration) {
    AdditionalCompilationUnitDecorationContainer deco = getDecoration(def);
    if (deco == null) {
      deco = new AdditionalCompilationUnitDecorationContainer();
      def.astSetDecoration(ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME, deco);
    }
    deco.list.add(decoration);
  }

  public static List<AdditionalCompilationUnitDecoration> getAdditionalCompilationUnit(
      final Definition def) {
    final AdditionalCompilationUnitDecorationContainer deco = getDecoration(def);
    if (deco != null)
      return deco.list;
    else
      return Collections.emptyList();
  }

  private static AdditionalCompilationUnitDecorationContainer getDecoration(
      final Definition def) {
    return (AdditionalCompilationUnitDecorationContainer) def
        .astGetDecoration(ADDITIONAL_COMPILATION_UNIT_DECORATION_NAME);
  }

  /*
   * This class is used to wrap AdditionalCompilationUnitDecoration list in a
   * non serializable object to avoid the serialization of these decorations.
   */
  private static class AdditionalCompilationUnitDecorationContainer {
    protected final List<AdditionalCompilationUnitDecoration> list = new ArrayList<AdditionalCompilationUnitDecoration>();
  }

  public static class AdditionalCompilationUnitDecoration {
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
  }

  // ---------------------------------------------------------------------------
  // Object Files
  // ---------------------------------------------------------------------------

  public static final String OBJECT_FILE_DECORATION_NAME = "object-files";

  public static void addObjectFiles(final Definition def, final String path) {
    Set<String> decoration = getObjectFileDecoration(def);
    if (decoration == null) {
      decoration = new HashSet<String>();
      def.astSetDecoration(OBJECT_FILE_DECORATION_NAME, decoration);
    }
    decoration.add(path);
  }

  public static void addObjectFiles(final Definition def,
      final Collection<String> paths) {
    Set<String> decoration = getObjectFileDecoration(def);
    if (decoration == null) {
      decoration = new HashSet<String>();
      def.astSetDecoration(OBJECT_FILE_DECORATION_NAME, decoration);
    }
    for (final String path : paths) {
      decoration.add(path);
    }
  }

  public static Set<String> getObjectFiles(final Definition def) {
    return getObjectFileDecoration(def);
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getObjectFileDecoration(final Definition def) {
    return (Set<String>) def.astGetDecoration(OBJECT_FILE_DECORATION_NAME);
  }

  public static void addObjectFiles(final ComponentGraph graph,
      final String path) {
    Set<String> decoration = getObjectFileDecoration(graph);
    if (decoration == null) {
      decoration = new HashSet<String>();
      graph.setDecoration(OBJECT_FILE_DECORATION_NAME, decoration);
    }
    decoration.add(path);
  }

  public static Set<String> getObjectFiles(final ComponentGraph graph) {
    return getObjectFileDecoration(graph);
  }

  @SuppressWarnings("unchecked")
  private static Set<String> getObjectFileDecoration(final ComponentGraph graph) {
    return (Set<String>) graph.getDecoration(OBJECT_FILE_DECORATION_NAME);
  }

}
