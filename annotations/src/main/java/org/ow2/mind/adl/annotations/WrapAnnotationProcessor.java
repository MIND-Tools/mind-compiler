/**
 * Copyright (C) 2010 France Telecom
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
 * Authors: Matthieu ANNE
 * Contributors:
 */

package org.ow2.mind.adl.annotations;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeInterfaceUtil;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.annotation.AnnotationErrors;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.annotations.VarArgsDual;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

/**
 * @author Matthieu ANNE
 */
public class WrapAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  protected static final String IDL2CPLWRAPPER_TEMPLATE_NAME = "st.interfaceWrapping.IDL2CPLWRAPPER";

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof Wrap;

    if (ASTHelper.isPrimitive(definition)) {
      final Interface itf = (Interface) node;
      if (TypeInterfaceUtil.isServer(itf)) {
        final InterfaceDefinition itfDef = itfSignatureResolverItf.resolve(
            (TypeInterface) itf, definition, context);

        final Map<String, String> dualMeths = new HashMap<String, String>();

        for (final Method meth : itfDef.getMethods()) {
          final VarArgsDual methAnnotation = AnnotationHelper.getAnnotation(
              meth, VarArgsDual.class);
          if (methAnnotation != null) {
            dualMeths.put(meth.getName(), (methAnnotation).value);
          }
        }

        // TODO might need a #line in generated file to find error in
        // source file
        // NodeErrorLocator sourceInfo = new NodeErrorLocator(node);

        final StringTemplate st = getTemplate(IDL2CPLWRAPPER_TEMPLATE_NAME,
            "cplFile");
        st.setAttribute("idl", itfDef);
        st.setAttribute("itfName", itf.getName());
        st.setAttribute("dualMeths", dualMeths);
        // st.setAttribute("sourceInfo", sourceInfo);

        final Source src = ASTHelper.newSource(nodeFactoryItf);
        src.setCCode(st.toString());
        ((ImplementationContainer) definition).addSource(src);

        // remove annotation from node to avoid it to be reprocessed on a
        // definition that extends this one.
        AnnotationHelper.removeAnnotation(node, annotation);
      } else {
        errorManagerItf.logError(AnnotationErrors.INVALID_ANNOTATION, node,
            "@Wrap. Client's interfaces cannot be wrapped.");
        return null;
      }
    } else {
      errorManagerItf.logError(
          AnnotationErrors.INVALID_ANNOTATION,
          node,
          "@Wrap applied to interface " + ((Interface) node).getName()
              + ".\n Composite's interfaces cannot be wrapped: "
              + definition.astGetSource());
    }
    return null;
  }
}
