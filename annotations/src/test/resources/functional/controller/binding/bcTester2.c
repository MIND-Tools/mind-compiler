#include <assert.h>
#include <string.h>

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------


// int main(int argc, string[] argv)
int METH(main, main)(int argc, char *argv[]) {
  void *itf_ptr;

  // starts with clientMain interface bound to tested1 (through enclosing
  // composite)
  assert(IS_BOUND(clientMain));
  assert(CALL(clientMain, main)(argc, argv) == 1);

  // unbound clientMain interface of enclosing composite
  assert(CALL(superBC, unbindFc)("clientMain") == FRACTAL_API_OK);
  assert(! IS_BOUND(clientMain));

  // rebound clientMain interface of enclosing composite on tested2
  assert(CALL(clientComp[2], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
  assert(CALL(superBC, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
  // clientMain is still unbound since main interface of tested2 composite is not
  // bound internally
  assert(! IS_BOUND(clientMain));

  // rebound clientMain interface of enclosing composite on tested1
  assert(CALL(clientComp[1], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
  assert(CALL(superBC, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
  assert(IS_BOUND(clientMain));
  assert(CALL(clientMain, main)(argc, argv) == 1);

  // unbound my clientMain interface
  assert(CALL(bindingController, unbindFc)("clientMain") == FRACTAL_API_OK);
  assert(! IS_BOUND(clientMain));

  // rebound clientMain interface of myself on tested0
   assert(CALL(clientComp[0], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
   assert(CALL(bindingController, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
   assert(IS_BOUND(clientMain));
   assert(CALL(clientMain, main)(argc, argv) == 0);

  return 0;
}
