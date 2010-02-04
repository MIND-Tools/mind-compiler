/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.adl.idl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.annotation.predefined.UseIDL;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class UseIDLAnnotationProcessor
    extends
      AbstractADLLoaderAnnotationProcessor {

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLoaderAnnotationProcessor interface
  // ---------------------------------------------------------------------------

  public Definition processAnnotation(final Annotation annotation,
      final Node node, final Definition definition, final ADLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof UseIDL;
    final UseIDL useIDL = (UseIDL) annotation;
    final String[] idlNames = useIDL.value;
    for (final String idlName : idlNames) {
      final TypeInterface tItf = ASTHelper.newInterfaceNode(nodeFactoryItf);
      tItf.setSignature(idlName);
      try {
        final InterfaceDefinition itfDef = itfSignatureResolverItf.resolve(
            tItf, definition, context);
        InterfaceDefinitionDecorationHelper.addUsedInterfaceDefinition(
            definition, itfDef);
      } catch (final ADLException e) {
        ChainedErrorLocator.chainLocator(e, node);
        throw e;
      }
    }

    return null;
  }

}
