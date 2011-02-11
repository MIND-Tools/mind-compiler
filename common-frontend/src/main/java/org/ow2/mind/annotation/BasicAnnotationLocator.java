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

import java.util.List;
import java.util.Map;

import org.ow2.mind.plugin.PluginManager;

import com.google.inject.Inject;

public class BasicAnnotationLocator implements AnnotationLocator {

  @Inject
  protected PluginManager pluginLoaderItf;

  public Class<? extends Annotation> findAnnotationClass(final String name,
      final Map<Object, Object> context) throws ClassNotFoundException,
      ClassCastException {
    try {
      return getClass().getClassLoader().loadClass(name)
          .asSubclass(Annotation.class);
    } catch (final ClassNotFoundException e) {
      // If the class cannot be loaded directly, look into the context if there
      // are default packages where we need to look for annotations.
      final List<String> packageList = PredefinedAnnotationsHelper
          .getPredefinedAnnotations(pluginLoaderItf, context);
      for (final String packageName : packageList) {
        try {
          final String qualifiedName = packageName + "." + name;
          return getClass().getClassLoader().loadClass(qualifiedName)
              .asSubclass(Annotation.class);
        } catch (final ClassNotFoundException e1) {
        }
      }
      throw e;
    }
  }

}
