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

package org.ow2.mind.preproc;

import org.testng.annotations.Test;

public class TestMPP extends AbstractTestMPP {

  @Test(groups = {"functional", "checkin"})
  public void testSimpleSingleton() throws Exception {
    compileSingleton("simple", "simple");
  }

  @Test(groups = {"functional", "checkin"})
  public void testSimpleMulti() throws Exception {
    compileMulti("simple", "simple");
  }

  @Test(groups = {"functional"})
  public void testAttributeSingleton() throws Exception {
    compileSingleton("attribute", "attribute");
  }

  @Test(groups = {"functional"})
  public void testAttributeMulti() throws Exception {
    compileMulti("attribute", "attribute");
  }

// @Test(groups = {"functional"})
// public void testinitSingleton() throws Exception {
// compileSingleton("init", "init");
// }
//
// @Test(groups = {"functional"})
// public void testinitMulti() throws Exception {
// compileMulti("init", "init");
// }

  @Test(groups = {"functional"})
  public void testmultidecl2Singleton() throws Exception {
    compileSingleton("multidecl-2", "multidecl-2");
  }

  @Test(groups = {"functional"})
  public void testmultidecl2Multi() throws Exception {
    compileMulti("multidecl-2", "multidecl-2");
  }

  @Test(groups = {"functional"})
  public void testvoidSingleton() throws Exception {
    compileSingleton("void", "void");
  }

  @Test(groups = {"functional"})
  public void testvoidMulti() throws Exception {
    compileMulti("void", "void");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtrSingleton() throws Exception {
    compileSingleton("functionPtr", "functionPtr");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtrMulti() throws Exception {
    compileMulti("functionPtr", "functionPtr");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr2Singleton() throws Exception {
    compileSingleton("functionPtr2", "functionPtr2");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr2Multi() throws Exception {
    compileMulti("functionPtr2", "functionPtr2");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr3Singleton() throws Exception {
    compileSingleton("functionPtr3", "functionPtr3");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr3Multi() throws Exception {
    compileMulti("functionPtr3", "functionPtr3");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr4Singleton() throws Exception {
    compileSingleton("functionPtr4", "functionPtr4");
  }

  @Test(groups = {"functional"})
  public void testfunctionPtr4Multi() throws Exception {
    compileMulti("functionPtr4", "functionPtr4");
  }

  @Test(groups = {"functional"})
  public void testextraParentSingleton() throws Exception {
    compileSingleton("extraParent", "extraparent");
  }

  @Test(groups = {"functional"})
  public void testextraParentMulti() throws Exception {
    compileMulti("extraParent", "extraparent");
  }

  @Test(groups = {"functional"})
  public void testmissingCAllSingleton() throws Exception {
    compileSingleton("missingCALL", "missingCALL");
  }

  @Test(groups = {"functional"})
  public void testmissingCALLMulti() throws Exception {
    compileMulti("missingCALL", "missingCALL");
  }

  @Test(groups = {"functional"})
  public void testmissingCALL2Singleton() throws Exception {
    compileSingleton("missingCALL2", "missingCALL2");
  }

  @Test(groups = {"functional"})
  public void testmissingCALL2Multi() throws Exception {
    compileMulti("missingCALL2", "missingCALL2");
  }

  @Test(groups = {"functional"})
  public void testaccessToThisSingleton() throws Exception {
    compileSingleton("accessToThis", "accesToThis");
  }

  @Test(groups = {"functional"})
  public void testaccessToThisMulti() throws Exception {
    compileMulti("accessToThis", "accesToThis");
  }

  @Test(groups = {"functional"})
  public void testprivateArraySingleton() throws Exception {
    compileSingleton("privateArray", "privateArray");
  }

  @Test(groups = {"functional"})
  public void testprivatePtrSingleton() throws Exception {
    compileSingleton("privatePtr", "privatePtr");
  }

  @Test(groups = {"functional"})
  public void testexternSingleton() throws Exception {
    compileSingleton("extern", "extern");
  }

  @Test(groups = {"functional"})
  public void testkrSingleton() throws Exception {
    compileSingleton("kr", "kr");
  }

  @Test(groups = {"unsupported"})
  public void testmultideclSingleton() throws Exception {
    compileSingleton("multidecl", "multidecl");
  }

  @Test(groups = {"functional"})
  public void testsingletonSingleton() throws Exception {
    compileSingleton("singleton", "singleton");
  }

  @Test(groups = {"functional"})
  public void testmaskingSingleton() throws Exception {
    compileSingleton("masking", "masking");
  }

  @Test(groups = {"functional"})
  public void testconstructorSingleton() throws Exception {
    compileSingleton("constructor", "constructor");
  }

  @Test(groups = {"functional"})
  public void testconstructorMulti() throws Exception {
    compileMulti("constructor", "constructor");
  }

}
