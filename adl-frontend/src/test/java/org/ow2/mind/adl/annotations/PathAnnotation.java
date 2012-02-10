
package org.ow2.mind.adl.annotations;

import java.net.URL;

import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;

public class PathAnnotation implements Annotation {

  private static final AnnotationTarget[] TARGETS = {ADLAnnotationTarget.SOURCE};

  @AnnotationElement(hasDefaultValue = true)
  public String                           value   = null;

  @AnnotationElement(hasDefaultValue = true)
  public URL                              url     = null;

  public AnnotationTarget[] getAnnotationTargets() {
    return TARGETS;
  }

  public boolean isInherited() {
    return false;
  }

}