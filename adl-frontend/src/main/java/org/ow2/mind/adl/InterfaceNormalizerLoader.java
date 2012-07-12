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

package org.ow2.mind.adl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.idl.IDLCache;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IncludeResolver;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.IDLASTHelper.IncludeDelimiter;
import org.ow2.mind.idl.ast.Include;
import org.ow2.mind.idl.ast.IncludeContainer;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;
import org.ow2.mind.idl.ast.Parameter;
import org.ow2.mind.idl.ast.PrimitiveType.PrimitiveTypeEnum;
import org.ow2.mind.idl.parser.IDLParserContextHelper;

import com.google.inject.Inject;

/**
 * Interface Normalizer. Transforms Flow interfaces (input/output) into
 * functional interfaces (provided/required respectively)
 */
public class InterfaceNormalizerLoader
    extends
      AbstractNormalizerLoader<Interface> {

  @Inject
  protected NodeFactory     nodeFactoryItf;

  @Inject
  protected IDLCache        idlCacheItf;

  @Inject
  protected IDLLoader       idlLoaderItf;

  @Inject
  protected IncludeResolver includeResolverItf;

  @Override
  public Definition load(final String name, final Map<Object, Object> context)
      throws ADLException {
    final Definition d = clientLoader.load(name, context);
    if (d instanceof InterfaceContainer) {
      final InterfaceContainer container = (InterfaceContainer) d;
      boolean containsFlowInterfaces = false;
      for (final Interface itf : container.getInterfaces()) {
        if (ASTHelper.isFlowInterface(itf)) {
          final Interface newItf = convertFlowInterface(d, itf, context);
          container.removeInterface(itf);
          container.addInterface(newItf);
          containsFlowInterfaces = true;
        } else if (containsFlowInterfaces) {
          // If a flow interface has already been converted, remove and re-add
          // interface to preserve interface order.
          container.removeInterface(itf);
          container.addInterface(itf);
        }
      }
    }
    return d;
  }

  protected Interface convertFlowInterface(final Definition d,
      final Interface itf, final Map<Object, Object> context)
      throws ADLException {
    String signature = ((TypeInterface) itf).getSignature();
    IDL idtFile;
    String typeName;
    final int i = signature.indexOf(':');
    if (i > 0) {
      final String idtPath = signature.substring(0, i);

      // create an include node to use the IncludeResolver
      final Include includeNode = IDLASTHelper.newIncludeNode(nodeFactoryItf,
          idtPath, IncludeDelimiter.QUOTE);
      // copy source info for error reporting
      includeNode.astSetSource(itf.astGetSource());
      idtFile = includeResolverItf.resolve(includeNode, null, d.getName(),
          context);
      typeName = signature.substring(i + 1);
      signature = idtFile.getName() + ":" + typeName;
      ((TypeInterface) itf).setSignature(signature);
    } else {
      idtFile = null;
      typeName = signature;
    }

    final String itfDefName = "flow.generated.Push_"
        + Integer.toHexString(signature.hashCode());

    IDL itfDef = idlCacheItf.getInCache(itfDefName, context);
    if (itfDef == null) {
      itfDef = createFlowInterfaceDefinition(itfDefName, idtFile, typeName,
          context);
      IDLParserContextHelper.registerIDL(itfDef, context);
      itfDef = idlLoaderItf.load(itfDefName, context);
    }

    MindInterface newItf;
    if (ASTHelper.isInput(itf)) {
      newItf = ASTHelper.newServerInterfaceNode(nodeFactoryItf, itf.getName(),
          itfDefName);
    } else {
      assert ASTHelper.isOutput(itf);
      newItf = ASTHelper.newClientInterfaceNode(nodeFactoryItf, itf.getName(),
          itfDefName);
    }
    if (TypeInterfaceUtil.isOptional(itf)) {
      newItf.setContingency(TypeInterface.OPTIONAL_CONTINGENCY);
    }
    if (TypeInterfaceUtil.isCollection(itf)) {
      newItf.setCardinality(TypeInterface.COLLECTION_CARDINALITY);
    }
    final int noe = ASTHelper.getNumberOfElement(itf);
    if (noe != -1) {
      newItf.setNumberOfElement(Integer.toString(noe));
    }

    return newItf;
  }

  protected InterfaceDefinition createFlowInterfaceDefinition(
      final String itfDefName, final IDL idtFile, final String typeName,
      final Map<Object, Object> context) {

    // create interface definition
    final InterfaceDefinition itfDef = IDLASTHelper.newInterfaceDefinitionNode(
        nodeFactoryItf, itfDefName);

    // add #include "idt file" if needed
    if (idtFile != null) {
      final Include includeIDT = IDLASTHelper.newIncludeNode(nodeFactoryItf,
          idtFile.getName(), IncludeDelimiter.QUOTE);
      ((IncludeContainer) itfDef).addInclude(includeIDT);
    }

    // create push method
    final Method pushMeth = IDLASTHelper.newMethodNode(nodeFactoryItf, "push");
    itfDef.addMethod(pushMeth);
    pushMeth.setType(IDLASTHelper.newPrimitiveTypeNode(nodeFactoryItf,
        PrimitiveTypeEnum.VOID));

    // create push parameter
    final Parameter pushParam = IDLASTHelper.newParameterNode(nodeFactoryItf,
        "data");
    pushMeth.addParameter(pushParam);

    // set parameter type.
    if (typeName.startsWith("struct ")) {
      pushParam.setType(IDLASTHelper.newStructReferenceNode(nodeFactoryItf,
          typeName.substring("struct ".length())));
    } else if (typeName.startsWith("union ")) {
      pushParam.setType(IDLASTHelper.newUnionReferenceNode(nodeFactoryItf,
          typeName.substring("union ".length())));
    } else if (typeName.startsWith("enum ")) {
      pushParam.setType(IDLASTHelper.newEnumReferenceNode(nodeFactoryItf,
          typeName.substring("enum ".length())));
    } else if (PrimitiveTypeEnum.isPrimitive(typeName)) {
      pushParam.setType(IDLASTHelper.newPrimitiveTypeNode(nodeFactoryItf,
          typeName));
    } else {
      pushParam.setType(IDLASTHelper.newTypeDefReferenceNode(nodeFactoryItf,
          typeName));
    }

    return itfDef;
  }

  // ---------------------------------------------------------------------------
  // Implementation of the abstract methods of AbstractNormalizerLoader
  // ---------------------------------------------------------------------------

  @Override
  protected Interface[] getSubNodes(final Node node) {
    if (node instanceof InterfaceContainer)
      return ((InterfaceContainer) node).getInterfaces();
    return null;
  }

  @Override
  protected Object getId(final Interface node) {
    return node.getName();
  }

  @Override
  protected void handleNameClash(final Interface previousDeclaration,
      final Interface subNode) throws ADLException {
    errorManagerItf.logError(InterfaceErrors.DUPLICATED_INTERFACE_NAME,
        subNode.getName(), new NodeErrorLocator(previousDeclaration));
  }

  @Override
  protected void removeSubNode(final Node node, final Interface subNode) {
    ((InterfaceContainer) node).removeInterface(subNode);
  }
}
