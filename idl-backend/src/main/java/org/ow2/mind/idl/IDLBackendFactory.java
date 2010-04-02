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

package org.ow2.mind.idl;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.st.STLoaderFactory;

public final class IDLBackendFactory {
  private IDLBackendFactory() {
  }

  public static IDLVisitor newIDLCompiler(final IDLLoader idlLoader) {
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();

    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    return newIDLCompiler(idlLoader, inputResourceLocator, outputFileLocator,
        stcLoader);
  }

  public static IDLVisitor newIDLCompiler(final IDLLoader idlLoader,
      final InputResourceLocator inputResourceLocator,
      final OutputFileLocator outputFileLocator,
      final StringTemplateGroupLoader stcLoader) {
    IDLVisitor idlCompiler;
    final IDLVisitorDispatcher ivd = new IDLVisitorDispatcher();
    final IDLHeaderCompiler ihc = new IDLHeaderCompiler();
    final IncludeCompiler ic = new IncludeCompiler();
    final BinaryIDLWriter biw = new BinaryIDLWriter();

    idlCompiler = ivd;
    ivd.visitorsItf.put("idl2h", ic);
    ivd.visitorsItf.put("bin", biw);

    ic.clientVisitorItf = ihc;
    biw.inputResourceLocatorItf = inputResourceLocator;
    biw.outputFileLocatorItf = outputFileLocator;

    ihc.templateGroupLoaderItf = stcLoader;

    ic.idlLoaderItf = idlLoader;
    ihc.inputResourceLocatorItf = inputResourceLocator;
    ihc.outputFileLocatorItf = outputFileLocator;

    return idlCompiler;
  }
}
