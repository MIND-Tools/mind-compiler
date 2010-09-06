#include <mindassert.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

int sharedInt;

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){
  int err;
  void *instance;

  mindassert(sharedInt == 0);

  err = CALL(factory, newFcInstance)(&instance);
  mindassert(err == 0);
  mindassert(sharedInt != 0);

  /* cast instance as a pointer to a 'main' interface, and call the 'main' method */
  err = CALL_PTR((Main) instance, main)(argc, argv);
  mindassert(err == 0);

  err = CALL(factory, destroyFcInstance)(instance);
  mindassert(err == 0);

  mindassert(sharedInt == 0);
  return 0;
}
