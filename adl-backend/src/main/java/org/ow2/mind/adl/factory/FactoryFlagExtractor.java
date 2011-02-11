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

package org.ow2.mind.adl.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.adl.FlagExtractor.AbstractDelegatingFlagExtractor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Source;

import com.google.inject.Inject;

public class FactoryFlagExtractor extends AbstractDelegatingFlagExtractor {

  @Inject
  protected Loader loaderItf;

  public Collection<String> getCFlags(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final Definition instanciatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(definition, loaderItf, context);
    if (instanciatedDefinition == null) {
      return clientExtractorItf.getCFlags(definition, context);
    } else {
      final List<String> flags = new ArrayList<String>(
          clientExtractorItf.getCFlags(definition, context));
      flags.addAll(clientExtractorItf
          .getCFlags(instanciatedDefinition, context));
      return flags;
    }
  }

  public Collection<String> getCFlags(final Source source,
      final Map<Object, Object> context) throws ADLException {
    return clientExtractorItf.getCFlags(source, context);
  }

  public Collection<String> getLDFlags(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final Definition instanciatedDefinition = ASTHelper
        .getFactoryInstantiatedDefinition(definition, loaderItf, context);
    if (instanciatedDefinition == null) {
      return clientExtractorItf.getLDFlags(definition, context);
    } else {
      final List<String> flags = new ArrayList<String>(
          clientExtractorItf.getLDFlags(definition, context));
      flags.addAll(clientExtractorItf.getLDFlags(instanciatedDefinition,
          context));
      return flags;
    }
  }

  public Collection<String> getLDFlags(final Source source,
      final Map<Object, Object> context) throws ADLException {
    return clientExtractorItf.getLDFlags(source, context);
  }
}
