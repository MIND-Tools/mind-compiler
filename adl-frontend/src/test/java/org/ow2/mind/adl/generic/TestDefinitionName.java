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

package org.ow2.mind.adl.generic;

import static org.testng.Assert.assertEquals;

import org.ow2.mind.adl.generic.DefinitionName;
import org.ow2.mind.adl.generic.DefinitionName.DefinitionNameArgument;
import org.testng.annotations.Test;

public class TestDefinitionName {

  @Test(groups = {"functional"})
  public void test_fromString_1() {
    final DefinitionName name = DefinitionName.fromString("n1");
    assertEquals("n1", name.getName());
    assertEquals(0, name.getTypeArguments().length);
  }

  @Test(groups = {"functional"})
  public void test_fromString_2() {
    final DefinitionName name = DefinitionName.fromString("pkg1.n1");
    assertEquals("pkg1.n1", name.getName());
    assertEquals(0, name.getTypeArguments().length);
  }

  @Test(groups = {"functional"})
  public void test_fromString_3() {
    final DefinitionName name = DefinitionName
        .fromString("pkg1.n1<pkg1.pkg2.v1,pkg1.pkg2.v2>");
    assertEquals("pkg1.n1", name.getName());
    final DefinitionNameArgument[] templateValues = name.getTypeArguments();
    assertEquals(2, templateValues.length);

    assertEquals("pkg1.pkg2.v1", templateValues[0].getValue().getName());
    assertEquals(0, templateValues[0].getValue().getTypeArguments().length);

    assertEquals("pkg1.pkg2.v2", templateValues[1].getValue().getName());
    assertEquals(0, templateValues[1].getValue().getTypeArguments().length);
  }

  @Test(groups = {"functional"})
  public void test_fromString_4() {
    final DefinitionName name = DefinitionName
        .fromString("pkg1.n1<pkg1.pkg2.v1<pkg1.pkg2.v2,pkg1.pkg2.v2>,pkg1.pkg2.v2>");
    assertEquals("pkg1.n1", name.getName());
    final DefinitionNameArgument[] templateValues = name.getTypeArguments();
    assertEquals(2, templateValues.length);

    final DefinitionName tmplValue0 = templateValues[0].getValue();
    assertEquals("pkg1.pkg2.v1", tmplValue0.getName());
    assertEquals(2, tmplValue0.getTypeArguments().length);

    assertEquals("pkg1.pkg2.v2", tmplValue0.getTypeArguments()[0].getValue()
        .getName());
    assertEquals(0, tmplValue0.getTypeArguments()[0].getValue()
        .getTypeArguments().length);

    assertEquals("pkg1.pkg2.v2", tmplValue0.getTypeArguments()[1].getValue()
        .getName());
    assertEquals(0, tmplValue0.getTypeArguments()[1].getValue()
        .getTypeArguments().length);

    assertEquals("pkg1.pkg2.v2", templateValues[1].getValue().getName());
    assertEquals(0, templateValues[1].getValue().getTypeArguments().length);
  }

}
