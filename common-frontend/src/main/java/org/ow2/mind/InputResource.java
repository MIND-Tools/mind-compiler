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

package org.ow2.mind;

import java.io.Serializable;
import java.net.URL;

public class InputResource implements Serializable {

  protected final String   kind;
  protected final String   name;
  protected transient URL  location;
  protected transient long timestamp = -1;

  public InputResource(final String kind, final String name) {
    if (kind == null)
      throw new IllegalArgumentException("kind cannot be null");
    if (name == null)
      throw new IllegalArgumentException("name cannot be null");

    this.kind = kind;
    this.name = name;
  }

  public String getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return 17 * kind.hashCode() + name.hashCode();
  }

  protected URL getLocation() {
    return location;
  }

  protected void setLocation(final URL location) {
    this.location = location;
  }

  protected long getTimestamp() {
    return timestamp;
  }

  protected void setTimestamp(final long timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof InputResource)) return false;
    final InputResource rsc = (InputResource) obj;
    return rsc.kind.equals(kind) && rsc.name.equals(name);
  }

  @Override
  public String toString() {
    return kind + ":" + name;
  }
}
