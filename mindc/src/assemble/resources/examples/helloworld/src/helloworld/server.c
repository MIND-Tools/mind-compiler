/**
 * mindc examples
 *
 * Copyright (C) 2010 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 */

#include <stdio.h>

/* -----------------------------------------------------------------------------
   Constructor implementation
   -------------------------------------------------------------------------- */

CONSTRUCTOR() {
  /* initialize the "count" private data */
  PRIVATE.count = 0;
}

/* -----------------------------------------------------------------------------
   Implementation of the s interface with signature helloworld.Service.
   -------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s, print)(string msg) {
  /* retrieve the value of the "header" attribute defined in "helloworld.Server"
     ADL. */
  string h = ATTR(header);

  /* retrieve the value of the "count" private data declared in the
     "serverData.h" file */
  int c = PRIVATE.count;

  /* Use printf to print message. */
  printf("%s(count=%d) %s", h, c, msg);

  /* increment the invocation counter. */
  PRIVATE.count ++;
}

/* void println(string msg) */
void METH(s, println)(string msg) {
  printf("%s(count=%d) %s\n", ATTR(header), PRIVATE.count++, msg);
}
