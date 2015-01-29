
package org.ow2.mind.adl.generic;

import static org.objectweb.fractal.adl.NodeUtil.cloneGraph;
import static org.objectweb.fractal.adl.NodeUtil.cloneNode;
import static org.ow2.mind.adl.ast.ASTHelper.isType;
import static org.ow2.mind.adl.ast.ASTHelper.setResolvedComponentDefinition;
import static org.ow2.mind.adl.ast.ASTHelper.unsetResolvedDefinition;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.isPartiallyInstantiatedTemplate;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setAnyDefinition;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setPartiallyInstiantedTemplate;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.FormalTypeParameterReference;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

import com.google.inject.Inject;

/**
 * Basic implementation of the {@link TemplateInstantiator} interface. The
 * instantiated definition is context-free. In particular, for the given
 * template:
 * 
 * <pre>
 * composite Def1&lt;U conformsto Type1, V conformsto Type2&gt; { ... }
 * </pre>
 * 
 * The following instantiations give the same result:
 * 
 * <pre>
 * composite Def2&lt;W conformsto Type1, Z conformsto Type1&gt; {
 *   contains Def1&lt;W, Def3&gt; as c1;
 *   contains Def1&lt;Z, Def3&gt; as c2;
 * }
 * </pre>
 */
public class TemplateInstantiatorImpl implements TemplateInstantiator {

  @Inject
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    // clone the genericDefinition to create the template instance
    final Definition templateInstance = cloneGraph(genericDefinition);

    // un-set 'PartiallyInstiantedTemplate' decoration. Will be re-set if needed
    setPartiallyInstiantedTemplate(templateInstance, false);

    // look for references to template variables into sub components
    if (templateInstance instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) templateInstance)
          .getComponents()) {
        if (subComp.getDefinitionReference() != null) {
          final DefinitionReference defRef = subComp.getDefinitionReference();

          final Definition d = instantiateDefinitionReference(defRef,
              templateInstance, typeArgumentValues, context);
          setResolvedComponentDefinition(subComp, d);

          if (isPartiallyInstantiatedTemplate(d))
            setPartiallyInstiantedTemplate(templateInstance, true);
        } else if (subComp instanceof FormalTypeParameterReference
            && ((FormalTypeParameterReference) subComp)
                .getTypeParameterReference() != null) {
          // sub component is an instance of a formal type parameter.
          final FormalTypeParameterReference typeParamRef = (FormalTypeParameterReference) subComp;
          final String ref = typeParamRef.getTypeParameterReference();

          // get the value of the formal type parameter.
          final Object value = typeArgumentValues.get(ref);
          assert value != null;

          if (value instanceof FormalTypeParameter) {
            // the value of the formal type parameter references another formal
            // type parameter
            final FormalTypeParameter typeParameter = (FormalTypeParameter) value;
            final Definition subCompDef = definitionReferenceResolverItf
                .resolve(typeParameter.getDefinitionReference(), null, context);
            setResolvedComponentDefinition(subComp, subCompDef);
            setPartiallyInstiantedTemplate(templateInstance, true);
          } else {
            assert value instanceof TypeArgument;
            final DefinitionReference defRef = ((TypeArgument) value)
                .getDefinitionReference();
            if (defRef != null) {
              // the value of the formal type parameter is a definition
              // reference
              final Definition subCompDef = definitionReferenceResolverItf
                  .resolve(defRef, null, context);

              if (!isPartiallyInstantiatedTemplate(subCompDef)) {
                typeParamRef.setTypeParameterReference(null);
                subComp.setDefinitionReference(cloneNode(defRef));
              }

              setResolvedComponentDefinition(subComp, subCompDef);
              if (isPartiallyInstantiatedTemplate(subCompDef)
                  || isType(subCompDef))
                setPartiallyInstiantedTemplate(templateInstance, true);
            } else {
              // the value of the formal type parameter is "ANY"
              setAnyDefinition(subComp, (TypeArgument) value);
            }
          }
        }
      }
    }
    return templateInstance;
  }

  protected Definition instantiateDefinitionReference(
      final DefinitionReference defRef, final Definition templateInstance,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {

    final boolean updated = updateDefinitionReference(defRef,
        typeArgumentValues, context);

    if (updated) {
      // unset resolved definition decoration to force re-evaluation of
      // definition reference.
      unsetResolvedDefinition(defRef);
    }

    final Definition d = definitionReferenceResolverItf.resolve(defRef,
        templateInstance, context);

    return d;
  }

  protected boolean updateDefinitionReference(final DefinitionReference defRef,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {
    boolean updated = false;

    final TypeArgument[] typeArguments = (defRef instanceof TypeArgumentContainer)
        ? ((TypeArgumentContainer) defRef).getTypeArguments()
        : null;

    if (typeArguments != null) {
      // update definition reference to replace type parameter references by
      // there actual values and resolve the definition reference again.
      for (final TypeArgument typeArgument : typeArguments) {

        if (typeArgument.getTypeParameterReference() != null) {
          // the type argument is a reference to a formal type
          // parameter.
          final String ref = typeArgument.getTypeParameterReference();
          final Object value = typeArgumentValues.get(ref);
          assert value != null;
          if (value instanceof TypeArgument) {
            final DefinitionReference definitionReference = ((TypeArgument) value)
                .getDefinitionReference();

            typeArgument.setTypeParameterReference(null);
            typeArgument
                .setDefinitionReference(cloneGraph(definitionReference));

            updated = true;
            // unset resolved definition decoration to force re-evaluation of
            // definition reference.
            unsetResolvedDefinition(defRef);
          } else {
            // do we need to do something ?
          }

        } else {
          final DefinitionReference typeArgumentDefRef = typeArgument
              .getDefinitionReference();
          if (typeArgumentDefRef != null) {
            final boolean result = updateDefinitionReference(
                typeArgumentDefRef, typeArgumentValues, context);
            if (result) updated = true;
          }
        }
      }
    }

    return updated;
  }
}
