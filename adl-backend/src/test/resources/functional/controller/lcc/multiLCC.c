
/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){
  return PRIVATE.a;
}

/* -----------------------------------------------------------------------------
   Implementation of the lifeCycleController interface.
----------------------------------------------------------------------------- */

int METH(lifeCycleController, startFc) (void) {
  PRIVATE.a = 1;
  return FRACTAL_API_OK;
}

int METH(lifeCycleController, stopFc) (void) {
  PRIVATE.a = 2;
  return FRACTAL_API_OK;
}
