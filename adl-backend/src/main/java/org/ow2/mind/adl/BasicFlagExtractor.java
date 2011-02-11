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

import static org.ow2.mind.annotation.AnnotationHelper.getAnnotation;
import static org.ow2.mind.compilation.DirectiveHelper.splitOptionString;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.adl.annotation.predefined.CFlags;
import org.ow2.mind.adl.annotation.predefined.LDFlags;
import org.ow2.mind.adl.ast.Source;

public class BasicFlagExtractor implements FlagExtractor {

  public Collection<String> getCFlags(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final CFlags flags = getAnnotation(definition, CFlags.class);
    if (flags != null)
      return splitOptionString(flags.value);
    else
      return Collections.emptyList();
  }

  public Collection<String> getCFlags(final Source source,
      final Map<Object, Object> context) throws ADLException {
    final CFlags flags = getAnnotation(source, CFlags.class);
    if (flags != null)
      return splitOptionString(flags.value);
    else
      return Collections.emptyList();
  }

  public Collection<String> getLDFlags(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final LDFlags flags = getAnnotation(definition, LDFlags.class);
    if (flags != null)
      return splitOptionString(flags.value);
    else
      return Collections.emptyList();
  }

  public Collection<String> getLDFlags(final Source source,
      final Map<Object, Object> context) throws ADLException {
    final LDFlags flags = getAnnotation(source, LDFlags.class);
    if (flags != null)
      return splitOptionString(flags.value);
    else
      return Collections.emptyList();
  }
}
