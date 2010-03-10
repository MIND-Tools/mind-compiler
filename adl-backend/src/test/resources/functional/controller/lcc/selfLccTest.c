#include <assert.h>

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]) {
  int err;

  err = CALL(lifeCycleController, getFcState)();
  assert(err == FRACTAL_API_STOPPED);

  err = CALL(lifeCycleController, startFc)();
  assert(err == FRACTAL_API_OK);

  err = CALL(lifeCycleController, getFcState)();
  assert(err == FRACTAL_API_STARTED);

  assert(PRIVATE.a  == 1);

  err = CALL(lifeCycleController, stopFc)();
  assert(err == FRACTAL_API_OK);

  err = CALL(lifeCycleController, getFcState)();
  assert(err == FRACTAL_API_STOPPED);

  assert(PRIVATE.a == 2);

  return 0;
}

// -----------------------------------------------------------------------------
// Implementation of the lifeCycleController interface.
// -----------------------------------------------------------------------------

int METH(lifeCycleController, startFc) (void) {
  PRIVATE.a = 1;
  return FRACTAL_API_OK;
}

int METH(lifeCycleController, stopFc) (void) {
  PRIVATE.a = 2;
  return FRACTAL_API_OK;
}
