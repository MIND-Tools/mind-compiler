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

package org.ow2.mind.adl.graph;

import static org.ow2.mind.BCImplChecker.checkBCImplementation;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.GraphChecker;
import org.ow2.mind.adl.graph.AttributeInstantiator;
import org.ow2.mind.adl.graph.BasicInstantiator;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.graph.Instantiator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGraph {

  Loader              loader;

  Instantiator        instantiator;

  Map<Object, Object> context;

  ASTChecker          astChecker;
  GraphChecker        graphChecker;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    loader = Factory.newLoader();

    final BasicInstantiator bi = new BasicInstantiator();
    final AttributeInstantiator ai = new AttributeInstantiator();

    ai.clientInstantiatorItf = bi;
    bi.loaderItf = loader;
    instantiator = ai;

    context = new HashMap<Object, Object>();

    astChecker = new ASTChecker();
    graphChecker = new GraphChecker(astChecker);
  }

  @Test(groups = {"functional", "checkin"})
  public void testBasicInstantiatorBC() throws Exception {
    checkBCImplementation(new BasicInstantiator());
  }

  @Test(groups = {"functional", "checkin"})
  public void testAttributeInstantiatorBC() throws Exception {
    checkBCImplementation(new AttributeInstantiator());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition d = loader.load("pkg1.parameterGeneric.Composite1",
        context);

    final ComponentGraph graph = instantiator.instantiate(d, context);
    graphChecker.assertGraph(graph).containsSubComponents("c1", "c2", "c3")
        .whereFirst()/* c1 */.containsAttributeValue("attr1", 10)

        .andNext()/* c2 */.containsAttributeValue("attr1", 12)

        .andNext()/* c3 */.containsSubComponents("c1")

        .whereFirst()/* c3/c1 */.containsSubComponents("c1", "c2")

        .whereFirst()/* c3/c1/c1 */.containsSubComponents("subComp1")

        .whereFirst()
        /* c3/c1/c1/subComp1 */.containsAttributeValue("attr1", 20);

  }
}
