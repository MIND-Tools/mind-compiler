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

package org.ow2.mind.idl.parser;

import java.io.InputStream;

import junit.framework.TestCase;

import org.ow2.mind.idl.jtb.Parser;
import org.ow2.mind.idl.jtb.syntaxtree.IDTFile;
import org.ow2.mind.idl.jtb.syntaxtree.ITFFile;

public class TestParser extends TestCase {

  protected Parser getParser(final String fileName) throws Exception {
    final ClassLoader loader = getClass().getClassLoader();
    final InputStream is = loader.getResourceAsStream(fileName);
    assertNotNull("Can't find input file \"" + fileName + "\"", is);

    return new Parser(is);
  }

  public void test1() throws Exception {
    final Parser p = getParser("test1.itf");
    final ITFFile content = p.ITFFile();
    assertNotNull(content);
  }

  public void test2() throws Exception {
    final Parser p = getParser("test2.idt");
    final IDTFile content = p.IDTFile();
    assertNotNull(content);
  }

}
