/**
 * Copyright (C) 2010 STMicroelectronics
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
 * Contributors: Julien Tous
 */

package org.ow2.mind.adl.compilation;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.adl.compilation.CompilationCommandFactory.AbstractDelegatingCompilationCommandFactory;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.compilation.AssemblerCommand;
import org.ow2.mind.compilation.CompilerCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.LinkerCommand;
import org.ow2.mind.compilation.PreprocessorCommand;
import org.ow2.mind.io.OutputFileLocator;
import org.ow2.mind.preproc.MPPCommand;

import com.google.inject.Inject;

public class ContextFlagsCompilationCommandFactory
    extends
      AbstractDelegatingCompilationCommandFactory {

  @Inject
  protected OutputFileLocator    outputFileLocatorItf;

  @Inject
  protected InputResourceLocator inputResourceLocatorItf;

  public PreprocessorCommand newPreprocessorCommand(
      final Definition definition, final Object source, final File inputFile,
      final Collection<File> dependencies, final File depFile,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final PreprocessorCommand command = factoryDelegate.newPreprocessorCommand(
        definition, source, inputFile, dependencies, depFile, outputFile,
        context);

    command.addFlags(CompilerContextHelper.getCPPFlags(context));
    for (final String inc : CompilerContextHelper.getIncPath(context)) {
      command.addIncludeDir(new File(inc));
    }
    command.addFlags(CompilerContextHelper.getCFlags(context));

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

    return command;
  }

  public MPPCommand newMPPCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final File headerOutputFile, final Map<Object, Object> context)
      throws ADLException {
    return factoryDelegate.newMPPCommand(definition, source, inputFile,
        outputFile, headerOutputFile, context);
  }

  public CompilerCommand newCompilerCommand(final Definition definition,
      final Object source, final File inputFile,
      final boolean preprocessedFile, final Collection<File> dependencies,
      final File depFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final CompilerCommand command = factoryDelegate.newCompilerCommand(
        definition, source, inputFile, preprocessedFile, dependencies, depFile,
        outputFile, context);

    if (!preprocessedFile) {
      command.addFlags(CompilerContextHelper.getCPPFlags(context));
    }
    for (final String inc : CompilerContextHelper.getIncPath(context)) {
      command.addIncludeDir(new File(inc));
    }
    command.addFlags(CompilerContextHelper.getCFlags(context));

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

    return command;
  }

  public AssemblerCommand newAssemblerCommand(final Definition definition,
      final Object source, final File inputFile, final File outputFile,
      final Map<Object, Object> context) throws ADLException {
    final AssemblerCommand command = factoryDelegate.newAssemblerCommand(
        definition, source, inputFile, outputFile, context);

    command.addFlags(CompilerContextHelper.getASFlags(context));

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

    return command;
  }

  public LinkerCommand newLinkerCommand(final ComponentGraph graph,
      final File outputFile, final Map<Object, Object> context)
      throws ADLException {
    final LinkerCommand command = factoryDelegate.newLinkerCommand(graph,
        outputFile, context);

    command.addFlags(CompilerContextHelper.getLDFlags(context));
    command.setLinkerScript(CompilerContextHelper.getLinkerScript(context));

    return command;
  }

  public CompilerCommand newFileProviderCompilerCommand(final File outputFile,
      final Map<Object, Object> context) {
    return factoryDelegate.newFileProviderCompilerCommand(outputFile, context);
  }

}
