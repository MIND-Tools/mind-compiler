
package org.ow2.mind.adl.generic;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.AbstractLoader;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.DefinitionName.DefinitionNameArgument;
import org.ow2.mind.adl.generic.ast.GenericASTHelper;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.generic.ast.TypeArgumentContainer;

public class TemplateInstanceLoader extends AbstractLoader {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String         RECURSIVE_LOADER_ITF_NAME = "recursive-loader";

  public DefinitionReferenceResolver definitionReferenceResolverItf;

  public NodeFactory                 nodeFactoryItf;

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
    final DefinitionReference defRef = newDefinitionReference();

    defRef.setName(definitionName.getName());

    for (final DefinitionNameArgument arg : definitionName.getTypeArguments()) {
      final TypeArgument typeArgument = GenericASTHelper
          .newTypeArgument(nodeFactoryItf);

      typeArgument
          .setDefinitionReference(toDefinitionReference(arg.getValue()));
      ((TypeArgumentContainer) defRef).addTypeArgument(typeArgument);
    }

    return defRef;
  }

  protected DefinitionReference newDefinitionReference() throws CompilerError {
    return ASTHelper.newNode(nodeFactoryItf, "definitionReference",
        DefinitionReference.class, TypeArgumentContainer.class);
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else {
      super.bindFc(itfName, value);
    }
  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), DefinitionReferenceResolver.ITF_NAME,
        NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
