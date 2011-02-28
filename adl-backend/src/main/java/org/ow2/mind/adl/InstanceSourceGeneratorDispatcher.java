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

import java.util.Map;
import java.util.Set;

import org.ow2.mind.AbstractVoidVisitorDispatcher;
import org.ow2.mind.VoidVisitor;
import org.ow2.mind.plugin.PluginManager;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class InstanceSourceGeneratorDispatcher
    extends
      AbstractVoidVisitorDispatcher<InstancesDescriptor>
    implements
      InstanceSourceGenerator {

  @Inject
  protected Set<InstanceSourceGenerator> visitorsItf;
  @Inject
  protected PluginManager                pluginManager;

  @Override
  protected Iterable<? extends VoidVisitor<InstancesDescriptor>> getVisitorsItf(
      final Map<Object, Object> context) {
    return Iterables.concat(visitorsItf, VisitorExtensionHelper
        .getInstanceSourceGeneratorExtensions(pluginManager, context));
  }
}
