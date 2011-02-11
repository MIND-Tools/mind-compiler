
package org.ow2.mind.adl.generic;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.inject.InjectDelegate;

/**
 * Interface used to instantiate a generic definition.
 */
public interface TemplateInstantiator {

  /**
   * Instantiate the given generic definition with the given type arguments.
   * 
   * @param genericDefinition the generic definition to instantiate.
   * @param typeArgumentValues the type argument values. Keys are formal type
   *          parameter names; values are either a {@link FormalTypeParameter}
   *          if the type argument references a formal type parameter or a
   *          {@link TypeArgument} otherwise.
   * @param context additional parameters.
   * @return the instantiated template.
   * @throws ADLException if something goes wrong.
   */
  Definition instantiateTemplate(Definition genericDefinition,
      Map<String, Object> typeArgumentValues, Map<Object, Object> context)
      throws ADLException;

  /**
   * An abstract delegating {@link TemplateInstantiator} component.
   */
  public abstract class AbstractDelegatingTemplateInstantiator
      implements
        TemplateInstantiator {

    /**
     * The client {@link DefinitionReferenceResolver} used by this component.
     */
    @InjectDelegate
    protected TemplateInstantiator clientInstantiatorItf;
  }
}
