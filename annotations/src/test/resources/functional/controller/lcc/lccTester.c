#include <mindassert.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  int err;

  err = CALL(testedLCC, getFcState)();
  mindassert(err == FRACTAL_API_STOPPED);

  err = CALL(testedLCC, startFc)();
  mindassert(err == FRACTAL_API_OK);

  err = CALL(testedLCC, getFcState)();
  mindassert(err == FRACTAL_API_STARTED);

  err =  CALL(testedMain, main) (argc, argv);
  mindassert(err == 1);

  err = CALL(testedLCC, stopFc)();
  mindassert(err == FRACTAL_API_OK);

  err = CALL(testedLCC, getFcState)();
  mindassert(err == FRACTAL_API_STOPPED);

  err =  CALL(testedMain, main) (argc, argv);
  mindassert(err == 2);

  return 0;
}
