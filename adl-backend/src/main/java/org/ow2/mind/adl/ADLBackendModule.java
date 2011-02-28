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
 * Contributors: 
 */

package org.ow2.mind.adl;

import org.ow2.mind.adl.compilation.AnnotationFlagsCompilationCommandFactory;
import org.ow2.mind.adl.compilation.BasicCompilationCommandFactory;
import org.ow2.mind.adl.compilation.CompilationCommandFactory;
import org.ow2.mind.adl.compilation.ContextFlagsCompilationCommandFactory;
import org.ow2.mind.adl.factory.FactoryFlagExtractor;
import org.ow2.mind.adl.factory.FactoryGraphCompiler;
import org.ow2.mind.adl.generic.GenericDefinitionNameSourceGenerator;
import org.ow2.mind.adl.idl.IDLDefinitionSourceGenerator;
import org.ow2.mind.adl.interfaces.CollectionInterfaceDefinitionSourceGenerator;
import org.ow2.mind.adl.membrane.MembraneSourceGenerator;
import org.ow2.mind.inject.AbstractMindModule;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class ADLBackendModule extends AbstractMindModule {

  /**
   * Returns the {@link Multibinder} that can be used to add
   * {@link DefinitionSourceGenerator}.
   * 
   * @param binder the local binder.
   * @return the {@link Multibinder} that can be used to add
   *         {@link DefinitionSourceGenerator}.
   */
  public static Multibinder<DefinitionSourceGenerator> getDefinitionSourceGeneratorMultiBinder(
      final Binder binder) {
    final Multibinder<DefinitionSourceGenerator> setBinder = Multibinder
        .newSetBinder(binder, DefinitionSourceGenerator.class);
    return setBinder;
  }

  protected void configureDefinitionSourceGenerator() {
    bind(DefinitionSourceGenerator.class).toChainStartingWith(
        CollectionInterfaceDefinitionSourceGenerator.class).endingWith(
        DefinitionSourceGeneratorDispatcher.class);

    final Multibinder<DefinitionSourceGenerator> setBinder = getDefinitionSourceGeneratorMultiBinder(binder());
    setBinder.addBinding().to(DefinitionHeaderSourceGenerator.class);
    setBinder.addBinding().to(DefinitionIncSourceGenerator.class);
    setBinder.addBinding().to(ImplementationHeaderSourceGenerator.class);
    setBinder.addBinding().to(DefinitionMacroSourceGenerator.class);
    setBinder.addBinding().to(MembraneSourceGenerator.class);
    setBinder.addBinding().to(IDLDefinitionSourceGenerator.class);
    setBinder.addBinding().to(GenericDefinitionNameSourceGenerator.class);
    setBinder.addBinding().to(BinaryADLWriter.class);
  }

  protected void configureDefinitionHeaderSourceGenerator() {
    bind(String.class).annotatedWith(
        Names.named(DefinitionHeaderSourceGenerator.TEMPLATE_NAME)).toInstance(
        DefinitionHeaderSourceGenerator.DEFAULT_TEMPLATE);
  }

  protected void configureDefinitionIncSourceGenerator() {
    bind(String.class).annotatedWith(
        Names.named(DefinitionIncSourceGenerator.TEMPLATE_NAME)).toInstance(
        DefinitionIncSourceGenerator.DEFAULT_TEMPLATE);
  }

  protected void configureDefinitionMacroSourceGenerator() {
    bind(String.class).annotatedWith(
        Names.named(DefinitionMacroSourceGenerator.TEMPLATE_NAME)).toInstance(
        DefinitionMacroSourceGenerator.DEFAULT_TEMPLATE);
  }

  protected void configureMembraneSourceGenerator() {
    bind(String.class).annotatedWith(
        Names.named(MembraneSourceGenerator.TEMPLATE_NAME)).toInstance(
        MembraneSourceGenerator.DEFAULT_TEMPLATE);
  }

  /**
   * Returns the {@link Multibinder} that can be used to add
   * {@link InstanceSourceGenerator}.
   * 
   * @param binder the local binder.
   * @return the {@link Multibinder} that can be used to add
   *         {@link InstanceSourceGenerator}.
   */
  public static Multibinder<InstanceSourceGenerator> getInstanceSourceGeneratorMultiBinder(
      final Binder binder) {
    final Multibinder<InstanceSourceGenerator> setBinder = Multibinder
        .newSetBinder(binder, InstanceSourceGenerator.class);
    return setBinder;
  }

  protected void configureInstanceSourceGenerator() {
    bind(InstanceSourceGenerator.class).to(
        InstanceSourceGeneratorDispatcher.class);

    final Multibinder<InstanceSourceGenerator> setBinder = getInstanceSourceGeneratorMultiBinder(binder());
    setBinder.addBinding().to(BasicInstanceSourceGenerator.class);
  }

  protected void configureBasicInstanceSourceGenerator() {
    bind(String.class).annotatedWith(
        Names.named(BasicInstanceSourceGenerator.TEMPLATE_NAME)).toInstance(
        BasicInstanceSourceGenerator.DEFAULT_TEMPLATE);
  }

  protected void configureDefinitionCompiler() {
    bind(DefinitionCompiler.class).to(BasicDefinitionCompiler.class);
  }

  protected void configureInstanceCompiler() {
    bind(InstanceCompiler.class).to(BasicInstanceCompiler.class);
  }

  protected void configureGraphCompiler() {
    bind(GraphCompiler.class).toChainStartingWith(BasicGraphLinker.class)
        .followedBy(FactoryGraphCompiler.class)
        .endingWith(BasicGraphCompiler.class);
  }

  protected void configureCompilationCommandFactory() {
    bind(CompilationCommandFactory.class)
        .toChainStartingWith(AnnotationFlagsCompilationCommandFactory.class)
        .followedBy(ContextFlagsCompilationCommandFactory.class)
        .endingWith(BasicCompilationCommandFactory.class);
  }

  protected void configureFlagExtractor() {
    bind(FlagExtractor.class).toChainStartingWith(FactoryFlagExtractor.class)
        .endingWith(BasicFlagExtractor.class);
  }

  // this binding overrides a ADLFrontend one.
  protected void configureADLLocator() {
    bind(ADLLocator.class).toChainStartingWith(OutputBinaryADLLocator.class)
        .endingWith(BasicADLLocator.class);
  }
}
