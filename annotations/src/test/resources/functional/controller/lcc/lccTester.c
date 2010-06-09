#include <assert.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  int err;

  err = CALL(testedLCC, getFcState)();
  assert(err == FRACTAL_API_STOPPED);

  err = CALL(testedLCC, startFc)();
  assert(err == FRACTAL_API_OK);

  err = CALL(testedLCC, getFcState)();
  assert(err == FRACTAL_API_STARTED);

  err =  CALL(testedMain, main) (argc, argv);
  assert(err == 1);

  err = CALL(testedLCC, stopFc)();
  assert(err == FRACTAL_API_OK);

  err = CALL(testedLCC, getFcState)();
  assert(err == FRACTAL_API_STOPPED);

  err =  CALL(testedMain, main) (argc, argv);
  assert(err == 2);

  return 0;
}
