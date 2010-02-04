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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.st;

import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplateGroup;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateComponentLoader;
import org.ow2.mind.st.templates.parser.StringTemplateLoader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleTest {

  final StringTemplateComponentLoader stcLoader      = new StringTemplateComponentLoader();
  final StringTemplateLoader          templateLoader = new StringTemplateLoader();
  final XMLNodeFactory                nodeFactory    = new XMLNodeFactoryImpl();

  Map<Object, Object>                 context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    context = new HashMap<Object, Object>();
    templateLoader.nodeFactoryItf = nodeFactory;
    stcLoader.loaderItf = templateLoader;
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final StringTemplateGroup group = stcLoader.loadGroup("simple.Group1");
    System.out.println(group.getInstanceOf("temp1").toString());
  }
}
