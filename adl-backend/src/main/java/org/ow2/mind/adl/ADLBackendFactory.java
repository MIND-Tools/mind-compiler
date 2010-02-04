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
 * Authors: Matthieu Leclercq, Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.adl;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.adl.factory.FactoryGraphCompiler;
import org.ow2.mind.adl.generic.GenericDefinitionNameSourceGenerator;
import org.ow2.mind.adl.idl.IDLDefinitionSourceGenerator;
import org.ow2.mind.adl.implementation.BasicImplementationLocator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.interfaces.CollectionInterfaceDefinitionSourceGenerator;
import org.ow2.mind.adl.membrane.MembraneSourceGenerator;
import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.gcc.GccCompilerWrapper;
import org.ow2.mind.idl.IDLBackendFactory;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.idl.st.IDLLoaderASTTransformer;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.preproc.BasicMPPWrapper;
import org.ow2.mind.preproc.MPPWrapper;
import org.ow2.mind.st.BasicASTTransformer;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateASTTransformer;

public final class ADLBackendFactory {
  private ADLBackendFactory() {
  }

  public static final DefinitionSourceGenerator newDefinitionSourceGenerator() {
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader();
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();

    final BasicASTTransformer astTransformer = new BasicASTTransformer();
    astTransformer.nodeFactoryItf = new STNodeFactoryImpl();

    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();
    final IDLVisitor idlCompiler = IDLBackendFactory.newIDLCompiler(idlLoader,
        inputResourceLocator, outputFileLocator, astTransformer, stcLoader);

    return newDefinitionSourceGenerator(inputResourceLocator,
        outputFileLocator, idlLoader, idlCompiler, astTransformer, stcLoader);
  }

  public static final DefinitionSourceGenerator newDefinitionSourceGenerator(
      final InputResourceLocator inputResourceLocator,
      final OutputFileLocator outputFileLocator, final IDLLoader idlLoader,
      final IDLVisitor idlCompiler,
      final StringTemplateASTTransformer astTransformer,
      final StringTemplateGroupLoader stcLoader) {

    DefinitionSourceGenerator definitionSourceGenerator;
    final CollectionInterfaceDefinitionSourceGenerator cidsg = new CollectionInterfaceDefinitionSourceGenerator();
    final DefinitionSourceGeneratorDispatcher dsgd = new DefinitionSourceGeneratorDispatcher();
    final DefinitionHeaderSourceGenerator dhsg = new DefinitionHeaderSourceGenerator();
    final DefinitionIncSourceGenerator disg = new DefinitionIncSourceGenerator();
    final ImplementationHeaderSourceGenerator ihsg = new ImplementationHeaderSourceGenerator();
    final DefinitionMacroSourceGenerator dmsg = new DefinitionMacroSourceGenerator();
    final MembraneSourceGenerator msg = new MembraneSourceGenerator();
    final IDLDefinitionSourceGenerator idsg = new IDLDefinitionSourceGenerator();
    final GenericDefinitionNameSourceGenerator gdnsg = new GenericDefinitionNameSourceGenerator();

    definitionSourceGenerator = cidsg;
    cidsg.clientSourceGeneratorItf = dsgd;
    dsgd.visitorsItf.put("header", dhsg);
    dsgd.visitorsItf.put("include", disg);
    dsgd.visitorsItf.put("impl", ihsg);
    dsgd.visitorsItf.put("macro", dmsg);
    dsgd.visitorsItf.put("membrane", msg);
    dsgd.visitorsItf.put("idl", idsg);
    dsgd.visitorsItf.put("generic-names", gdnsg);

    dhsg.inputResourceLocatorItf = inputResourceLocator;
    disg.inputResourceLocatorItf = inputResourceLocator;
    ihsg.inputResourceLocatorItf = inputResourceLocator;
    dmsg.inputResourceLocatorItf = inputResourceLocator;
    msg.inputResourceLocatorItf = inputResourceLocator;

    dhsg.outputFileLocatorItf = outputFileLocator;
    disg.outputFileLocatorItf = outputFileLocator;
    ihsg.outputFileLocatorItf = outputFileLocator;
    dmsg.outputFileLocatorItf = outputFileLocator;
    msg.outputFileLocatorItf = outputFileLocator;
    gdnsg.outputFileLocatorItf = outputFileLocator;

    final IDLLoaderASTTransformer ilat = new IDLLoaderASTTransformer();
    ilat.clientIDLLoaderItf = idlLoader;
    ilat.astTransformerItf = astTransformer;

    idsg.idlLoaderItf = ilat;
    idsg.idlCompilerItf = idlCompiler;

    dhsg.templateGroupLoaderItf = stcLoader;
    disg.templateGroupLoaderItf = stcLoader;
    dmsg.templateGroupLoaderItf = stcLoader;
    msg.templateGroupLoaderItf = stcLoader;

    return definitionSourceGenerator;
  }

