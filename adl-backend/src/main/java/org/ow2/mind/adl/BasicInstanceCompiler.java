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

package org.ow2.mind.adl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.PathHelper.replaceExtension;
import static org.ow2.mind.annotation.AnnotationHelper.getAnnotation;
import static org.ow2.mind.compilation.DirectiveHelper.splitOptionString;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.adl.annotation.predefined.CFlags;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerWrapper;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.preproc.MPPCommand;
import org.ow2.mind.preproc.MPPWrapper;

public class BasicInstanceCompiler
    implements
      InstanceCompiler,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String     INSTANCE_SOURCE_GENERATOR_ITF_NAME = "instance-source-generator";

  public InstanceSourceGenerator instanceSourceGeneratorItf;

  /** Client interface used to locate output files. */
  public OutputFileLocator       outputFileLocatorItf;

  public InputResourceLocator    inputResourceLocatorItf;

  public CompilerWrapper         compilerWrapperItf;

  public MPPWrapper              mppWrapperItf;

  // ---------------------------------------------------------------------------
  // Implementation of the Visitor interface
  // ---------------------------------------------------------------------------

  public Collection<CompilationCommand> visit(
      final InstancesDescriptor instanceDesc, final Map<Object, Object> context)
      throws ADLException {

    instanceSourceGeneratorItf.visit(instanceDesc, context);
    final String instancesFileName = BasicInstanceSourceGenerator
        .getInstancesFileName(instanceDesc);

    final File srcFile = outputFileLocatorItf.getCSourceOutputFile(
        instancesFileName, context);
    if (!srcFile.exists()) {
      throw new ADLException(GenericErrors.INTERNAL_ERROR,
          "Can't find source file \"" + instancesFileName + "\"");
    }

    Collection<File> dependencies = null;
    if (instanceDesc.instanceDefinition instanceof ImplementationContainer) {
      final Source sources[] = ((ImplementationContainer) instanceDesc.instanceDefinition)
          .getSources();
      if (sources.length == 1) {
        dependencies = new ArrayList<File>();
        dependencies.add(outputFileLocatorItf.getCSourceTemporaryOutputFile(
            ImplementationHeaderSourceGenerator
                .getImplHeaderFileName(instanceDesc.instanceDefinition),
            context));
      } else if (sources.length > 1) {
        dependencies = new ArrayList<File>();
        for (int i = 0; i < sources.length; i++) {
          dependencies.add(outputFileLocatorItf.getCSourceTemporaryOutputFile(
              ImplementationHeaderSourceGenerator.getImplHeaderFileName(
                  instanceDesc.instanceDefinition, i), context));
        }
      }
    }

    final File cppFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
        replaceExtension(instancesFileName, ".i"), context);
    final File mppFile = outputFileLocatorItf.getCSourceTemporaryOutputFile(
        replaceExtension(instancesFileName, ".mpp.c"), context);
    final File objectFile = outputFileLocatorItf.getCCompiledOutputFile(
        replaceExtension(instancesFileName, ".o"), context);
    final File depFile = outputFileLocatorItf.getCCompiledOutputFile(
        replaceExtension(instancesFileName, ".d"), context);

    final PreprocessorCommand cppCommand = newPreprocessorCommand(
        instanceDesc.instanceDefinition, srcFile, dependencies, depFile,
        cppFile, context);
    final MPPCommand mppCommand = newMPPCommand(
        instanceDesc.instanceDefinition, cppFile, mppFile, context);
    final CompilerCommand gccCommand = newCompilerCommand(
        instanceDesc.instanceDefinition, mppFile, objectFile, context);

    final List<CompilationCommand> compilationTasks = new ArrayList<CompilationCommand>();
    compilationTasks.add(cppCommand);
    compilationTasks.add(mppCommand);
    compilationTasks.add(gccCommand);

    return compilationTasks;
  }

  protected PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = compilerWrapperItf
        .newPreprocessorCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setDependencyOutputFile(depFile);

    if (dependencies != null) {
      for (final File dep : dependencies) {
        command.addDependency(dep);
      }
    }

    command.addIncludeDir(outputFileLocatorItf.getCSourceOutputDir(context));
    command.addIncludeDir(outputFileLocatorItf
        .getCSourceTemporaryOutputDir(context));

    final URL[] inputResourceRoots = inputResourceLocatorItf
        .getInputResourcesRoot(context);
    if (inputResourceRoots != null) {
      for (final URL inputResourceRoot : inputResourceRoots) {
        try {
          final File inputDir = new File(inputResourceRoot.toURI());
          if (inputDir.isDirectory()) {
            command.addIncludeDir(inputDir);
          }
        } catch (final URISyntaxException e) {
          continue;
        }
      }
    }

    // Add definition level C-Flags
    final CFlags definitionflags = getAnnotation(definition, CFlags.class);
    if (definitionflags != null)
      command.addFlags(splitOptionString(definitionflags.value));

    return command;
  }

  protected MPPCommand newMPPCommand(final Definition definition,
      final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final MPPCommand command = mppWrapperItf.newMPPCommand(definition, context);
    command.setOutputFile(outputFile).setInputFile(inputFile);

    if (ASTHelper.isSingleton(definition)) {
      command.setSingletonMode();
    }

    return command;
  }

  protected CompilerCommand newCompilerCommand(final Definition definition,
      final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = compilerWrapperItf
        .newCompilerCommand(context);
    command.setOutputFile(outputFile).setInputFile(inputFile)
        .setAllDependenciesManaged(true);

    command.addIncludeDir(outputFileLocatorItf.getCSourceOutputDir(context));

    command.addIncludeFile(outputFileLocatorItf.getCSourceOutputFile(
        DefinitionMacroSourceGenerator.getMacroFileName(definition), context));

    // Add definition level C-Flags
    final CFlags definitionflags = getAnnotation(definition, CFlags.class);
    if (definitionflags != null)
      command.addFlags(splitOptionString(definitionflags.value));

    return command;
  }

  // ---------------------------------------------------------------------------
  // implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(INSTANCE_SOURCE_GENERATOR_ITF_NAME)) {
      instanceSourceGeneratorItf = (InstanceSourceGenerator) value;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = (OutputFileLocator) value;
    } else if (itfName.equals(CompilerWrapper.ITF_NAME)) {
      compilerWrapperItf = (CompilerWrapper) value;
    } else if (itfName.equals(MPPWrapper.ITF_NAME)) {
      mppWrapperItf = (MPPWrapper) value;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = (InputResourceLocator) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public String[] listFc() {
    return listFcHelper(INSTANCE_SOURCE_GENERATOR_ITF_NAME,
        OutputFileLocator.ITF_NAME, CompilerWrapper.ITF_NAME,
        MPPWrapper.ITF_NAME, InputResourceLocator.ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(INSTANCE_SOURCE_GENERATOR_ITF_NAME)) {
      return instanceSourceGeneratorItf;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      return outputFileLocatorItf;
    } else if (itfName.equals(CompilerWrapper.ITF_NAME)) {
      return compilerWrapperItf;
    } else if (itfName.equals(MPPWrapper.ITF_NAME)) {
      return mppWrapperItf;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      return inputResourceLocatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(INSTANCE_SOURCE_GENERATOR_ITF_NAME)) {
      instanceSourceGeneratorItf = null;
    } else if (itfName.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = null;
    } else if (itfName.equals(CompilerWrapper.ITF_NAME)) {
      compilerWrapperItf = null;
    } else if (itfName.equals(MPPWrapper.ITF_NAME)) {
      mppWrapperItf = null;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
