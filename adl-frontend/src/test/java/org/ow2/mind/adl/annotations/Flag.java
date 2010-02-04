
package org.ow2.mind.adl.annotations;

import org.ow2.mind.adl.annotation.ADLAnnotationTarget;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationElement;
import org.ow2.mind.annotation.AnnotationTarget;

public class Flag implements Annotation {

  private static final AnnotationTarget[] TARGETS = {ADLAnnotationTarget.SOURCE};

  @AnnotationElement()
  public String                           value;

  public AnnotationTarget[] getAnnotationTargets() {
    return TARGETS;
  }

  public boolean isInherited() {
    return false;
  }

}
