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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.st;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * This class extends the StringTemplate and implements a delegate for
 * {@link Map}. The class is designed for passing map object as default formal
 * arguments to {@link StringTemplate} objects. Indeed, the current interface of
 * {@link StringTemplate} accepts only {@link StringTemplate} objects as default
 * values to be assigned to template formal arguments. Thanks to this class, one
 * can also assign {@link Map} objects as default argument value.
 */
public class MapDelegateStringTemplate extends StringTemplate
    implements
      Map<Object, Object> {

  // Receiver map
  private Map<Object, Object> map = null;

  /** Constructor for {@link StringTemplate} that defines an empty {@link Map} */
  public MapDelegateStringTemplate() {
    super();
    map = new HashMap<Object, Object>();
  }

  /** Special constructor for specifying the delegated map */
  public MapDelegateStringTemplate(final Map map) {
    super();
    this.map = map;
  }

  /** Constructor for {@link StringTemplate} that defines an empty {@link Map} */
  public MapDelegateStringTemplate(final String arg0, final Class arg1) {
    super(arg0, arg1);
    map = new HashMap<Object, Object>();
  }

  /** Constructor for {@link StringTemplate} that defines an empty {@link Map} */
  public MapDelegateStringTemplate(final String arg0) {
    super(arg0);
    map = new HashMap<Object, Object>();
  }

  /** Constructor for {@link StringTemplate} that defines an empty {@link Map} */
  public MapDelegateStringTemplate(final StringTemplateGroup arg0,
      final String arg1, final HashMap arg2) {
    super(arg0, arg1, arg2);
    map = new HashMap<Object, Object>();
  }

  /** Constructor for {@link StringTemplate} that defines an empty {@link Map} */
  public MapDelegateStringTemplate(final StringTemplateGroup arg0,
      final String arg1) {
    super(arg0, arg1);
    map = new HashMap<Object, Object>();
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey(final Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(final Object value) {
    return map.containsValue(value);
  }

  public Set<java.util.Map.Entry<Object, Object>> entrySet() {
    return map.entrySet();
  }

  @Override
  public boolean equals(final Object o) {
    return map.equals(o);
  }

  public Object get(final Object key) {
    return map.get(key);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<Object> keySet() {
    return map.keySet();
  }

  public Object put(final Object key, final Object value) {
    return map.put(key, value);
  }

  public void putAll(final Map<? extends Object, ? extends Object> m) {
    map.putAll(m);
  }

  public Object remove(final Object key) {
    return map.remove(key);
  }

  public int size() {
    return map.size();
  }

  @Override
  public String toString() {
    return map.toString();
  }

  public Collection<Object> values() {
    return map.values();
  }
}
