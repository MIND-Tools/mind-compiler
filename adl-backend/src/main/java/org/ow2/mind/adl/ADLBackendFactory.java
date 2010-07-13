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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.VoidVisitor;
import org.ow2.mind.adl.VisitorExtensionHelper.VisitorExtension;
import org.ow2.mind.adl.factory.FactoryGraphCompiler;
import org.ow2.mind.adl.generic.GenericDefinitionNameSourceGenerator;
import org.ow2.mind.adl.idl.IDLDefinitionSourceGenerator;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.adl.interfaces.CollectionInterfaceDefinitionSourceGenerator;
import org.ow2.mind.adl.membrane.MembraneSourceGenerator;
import org.ow2.mind.compilation.BasicCompilationCommandExecutor;
import org.ow2.mind.compilation.CompilationCommandExecutor;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLVisitor;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.preproc.MPPWrapper;
import org.ow2.mind.st.StringTemplateComponentLoader;

public final class ADLBackendFactory {

  private ADLBackendFactory() {
  }

  // ///////////////////////////////////////////////////////////
  // Services that can be used by visitor plugins //
  // //////////////////////////////////////////////////////////
  public static final String INPUT_RESOURCE_LOCATOR_ITF_NAME = InputResourceLocator.ITF_NAME;
  public static final String OUTPUT_FILE_LOCATOR_ITF_NAME    = OutputFileLocator.ITF_NAME;
  public static final String IDL_LOADER_ITF_NAME             = IDLLoader.ITF_NAME;
  public static final String IDL_COMPILER_ITF_NAME           = "idl-compiler";
  public static final String TEMPLATE_GROUP_LOADER_ITF_NAME  = StringTemplateComponentLoader.ITF_NAME;

