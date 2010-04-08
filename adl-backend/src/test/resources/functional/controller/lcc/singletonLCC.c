#include <stdio.h>

int a = 0;

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){
  return a;
}

/* -----------------------------------------------------------------------------
   Implementation of the lifeCycleController interface.
----------------------------------------------------------------------------- */

int METH(lifeCycleController, startFc) (void) {
  printf("in startFc\n");
  a = 1;
  return FRACTAL_API_OK;
}

int METH(lifeCycleController, stopFc) (void) {
  printf("in stopFc\n");
  a = 2;
  return FRACTAL_API_OK;
}
