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

package org.ow2.mind;

import java.util.Collection;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.adl.DefinitionCompiler;
import org.ow2.mind.adl.DefinitionSourceGenerator;
import org.ow2.mind.adl.GraphCompiler;
import org.ow2.mind.adl.graph.ComponentGraph;
import org.ow2.mind.adl.graph.Instantiator;
import org.ow2.mind.compilation.CompilationCommand;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.target.TargetDescriptorLoader;
import org.ow2.mind.target.TargetDescriptorOptionHandler;
import org.ow2.mind.target.ast.ADLMapping;
import org.ow2.mind.target.ast.Target;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Basic implementation of {@link ADLCompiler} interface.
 */
public class BasicADLCompiler extends AbstractADLCompiler {

  @Inject
  protected Loader                    adlLoader;
  @Inject
  protected DefinitionSourceGenerator definitionSourceGenerator;
  @Inject
  protected DefinitionCompiler        definitionCompiler;
  @Inject
  protected Instantiator              graphInstantiator;
  @Inject
  protected GraphCompiler             graphCompiler;
  @Inject
  protected TargetDescriptorLoader    targetDescriptorLoader;

  @Override
  protected void initContext(final String adlName, final String execName,
      final CompilationStage stage, final Map<Object, Object> context)
      throws ADLException {
  }

  @Override
  protected Iterable<Definition> load(String adlName,
      final Map<Object, Object> context) throws ADLException {
    final String targetDescName = TargetDescriptorOptionHandler
        .getTargetDescriptor(context);
    if (targetDescName != null) {
      final Target target = targetDescriptorLoader
          .load(targetDescName, context);
      final ADLMapping adlMapping = target.getAdlMapping();
      if (adlMapping != null && adlMapping.getMapping() != null) {
        adlName = adlMapping.getMapping().replace("${inputADL}", adlName);
      }
    }

    return Lists.newArrayList(adlLoader.load(adlName, context));
  }

  @Override
  protected void generateSource(final Map<Object, Object> context,
      final Definition adlDef) throws ADLException {
    definitionSourceGenerator.visit(adlDef, context);
  }

  @Override
  protected Collection<CompilationCommand> compileDefinition(
      final Map<Object, Object> context, final Definition adlDef)
      throws ADLException {
    return definitionCompiler.visit(adlDef, context);
  }

  @Override
  protected ComponentGraph instantiateGraph(final Map<Object, Object> context,
      final Definition adlDef) throws ADLException {
    return graphInstantiator.instantiate(adlDef, context);
  }

  @Override
  protected Collection<CompilationCommand> compileGraph(
      final Map<Object, Object> context, final ComponentGraph graph,
      String execName) throws ADLException {
    if (execName != null) {
      CompilerContextHelper.setExecutableName(context, execName);
    } else {
      final String targetDescName = TargetDescriptorOptionHandler
          .getTargetDescriptor(context);
      if (targetDescName != null) {
        final Target target = targetDescriptorLoader.load(targetDescName,
            context);
        final ADLMapping adlMapping = target.getAdlMapping();
        if (adlMapping != null && adlMapping.getOutputName() != null) {
          execName = adlMapping.getMapping().replace("${inputADL}",
              graph.getDefinition().getName());
        }
      }
    }

    return graphCompiler.visit(graph, context);
  }

}
