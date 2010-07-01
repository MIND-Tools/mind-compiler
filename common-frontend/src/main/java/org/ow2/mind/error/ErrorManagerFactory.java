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

package org.ow2.mind.error;

import java.io.PrintStream;

public final class ErrorManagerFactory {

  private ErrorManagerFactory() {
  }

  public static ErrorManager newSimpleErrorManager() {
    return new SimpleErrorManager();
  }

  public static ErrorManager newStreamErrorManager() {
    ErrorManager errorManager;
    final StreamErrorManager sem = new StreamErrorManager();
    errorManager = sem;

    return errorManager;
  }

  public static ErrorManager newStreamErrorManager(final PrintStream stream,
      final boolean printStackTraces) {
    ErrorManager errorManager;
    final StreamErrorManager sem = new StreamErrorManager();
    sem.printStackTraces = printStackTraces;
    sem.errorStream = stream;
    sem.warningStream = stream;

    errorManager = sem;

    return errorManager;
  }
}