  public static DefinitionCompiler newDefinitionCompiler() {
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader();
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();
    final ImplementationLocator implementationLocator = new BasicImplementationLocator();

    final BasicASTTransformer astTransformer = new BasicASTTransformer();
    astTransformer.nodeFactoryItf = new STNodeFactoryImpl();

    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    final IDLVisitor idlCompiler = IDLBackendFactory.newIDLCompiler(idlLoader,
        inputResourceLocator, outputFileLocator, astTransformer, stcLoader);
    final DefinitionSourceGenerator definitionSourceGenerator = newDefinitionSourceGenerator(
        inputResourceLocator, outputFileLocator, idlLoader, idlCompiler,
        astTransformer, stcLoader);
    final CompilerWrapper compilerWrapper = new GccCompilerWrapper();
    final MPPWrapper mppWrapper = new BasicMPPWrapper();
    return newDefinitionCompiler(definitionSourceGenerator,
        implementationLocator, outputFileLocator, compilerWrapper, mppWrapper);
  }

  public static DefinitionCompiler newDefinitionCompiler(
      final DefinitionSourceGenerator definitionSourceGenerator,
      final ImplementationLocator implementationLocator,
      final OutputFileLocator outputFileLocator,
      final CompilerWrapper compilerWrapper, final MPPWrapper mppWrapper) {

    DefinitionCompiler definitionCompiler;
    final BasicDefinitionCompiler bdc = new BasicDefinitionCompiler();

    definitionCompiler = bdc;
    bdc.definitionSourceGeneratorItf = definitionSourceGenerator;
    bdc.implementationLocatorItf = implementationLocator;
    bdc.outputFileLocatorItf = outputFileLocator;
    bdc.compilerWrapperItf = compilerWrapper;
    bdc.mppWrapperItf = mppWrapper;

    return definitionCompiler;
  }

  public static GraphCompiler newGraphCompiler(
      final InputResourceLocator inputResourceLocator,
      final ImplementationLocator implementationLocator,
      final OutputFileLocator outputFileLocator,
      final CompilerWrapper compilerWrapper, final MPPWrapper mppWrapper,
      final DefinitionCompiler definitionCompiler,
      final StringTemplateGroupLoader stcLoader) {

    // Instance source generator
    InstanceSourceGenerator instanceSourceGenerator;
    final BasicInstanceSourceGenerator bisg = new BasicInstanceSourceGenerator();

    instanceSourceGenerator = bisg;
    bisg.inputResourceLocatorItf = inputResourceLocator;
    bisg.outputFileLocatorItf = outputFileLocator;

    bisg.templateGroupLoaderItf = stcLoader;

    // Instance compiler
    InstanceCompiler instanceCompiler;
    final BasicInstanceCompiler bic = new BasicInstanceCompiler();

    instanceCompiler = bic;
    bic.instanceSourceGeneratorItf = instanceSourceGenerator;

    bic.inputResourceLocatorItf = inputResourceLocator;
    bic.compilerWrapperItf = compilerWrapper;
    bic.mppWrapperItf = mppWrapper;
    bic.outputFileLocatorItf = outputFileLocator;

    // graph compiler
    final BasicGraphCompiler bgc = new BasicGraphCompiler();
    final FactoryGraphCompiler fgc = new FactoryGraphCompiler();
    final BasicGraphLinker bgl = new BasicGraphLinker();
    final GraphCompiler graphCompiler = bgl;
    bgl.clientCompilerItf = fgc;
    fgc.clientCompilerItf = bgc;

    fgc.definitionCompilerItf = definitionCompiler;

    bgl.compilerWrapperItf = compilerWrapper;
    bgl.outputFileLocatorItf = outputFileLocator;
    bgl.implementationLocatorItf = implementationLocator;
    bgc.definitionCompilerItf = definitionCompiler;
    bgc.instanceCompilerItf = instanceCompiler;

    return graphCompiler;
  }

  public static GraphCompiler newGraphCompiler() {
    final IDLLoader idlLoader = IDLLoaderChainFactory.newLoader();
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final BasicOutputFileLocator outputFileLocator = new BasicOutputFileLocator();
    final ImplementationLocator implementationLocator = new BasicImplementationLocator();

    final BasicASTTransformer astTransformer = new BasicASTTransformer();
    astTransformer.nodeFactoryItf = new STNodeFactoryImpl();

    final StringTemplateGroupLoader stcLoader = STLoaderFactory.newSTLoader();

    final IDLVisitor idlCompiler = IDLBackendFactory.newIDLCompiler(idlLoader,
        inputResourceLocator, outputFileLocator, astTransformer, stcLoader);

    final DefinitionSourceGenerator definitionSourceGenerator = newDefinitionSourceGenerator(
        inputResourceLocator, outputFileLocator, idlLoader, idlCompiler,
        astTransformer, stcLoader);
    final CompilerWrapper compilerWrapper = new GccCompilerWrapper();
    final MPPWrapper mppWrapper = new BasicMPPWrapper();

    final DefinitionCompiler definitionCompiler = newDefinitionCompiler(
        definitionSourceGenerator, implementationLocator, outputFileLocator,
        compilerWrapper, mppWrapper);

    return newGraphCompiler(inputResourceLocator, implementationLocator,
        outputFileLocator, compilerWrapper, mppWrapper, definitionCompiler,
        stcLoader);
  }

  public static CompilationCommandExecutor newCompilationCommandExecutor() {
    return new BasicCompilationCommandExecutor();
  }
}
