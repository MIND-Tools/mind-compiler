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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnnotationLocatorHelper {

  public static final String DEFAULT_ANNOTATION_PACKAGES = "default-annotation-packages";

  private AnnotationLocatorHelper() {
  }

  public static List<String> getDefaultAnnotationPackages(
      final Map<Object, Object> context) {
    return getPackageListDecoration(context);
  }

  public static void addDefaultAnnotationPackage(final String packageName,
      final Map<Object, Object> context) {
    final List<String> list = getPackageListDecoration(context);
    list.add(packageName);
  }

  public static void removeDefaultAnnotationPackage(final String packageName,
      final Map<Object, Object> context) {
    final List<String> list = getPackageListDecoration(context);
    list.remove(packageName);
  }

  @SuppressWarnings("unchecked")
  private static List<String> getPackageListDecoration(
      final Map<Object, Object> context) {
    List<String> list = (List<String>) context.get(DEFAULT_ANNOTATION_PACKAGES);
    if (list == null) {
      list = new ArrayList<String>();
      context.put(DEFAULT_ANNOTATION_PACKAGES, list);
    }
    return list;
  }

}
