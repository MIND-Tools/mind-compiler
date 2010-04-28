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

import java.io.Reader;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroupInterface;

public class StringTemplateGroup
    extends
      org.antlr.stringtemplate.StringTemplateGroup {

  public StringTemplateGroup(final Reader arg0, final Class arg1,
      final StringTemplateErrorListener arg2, final StringTemplateGroup arg3) {
    super(arg0, arg1, arg2, arg3);
  }

  public StringTemplateGroup(final Reader arg0, final Class arg1,
      final StringTemplateErrorListener arg2) {
    super(arg0, arg1, arg2);
  }

  public StringTemplateGroup(final Reader arg0, final Class arg1) {
    super(arg0, arg1);
  }

  public StringTemplateGroup(final Reader arg0,
      final StringTemplateErrorListener arg1) {
    super(arg0, arg1);
  }

  public StringTemplateGroup(final Reader arg0) {
    super(arg0);
  }

  public StringTemplateGroup(final String arg0, final Class arg1) {
    super(arg0, arg1);
  }

  public StringTemplateGroup(final String arg0, final String arg1,
      final Class arg2) {
    super(arg0, arg1, arg2);
  }

  public StringTemplateGroup(final String arg0, final String arg1) {
    super(arg0, arg1);
  }

  public StringTemplateGroup(final String arg0) {
    super(arg0);
  }

  public boolean implementsInterface(final String name) {
    if (interfaces == null) {
      return false;
    }
    for (final Object groupInterface : interfaces) {
      if (((StringTemplateGroupInterface) groupInterface).getName()
          .equals(name)) {
        return true;
      }
    }
    return false;
  }

  public Set<String> getMapsKeySet() {
    return maps.keySet();
  }

}
