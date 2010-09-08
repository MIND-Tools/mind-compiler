#include <mindassert.h>
#include <string.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main)(int argc, char *argv[]) {
  void *itf_ptr;

  /* starts with clientMain interface bound to tested1 (through enclosing
     composite) */
  mindassert(IS_BOUND(clientMain));
  mindassert(CALL(clientMain, main)(argc, argv) == 1);

  /* unbound clientMain interface of enclosing composite */
  mindassert(CALL(superBC, unbindFc)("clientMain") == FRACTAL_API_OK);
  mindassert(! IS_BOUND(clientMain));

  /* rebound clientMain interface of enclosing composite on tested2 */
  mindassert(CALL(clientComp[2], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
  mindassert(CALL(superBC, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
  /* clientMain is still unbound since main interface of tested2 composite is not
     bound internally */
  mindassert(! IS_BOUND(clientMain));

  /* rebound clientMain interface of enclosing composite on tested1 */
  mindassert(CALL(clientComp[1], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
  mindassert(CALL(superBC, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
  mindassert(IS_BOUND(clientMain));
  mindassert(CALL(clientMain, main)(argc, argv) == 1);

  /* unbound my clientMain interface */
  mindassert(CALL(bindingController, unbindFc)("clientMain") == FRACTAL_API_OK);
  mindassert(! IS_BOUND(clientMain));

  /* rebound clientMain interface of myself on tested0 */
  mindassert(CALL(clientComp[0], getFcInterface)("main", &itf_ptr) == FRACTAL_API_OK);
  mindassert(CALL(bindingController, bindFc)("clientMain", itf_ptr) == FRACTAL_API_OK);
  mindassert(IS_BOUND(clientMain));
  mindassert(CALL(clientMain, main)(argc, argv) == 0);

  return 0;
}
