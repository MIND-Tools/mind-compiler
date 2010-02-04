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

package org.ow2.mind.adl.jtb;

import static org.testng.Assert.assertNotNull;

import java.io.InputStream;

import org.ow2.mind.adl.jtb.Parser;
import org.ow2.mind.adl.jtb.syntaxtree.ADLFile;
import org.testng.annotations.Test;

public class TestJTBParser {

  protected Parser getParser(final String fileName) throws Exception {
    final ClassLoader loader = getClass().getClassLoader();
    final InputStream is = loader.getResourceAsStream(fileName);
    assertNotNull(is, "Can't find input file \"" + fileName + "\"");

    return new Parser(is);
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Parser parser = getParser("Test1.adl");
    final ADLFile content = parser.ADLFile();
    assertNotNull(content);
  }

  @Test(groups = {"functional"})
  public void test2() throws Exception {
    final Parser parser = getParser("Test2.adl");
    final ADLFile content = parser.ADLFile();
    assertNotNull(content);
  }
}
