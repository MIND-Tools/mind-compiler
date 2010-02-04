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
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.st.IDLLoaderASTTransformer;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.st.BasicASTTransformer;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateASTTransformer;

public final class IDLBackendFactory {
  private IDLBackendFactory() {
  }

  public static IDLVisitor newIDLCompiler() {
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader();
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();

    final BasicASTTransformer astTransformer = new BasicASTTransformer();
    astTransformer.nodeFactoryItf = new STNodeFactoryImpl();

    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    return newIDLCompiler(idlLoader, inputResourceLocator, outputFileLocator,
        astTransformer, stcLoader);
  }

  public static IDLVisitor newIDLCompiler(final IDLLoader idlLoader,
      final InputResourceLocator inputResourceLocator,
      final OutputFileLocator outputFileLocator,
      final StringTemplateASTTransformer astTransformer,
      final StringTemplateGroupLoader stcLoader) {
    IDLVisitor idlCompiler;
    final IDLHeaderCompiler ihc = new IDLHeaderCompiler();
    final IncludeCompiler ic = new IncludeCompiler();

    idlCompiler = ic;
    ic.clientVisitorItf = ihc;

    ihc.templateGroupLoaderItf = stcLoader;

    final IDLLoaderASTTransformer ilat = new IDLLoaderASTTransformer();
    ilat.clientIDLLoaderItf = idlLoader;
    ilat.astTransformerItf = astTransformer;

    ic.idlLoaderItf = ilat;
    ihc.inputResourceLocatorItf = inputResourceLocator;
    ihc.outputFileLocatorItf = outputFileLocator;

    return idlCompiler;
  }
}
