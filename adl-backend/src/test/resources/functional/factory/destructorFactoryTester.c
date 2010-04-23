#include <assert.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

int sharedInt;

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){
  int err;
  void *instance;

  assert(sharedInt == 0);

  err = CALL(factory, newFcInstance)(&instance);
  assert(err == 0);
  assert(sharedInt != 0);

  /* cast instance as a pointer to a 'main' interface, and call the 'main' method */
  err = CALL_PTR((Main) instance, main)(argc, argv);
  assert(err == 0);

  err = CALL(factory, destroyFcInstance)(instance);
  assert(err == 0);

  assert(sharedInt == 0);
  return 0;
}
