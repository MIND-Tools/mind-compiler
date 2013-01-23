/**
 * Copyright (C) 2013 Schneider-Electric
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
 * Authors: Stephane Seyvoz
 * Contributors: 
 */

package org.ow2.mind.idl.annotation;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.idl.annotation.predefined.ImportIDL;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

public class ImportIDLAnnotationProcessor
    extends
      AbstractIDLLoaderAnnotationProcessor {

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLoaderAnnotationProcessor interface
  // ---------------------------------------------------------------------------

  public IDL processAnnotation(final Annotation annotation, final Node node,
      final IDL idl, final IDLLoaderPhase phase,
      final Map<Object, Object> context) throws ADLException {
    assert annotation instanceof ImportIDL;
    final ImportIDL useIDL = (ImportIDL) annotation;
    final String[] idlNames = useIDL.value;
    for (final String idlName : idlNames) {

      final IDL currentArgIDL = idlLoaderItf.load(idlName, context);

      // should be everytime ?
      if (currentArgIDL instanceof InterfaceDefinition)
        IDLASTHelper.addReferencedInterface(idl,
            (InterfaceDefinition) currentArgIDL);
    }

    return null;
  }
}
