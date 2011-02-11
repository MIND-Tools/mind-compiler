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

package org.ow2.mind.adl.parser;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.AbstractADLFrontendModule;
import org.ow2.mind.adl.jtb.Parser;
import org.ow2.mind.adl.jtb.syntaxtree.ADLFile;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestJTBProcessor {

  protected static final String DTD = "classpath://org/ow2/mind/adl/mind_v1.dtd";
  XMLNodeFactory                nodeFactory;
  JTBProcessor                  processor;
  Injector                      injector;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {

    injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new AbstractADLFrontendModule() {
        });

    nodeFactory = injector.getInstance(XMLNodeFactory.class);
  }

  protected Parser getParser(final String fileName) throws Exception {
    final ClassLoader loader = getClass().getClassLoader();
    final InputStream is = loader.getResourceAsStream(fileName);
    assertNotNull(is, "Can't find input file \"" + fileName + "\"");
    processor = injector.getInstance(JTBProcessor.class);
    processor.setFilename(fileName);

    return new Parser(is);
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Parser parser = getParser("Test1.adl");
    final ADLFile content = parser.ADLFile();
    final Node node = processor.visit(content, null);
    assertTrue(node instanceof Definition);
  }
}
