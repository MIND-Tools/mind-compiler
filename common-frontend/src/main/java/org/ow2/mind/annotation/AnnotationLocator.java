
package org.ow2.mind.annotation;

import java.util.Map;

public interface AnnotationLocator {

  Class<? extends Annotation> findAnnotationClass(String name,
      Map<Object, Object> context) throws ClassNotFoundException,
      ClassCastException;

  public abstract class AbstractDelegatingAnnotationLocator
      implements
        AnnotationLocator {
    public AnnotationLocator clientAnnotationLocatorItf;
  }
}
