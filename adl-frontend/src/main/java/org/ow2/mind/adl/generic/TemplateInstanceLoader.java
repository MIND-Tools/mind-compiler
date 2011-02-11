
package org.ow2.mind.adl.generic;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.ow2.mind.adl.AbstractDelegatingLoader;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.DefinitionName.DefinitionNameArgument;
import org.ow2.mind.adl.generic.ast.GenericASTHelper;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

import com.google.inject.Inject;

public class TemplateInstanceLoader extends AbstractDelegatingLoader {

  @Inject
  protected DefinitionReferenceResolver definitionReferenceResolverItf;

  @Inject
  protected NodeFactory                 nodeFactoryItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Loader interface
  // ---------------------------------------------------------------------------

  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    if (name.contains("<")) {
      final DefinitionName defName = DefinitionName.fromString(name);
      final DefinitionReference defRef = toDefinitionReference(defName);
      return definitionReferenceResolverItf.resolve(defRef, null, context);
    } else {
      return clientLoader.load(name, context);
    }
  }

  protected DefinitionReference toDefinitionReference(
      final DefinitionName definitionName) {
    final DefinitionReference defRef = ASTHelper.newDefinitionReference(
        nodeFactoryItf, definitionName.getName());

    for (final DefinitionNameArgument arg : definitionName.getTypeArguments()) {
      final TypeArgument typeArgument = GenericASTHelper
          .newTypeArgument(nodeFactoryItf);

      typeArgument
          .setDefinitionReference(toDefinitionReference(arg.getValue()));
      ((TypeArgumentContainer) defRef).addTypeArgument(typeArgument);
    }

    return defRef;
  }
}
