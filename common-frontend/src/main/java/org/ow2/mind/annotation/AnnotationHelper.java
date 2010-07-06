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

package org.ow2.mind.annotation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.MergeableDecoration;

public final class AnnotationHelper {
  private AnnotationHelper() {
  }

  public static final String ANNOTATION_DECORATION_NAME = "annotations";

  public static void addAnnotation(final Node container,
      final Annotation annotation) throws ADLException {
    AnnotationDecoration decoration = getDecoration(container);
    if (decoration == null) {
      decoration = new AnnotationDecoration();
      addDecoration(container, decoration);
    }

    decoration.addAnnotation(annotation);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Annotation> T getAnnotation(final Node container,
      final Class<T> annotationClass) {
    final AnnotationDecoration decoration = getDecoration(container);
    if (decoration == null)
      return null;
    else
      return (T) decoration.getAnnotation(annotationClass);
  }

  public static Annotation[] getAnnotations(final Node container) {
    final AnnotationDecoration decoration = getDecoration(container);
    if (decoration == null)
      return new Annotation[0];
    else
      return decoration.getAnnotations();
  }

  private static AnnotationDecoration getDecoration(final Node container) {
    return (AnnotationDecoration) container
        .astGetDecoration(ANNOTATION_DECORATION_NAME);
  }

  private static void addDecoration(final Node container,
      final AnnotationDecoration decoration) {
    container.astSetDecoration(ANNOTATION_DECORATION_NAME, decoration);
  }

  public static class AnnotationDecoration
      implements
        MergeableDecoration,
        Externalizable {

    final Map<Class<? extends Annotation>, Annotation> annotations = new IdentityHashMap<Class<? extends Annotation>, Annotation>();

    void addAnnotation(final Annotation annotation) throws ADLException {
      final Annotation previousAnnotation = annotations.put(
          annotation.getClass(), annotation);
      if (previousAnnotation != null) {
        throw new IllegalArgumentException(
            "Can't specify the same annotation several time on a given element");
      }
    }

    Annotation getAnnotation(final Class<? extends Annotation> annotationClass) {
      return annotations.get(annotationClass);
    }

    /**
     * To be used from string template
     * 
     * @return a map associating annotation class names to annotation objects.
     */
    public Map<String, Annotation> getAnnotationMap() {
      final Map<String, Annotation> map = new HashMap<String, Annotation>(
          annotations.size());
      for (final Map.Entry<Class<? extends Annotation>, Annotation> e : annotations
          .entrySet()) {
        map.put(e.getKey().getName(), e.getValue());
      }
      return map;
    }

    Annotation[] getAnnotations() {
      return annotations.values().toArray(new Annotation[0]);
    }

    public Object mergeDecoration(final Object overridingDecoration)
        throws MergeException {
      final AnnotationDecoration newDecoration = new AnnotationDecoration();
      if (overridingDecoration == null) {
        // no overridding annotations
        // returns only annotations that are marked as "inherited".
        for (final Annotation overriddenAnno : getAnnotations()) {
          if (overriddenAnno.isInherited()) {
            // TODO should we clone the annotation ?
            newDecoration.annotations.put(overriddenAnno.getClass(),
                overriddenAnno);
          }
        }
      } else {
        final AnnotationDecoration overridingAnnotations = (AnnotationDecoration) overridingDecoration;
        // returns overridingAnnotations + overridden annotations that are
        // marked as "inherited"
        newDecoration.annotations.putAll(overridingAnnotations.annotations);
        for (final Annotation overriddenAnno : getAnnotations()) {
          if (!newDecoration.annotations.containsKey(overriddenAnno.getClass())
              && overriddenAnno.isInherited()) {
            // TODO should we clone the annotation ?
            newDecoration.annotations.put(overriddenAnno.getClass(),
                overriddenAnno);
          }
        }
      }
      return newDecoration;
    }

    public void readExternal(final ObjectInput in) throws IOException,
        ClassNotFoundException {
      final int size = in.readInt();
      for (int i = 0; i < size; i++) {
        final String className = in.readUTF();
        final Class<? extends Annotation> c = getClass().getClassLoader()
            .loadClass(className).asSubclass(Annotation.class);
        final Annotation a = (Annotation) in.readObject();
        annotations.put(c, a);
      }
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
      out.writeInt(annotations.size());
      for (final Map.Entry<Class<? extends Annotation>, Annotation> entry : annotations
          .entrySet()) {
        out.writeUTF(entry.getKey().getName());
        out.writeObject(entry.getValue());
      }
    }

  }
}