  public static final DefinitionSourceGenerator newDefinitionSourceGenerator(
      final InputResourceLocator inputResourceLocator,
      final OutputFileLocator outputFileLocator, final IDLLoader idlLoader,
      final IDLVisitor idlCompiler,
      final StringTemplateGroupLoader templateGroupLoader,
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {
    final Map<String, Object> serviceMap = new HashMap<String, Object>();
    serviceMap.put(INPUT_RESOURCE_LOCATOR_ITF_NAME, inputResourceLocator);
    serviceMap.put(OUTPUT_FILE_LOCATOR_ITF_NAME, outputFileLocator);
    serviceMap.put(IDL_LOADER_ITF_NAME, idlLoader);
    serviceMap.put(IDL_COMPILER_ITF_NAME, idlCompiler);
    serviceMap.put(TEMPLATE_GROUP_LOADER_ITF_NAME, templateGroupLoader);

    DefinitionSourceGenerator definitionSourceGenerator;
    final CollectionInterfaceDefinitionSourceGenerator cidsg = new CollectionInterfaceDefinitionSourceGenerator();
    final DefinitionSourceGeneratorDispatcher dsgd = new DefinitionSourceGeneratorDispatcher();

    definitionSourceGenerator = cidsg;
    cidsg.clientSourceGeneratorItf = dsgd;

    // Instantiate the default source generators
    final DefinitionHeaderSourceGenerator dhsg = new DefinitionHeaderSourceGenerator();
    final DefinitionIncSourceGenerator disg = new DefinitionIncSourceGenerator();
    final ImplementationHeaderSourceGenerator ihsg = new ImplementationHeaderSourceGenerator();
    final DefinitionMacroSourceGenerator dmsg = new DefinitionMacroSourceGenerator();
    final MembraneSourceGenerator msg = new MembraneSourceGenerator();
    final IDLDefinitionSourceGenerator idsg = new IDLDefinitionSourceGenerator();
    final GenericDefinitionNameSourceGenerator gdnsg = new GenericDefinitionNameSourceGenerator();
    final BinaryADLWriter baw = new BinaryADLWriter();
    // Bind the default source generators to the dispatcher
    dsgd.visitorsItf.put("header", dhsg);
    dsgd.visitorsItf.put("include", disg);
    dsgd.visitorsItf.put("impl", ihsg);
    dsgd.visitorsItf.put("macro", dmsg);
    dsgd.visitorsItf.put("membrane", msg);
    dsgd.visitorsItf.put("idl", idsg);
    dsgd.visitorsItf.put("generic-names", gdnsg);
    dsgd.visitorsItf.put("binary-writer", baw);

    // Bind the default source generator's client interfaces
    for (final String visitorName : dsgd.visitorsItf.keySet()) {
      final VoidVisitor<Definition> visitor = dsgd.visitorsItf.get(visitorName);
      for (final String itfName : ((BindingController) visitor).listFc()) {
        bindVisitor(serviceMap, visitorName, visitor, itfName);
      }
    }

    // Instantiate and bind the visitor extensions
    final Collection<VisitorExtension> visitorExtensions = VisitorExtensionHelper
        .getVisitorExtensions(
            VisitorExtensionHelper.DEFINITION_SOURCE_GENERATOR_EXTENSION,
            pluginManagerItf, context);
    for (final VisitorExtension visitorExtension : visitorExtensions) {
      final VoidVisitor<Definition> visitor = (VoidVisitor<Definition>) visitorExtension
          .getVisitor();
      dsgd.visitorsItf.put(visitorExtension.getVisitorName(), visitor);
      for (final String itfName : ((BindingController) visitor).listFc()) {
        bindVisitor(serviceMap, visitorExtension.getVisitorName(), visitor,
            itfName);
      }
    }

    return definitionSourceGenerator;
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
      final DefinitionCompiler definitionCompiler, final Loader adlLoader,
      final StringTemplateGroupLoader templateGroupLoader,
      final PluginManager pluginManagerItf, final Map<Object, Object> context)
      throws ADLException {

    final Map<String, Object> serviceMap = new HashMap<String, Object>();
    serviceMap.put(INPUT_RESOURCE_LOCATOR_ITF_NAME, inputResourceLocator);
    serviceMap.put(OUTPUT_FILE_LOCATOR_ITF_NAME, outputFileLocator);
    serviceMap.put(TEMPLATE_GROUP_LOADER_ITF_NAME, templateGroupLoader);
    serviceMap.put("compiler-wrapper", compilerWrapper);
    serviceMap.put("mpp-wrapper", mppWrapper);
    serviceMap.put("definition-compiler", definitionCompiler);

    // Instance compiler
    InstanceCompiler instanceCompiler;
    final BasicInstanceCompiler bic = new BasicInstanceCompiler();
    final InstanceSourceGeneratorDispatcher isgd = new InstanceSourceGeneratorDispatcher();

    instanceCompiler = bic;
    bic.instanceSourceGeneratorItf = isgd;

    bic.inputResourceLocatorItf = inputResourceLocator;
    bic.compilerWrapperItf = compilerWrapper;
    bic.mppWrapperItf = mppWrapper;
    bic.outputFileLocatorItf = outputFileLocator;
    // Create and bind the default instance source generator
    final InstanceSourceGenerator instanceSourceGenerator = new BasicInstanceSourceGenerator();
    for (final String itfName : ((BindingController) instanceSourceGenerator)
        .listFc()) {
      bindVisitor(serviceMap, "instance", instanceSourceGenerator, itfName);
    }
    bindVisitorToDispatcher(isgd, instanceSourceGenerator, "instance");

    // Instance source generators
    for (final VisitorExtension visitorExtension : VisitorExtensionHelper
        .getVisitorExtensions(VisitorExtensionHelper.INSTANCE_SOURCE_GENERATOR,
            pluginManagerItf, context)) {
      final VoidVisitor<?> visitor = visitorExtension.getVisitor();
      bindVisitorToDispatcher(isgd, visitor, visitorExtension.getVisitorName());
      for (final String itfName : ((BindingController) visitor).listFc()) {
        bindVisitor(serviceMap, visitorExtension.getVisitorName(), visitor,
            itfName);
      }
    }

    // graph compiler
    final BasicGraphCompiler bgc = new BasicGraphCompiler();
    final FactoryGraphCompiler fgc = new FactoryGraphCompiler();
    final BasicGraphLinker bgl = new BasicGraphLinker();
    final GraphCompiler graphCompiler = bgl;
    bgl.clientCompilerItf = fgc;
    fgc.clientCompilerItf = bgc;

    fgc.definitionCompilerItf = definitionCompiler;
    fgc.loaderItf = adlLoader;

    bgl.compilerWrapperItf = compilerWrapper;
    bgl.outputFileLocatorItf = outputFileLocator;
    bgl.implementationLocatorItf = implementationLocator;
    bgc.definitionCompilerItf = definitionCompiler;
    bgc.instanceCompilerItf = instanceCompiler;

    return graphCompiler;
  }

  public static CompilationCommandExecutor newCompilationCommandExecutor(
      final ErrorManager errorManager) {
    final BasicCompilationCommandExecutor bcce = new BasicCompilationCommandExecutor();
    bcce.errorManagerItf = errorManager;
    return bcce;
  }

  private static void bindVisitor(final Map<String, Object> serviceMap,
      final String visitorName, final VoidVisitor<?> visitor,
      final String itfName) throws ADLException {
    try {
      ((BindingController) visitor).bindFc(itfName, serviceMap.get(itfName));
    } catch (final NoSuchInterfaceException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Illegal binding of the interface '" + itfName + "' of the visitor '"
              + visitorName + "'.", e);
    } catch (final IllegalBindingException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Illegal binding of the interface '" + itfName + "' of the visitor '"
              + visitorName + "'.", e);
    } catch (final IllegalLifeCycleException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Cannot bind the interface '" + itfName + "' of the visitor '"
              + visitorName + "'.", e);
    }
  }

  private static void bindVisitorToDispatcher(
      final InstanceSourceGeneratorDispatcher dispatcher,
      final VoidVisitor<?> visitor, final String visitorName)
      throws ADLException {
    final String itfName = InstanceSourceGeneratorDispatcher.CLIENT_VISITOR
        + "-" + visitorName;
    try {
      dispatcher.bindFc(itfName, visitor);
    } catch (final NoSuchInterfaceException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Illegal binding of the interface '" + itfName + "' of the visitor '"
              + visitorName + "'.", e);
    } catch (final IllegalBindingException e) {

      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Illegal binding of the interface '" + itfName + "' to the visitor '"
              + visitorName + "'.", e);
    } catch (final IllegalLifeCycleException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Cannot bind the interface '" + itfName + "' to the visitor '"
              + visitorName + "'.", e);
    }

  }
}
