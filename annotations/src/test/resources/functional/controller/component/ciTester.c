#include <mindassert.h>
#include <string.h>

#define NB_ITF 2

const char * expectedNames[NB_ITF] = {"component", "main"};
const char * expectedSignatures[NB_ITF] = {"fractal.api.Component", "Main"};
int expectedRoles[NB_ITF] = {FRACTAL_API_SERVER, FRACTAL_API_SERVER};

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  int nbItf, err, i;
  const char *itfNames[NB_ITF];
  void *itfRefs[NB_ITF];
  void *itfRef;
  const char *signature;

  nbItf = CALL(testedComponent, listFcInterfaces)(NULL);
  mindassert(nbItf == NB_ITF);

  err = CALL(testedComponent, listFcInterfaces)(itfNames);
  mindassert(err == NB_ITF);

  err = CALL(testedComponent, getFcInterfaces)(itfRefs);
  mindassert(err == NB_ITF);

  for (i = 0; i < NB_ITF; i++) {
    mindassert(itfNames[i] != NULL);
    mindassert(strcmp(itfNames[i], expectedNames[i]) == 0);

    mindassert(CALL(testedComponent, getFcInterfaceRole)(itfNames[i]) == expectedRoles[i]);
    mindassert(CALL(testedComponent, getFcInterfaceSignature)(itfNames[i], &signature) == FRACTAL_API_OK);
    mindassert(strcmp(signature, expectedSignatures[i]) == 0);

    mindassert(CALL(testedComponent, getFcInterface)(itfNames[i], &itfRef) == FRACTAL_API_OK);
    mindassert(itfRef == itfRefs[i]);
  }

  err = CALL(testedComponent, getFcInterface) ("main", &itfRef);
  mindassert (err == FRACTAL_API_OK);

  err = CALL_PTR((Main) itfRef, main) (argc, argv);

  return err;
}

