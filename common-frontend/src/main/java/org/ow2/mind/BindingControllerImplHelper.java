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

import org.objectweb.fractal.api.control.BindingController;

/**
 * Helper class for the implementation of the {@link BindingController}
 * interface. This class is not strictly related to Mind-compiler and could be
 * moved to fractal-util package.
 */
public final class BindingControllerImplHelper {

  private BindingControllerImplHelper() {
  }

  /**
   * If the given interface name is <code>null</code>, throws an
   * {@link IllegalArgumentException}.
   * 
   * @param itfName an interface name.
   * @throws IllegalArgumentException if the given interface name is
   *           <code>null</code>.
   */
  public static void checkItfName(final String itfName)
      throws IllegalArgumentException {
    if (itfName == null) {
      throw new IllegalArgumentException("Interface name can't be null");
    }
  }

  /**
   * Returns an array of String that contains the given interface names. This
   * method can be used to implement the {@link BindingController#listFc()}
   * method. For instance :
   * 
   * <pre>
   * public String[] listFc() {
   *   return listFcHelper(&lt;list of client interfaces&gt;);
   * }
   * </pre>
   * 
   * @param itfNames a list of interface name
   * @return an array that contains the given interface names.
   */
  public static String[] listFcHelper(final String... itfNames) {
    return itfNames;
  }

  /**
   * Returns a concatenation of the provided String arrays. This method can be
   * used to override an inherited implementation of the
   * {@link BindingController#listFc()} method. For instance :
   * 
   * <pre>
   * &#064;Override
   * public String[] listFc() {
   *   return listFcHelper(super.listFc(), &lt;additional client interfaces&gt;);
   * }
   * </pre>
   * 
   * @param superListFc the inherited client interfaces
   * @param itfNames the additional client interfaces.
   * @return an array that contains the inherited and the additional client
   *         interfaces.
   */
  public static String[] listFcHelper(final String[] superListFc,
      final String... itfNames) {
    final String[] result = new String[superListFc.length + itfNames.length];
    System.arraycopy(superListFc, 0, result, 0, superListFc.length);
    System.arraycopy(itfNames, 0, result, superListFc.length, itfNames.length);

    return result;
  }
}
