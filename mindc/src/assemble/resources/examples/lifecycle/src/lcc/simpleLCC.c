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

// -----------------------------------------------------------------------------
// Constructor implementation
// -----------------------------------------------------------------------------

CONSTRUCTOR() {
  printf("In CONSTRUCTOR\n");
}

DESTRUCTOR() {
  printf("In DESTRUCTOR\n");
}

// -----------------------------------------------------------------------------
// Implementation of the entryPoint interface with signature boot.Main.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(entryPoint, main) (int argc, char *argv[]) {

  printf("In entryPoint.main\n");

  return 0;
}

// -----------------------------------------------------------------------------
// Implementation of the lifeCycleController interface with signature boot.Main.
// -----------------------------------------------------------------------------


int METH(lifeCycleController, startFc) (void) {
  printf("In lifeCycleController.startFc\n");
  return FRACTAL_API_OK;
}

int METH(lifeCycleController, stopFc) (void) {
  printf("In lifeCycleController.stopFc\n");
  return FRACTAL_API_OK;
}
