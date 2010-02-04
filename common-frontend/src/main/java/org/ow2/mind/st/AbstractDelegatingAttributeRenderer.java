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

package org.ow2.mind.st;

import org.antlr.stringtemplate.AttributeRenderer;

public abstract class AbstractDelegatingAttributeRenderer
    implements
      AttributeRenderer {

  public AttributeRenderer delegate;

  public String toString(final Object o) {
    return o.toString();
  }

  public String toString(final Object o, final String format) {
    final String s = render(o, format);
    if (s == null && delegate != null) return delegate.toString(o, format);

    return o.toString();
  }

  protected abstract String render(final Object o, String format);
}
