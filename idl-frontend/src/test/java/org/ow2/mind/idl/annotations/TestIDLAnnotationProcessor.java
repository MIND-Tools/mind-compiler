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

package org.ow2.mind.idl.annotations;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.ow2.mind.idl.IDLErrorLoader;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.annotation.AbstractIDLLoaderAnnotationProcessor;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestIDLAnnotationProcessor {

  private IDLLoader loader;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    // loader chains
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();
    final IDLErrorLoader errorLoader = new IDLErrorLoader();
    errorLoader.errorManagerItf = errorManager;
    errorLoader.clientIDLLoaderItf = IDLLoaderChainFactory
        .newLoader(errorManager).loader;
    loader = errorLoader;
    // ensure that phases are empty.
    FooProcessor.phases = new HashSet<IDLLoaderPhase>();
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    loader.load("annotations.test1", new HashMap<Object, Object>());
    assertTrue(FooProcessor.phases.contains(IDLLoaderPhase.AFTER_PARSING));
    assertTrue(FooProcessor.phases.contains(IDLLoaderPhase.AFTER_CHECKING));
  }

  public static class FooProcessor extends AbstractIDLLoaderAnnotationProcessor {

    public static Set<IDLLoaderPhase> phases = new HashSet<IDLLoaderPhase>();

    public IDL processAnnotation(final Annotation annotation, final Node node,
        final IDL idl, final IDLLoaderPhase phase,
        final Map<Object, Object> context) throws ADLException {
      assertNotNull(annotation);
      assertNotNull(node);
      assertNotNull(idl);
      assertNotNull(phase);
      phases.add(phase);

      assertTrue(annotation instanceof FooAnnotation);
      assertTrue(node instanceof InterfaceDefinition || node instanceof Method);
      return null;
    }
  }

}
