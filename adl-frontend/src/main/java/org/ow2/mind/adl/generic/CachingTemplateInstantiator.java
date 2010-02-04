
package org.ow2.mind.adl.generic;

import static org.ow2.mind.adl.generic.ast.GenericASTHelper.getTemplateName;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.isPartiallyInstantiatedTemplate;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setTemplateName;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.DefinitionCache;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterContainer;
import org.ow2.mind.adl.generic.ast.TypeArgument;

/**
 * Delegating {@link TemplateInstantiator} component that uses a
 * {@link DefinitionCache} to reuse already instantiated template. To do so this
 * component builds a context-free name for each instantiated template that
 * identify it based on the value of its formal type parameters.
 */
public class CachingTemplateInstantiator
    implements
      TemplateInstantiator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The interface used to resolve referenced definitions. */
  public DefinitionReferenceResolver definitionReferenceResolverItf;

  /** The name of the {@link #clientInstantiatorItf} client interface. */
  public static final String         CLIENT_INSTANTIATOR_ITF_NAME = "client-instantiator";

  /**
   * The client {@link TemplateInstantiator} interface used to actually
   * instantiate template if it has not been found in cache.
   */
  public TemplateInstantiator        clientInstantiatorItf;

  /** The cache of loaded definition. */
  public DefinitionCache             definitionCacheItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    // Try to get the template name from the 'templateName' decoration'. This is
    // useful if the template to instantiate is a partially instantiated
    // template.
    String templateName = getTemplateName(genericDefinition);
    // if the name is not found as a decoration, get it in the 'name' attribute.
    if (templateName == null) templateName = genericDefinition.getName();

    // build template instance name
    final StringBuilder sb = new StringBuilder(templateName).append('<');
    final FormalTypeParameter[] formalTypeParameters = ((FormalTypeParameterContainer) genericDefinition)
        .getFormalTypeParameters();
    assert formalTypeParameters.length > 0;

    // for each formal type parameter
    for (int i = 0; i < formalTypeParameters.length; i++) {
      if (i != 0) sb.append(',');

      final FormalTypeParameter formalTypeParameter = formalTypeParameters[i];
      // get value of formal type parameter
      final Object typeArgumentValue = typeArgumentValues
          .get(formalTypeParameter.getName());
      assert typeArgumentValue != null;
      if (typeArgumentValue instanceof FormalTypeParameter) {
        // if the value is type parameter of the surrounding definition,
        // append the name of the type of this type parameter.
        sb.append(((FormalTypeParameter) typeArgumentValue)
            .getDefinitionReference().getName());
      } else {
        assert typeArgumentValue instanceof TypeArgument;
        final DefinitionReference defRef = ((TypeArgument) typeArgumentValue)
            .getDefinitionReference();
        if (defRef == null) {
          // If the value if the ANY-DEFINITION, append a '?'
          sb.append('?');
        } else {
          // If the value is a reference to a definition append the name of the
          // referenced definition.
          sb.append(definitionReferenceResolverItf.resolve(defRef, null,
              context).getName());
        }
      }
    }
    sb.append('>');

    final String templateInstanceName = sb.toString();

    // try to retrieve template instance from cache
    Definition templateInstance = definitionCacheItf.getInCache(
        templateInstanceName, context);

    if (templateInstance == null) {
      // Not found in cache
      templateInstance = clientInstantiatorItf.instantiateTemplate(
          genericDefinition, typeArgumentValues, context);

      // if the template is partially instantiated (i.e. the template instance
      // is itself a template), store the initial name of
      // the template in a decoration for future re-instantiation of this
      // template.
      if (isPartiallyInstantiatedTemplate(templateInstance))
        setTemplateName(templateInstance, templateName);

      // Set name of template instance.
      templateInstance.setName(templateInstanceName);

      // put in cache
      definitionCacheItf.addInCache(templateInstance, context);
    }

    return templateInstance;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = (TemplateInstantiator) value;
    } else if (itfName.equals(DefinitionCache.ITF_NAME)) {
      definitionCacheItf = (DefinitionCache) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    return new String[]{DefinitionReferenceResolver.ITF_NAME,
        CLIENT_INSTANTIATOR_ITF_NAME, DefinitionCache.ITF_NAME};
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      return clientInstantiatorItf;
    } else if (itfName.equals(DefinitionCache.ITF_NAME)) {
      return definitionCacheItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {

    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = null;
    } else if (itfName.equals(DefinitionCache.ITF_NAME)) {
      definitionCacheItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
