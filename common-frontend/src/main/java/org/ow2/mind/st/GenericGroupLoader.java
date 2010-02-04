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

import java.util.ArrayList;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.StringTemplateErrorListener;

public class GenericGroupLoader extends CommonGroupLoader {

  public GenericGroupLoader(final String arg0,
      final StringTemplateErrorListener arg1) {
    super(arg0, arg1);
  }

  public GenericGroupLoader(final StringTemplateErrorListener arg1) {
    super(arg1);
  }

  public void addDir(final String dir) {
    if (dirs == null) {
      dirs = new ArrayList<String>();
    }
    if (!dirs.contains(dir)) {
      dirs.add(dir);
    }
  }

  public void removeDir(final String dir) {
    dirs.remove(dir);
  }

  public String[] getDirs() {
    final String[] array = new String[dirs.size()];
    for (int i = 0; i < dirs.size(); i++) {
      array[i] = (String) dirs.get(i);
    }
    return array;
  }

}
