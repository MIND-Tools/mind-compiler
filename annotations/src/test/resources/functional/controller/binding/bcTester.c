#include <mindassert.h>
#include <string.h>

int testCalled = 0;
int testCollection0Called = 0;
int testCollection1Called = 0;
int testCollection2Called = 0;

#define NB_ITF 4

const char * expectedNames[NB_ITF] = { "clientMain", "clientCollectionMain[0]",
    "clientCollectionMain[1]", "clientCollectionMain[2]" };
void *expectedBinding[NB_ITF] = { GET_MY_INTERFACE(test),
    GET_MY_INTERFACE(testCollection[0]), GET_MY_INTERFACE(testCollection[1]),
    GET_MY_INTERFACE(testCollection[2]) };

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main)(int argc, char *argv[]) {
  int nbItf, err, i;
  const char *itfNames[NB_ITF];
  void *itfRef;

  nbItf = CALL(testedBC, listFc)(NULL);
  mindassert(nbItf == NB_ITF);

  err = CALL(testedBC, listFc)(itfNames);
  mindassert(err == NB_ITF);

  for (i = 0; i < NB_ITF; i++) {
    mindassert(itfNames[i] != NULL);
    mindassert(strcmp(itfNames[i], expectedNames[i]) == 0);

    mindassert(CALL(testedBC, lookupFc)(itfNames[i], &itfRef) == FRACTAL_API_OK);
    mindassert(((Main) itfRef)->selfData == ((Main) expectedBinding[i])->selfData);
    mindassert(((Main) itfRef)->meths == ((Main) expectedBinding[i])->meths);

    mindassert(CALL(testedBC, unbindFc)(itfNames[i]) == FRACTAL_API_OK);
    mindassert(CALL(testedBC, lookupFc)(itfNames[i], &itfRef) == FRACTAL_API_OK);
    mindassert(itfRef == NULL);

    mindassert(CALL(testedBC, bindFc)(itfNames[i], expectedBinding[i]) == FRACTAL_API_OK);
    mindassert(CALL(testedBC, lookupFc)(itfNames[i], &itfRef) == FRACTAL_API_OK);
    mindassert(((Main) itfRef)->selfData == ((Main) expectedBinding[i])->selfData);
    mindassert(((Main) itfRef)->meths == ((Main) expectedBinding[i])->meths);

  }

  err = CALL(testedMain, main) (argc, argv);

  mindassert(err == 5);
  mindassert(testCalled);
  mindassert(testCollection0Called);
  mindassert(testCollection1Called);
  mindassert(testCollection2Called);

  return 0;
}

/* -----------------------------------------------------------------------------
   Implementation of the test interface.
----------------------------------------------------------------------------- */

int METH(test, main) (int argc, char *argv[]) {
  testCalled = 1;
  return 5;
}

/* -----------------------------------------------------------------------------
   Implementation of the testCollection interface.
----------------------------------------------------------------------------- */

int METH(testCollection[0], main) (int argc, char *argv[]) {
  testCollection0Called = 1;
  return 0;
}

int METH(testCollection[1], main) (int argc, char *argv[]) {
  testCollection1Called = 1;
  return 0;
}

int METH(testCollection[2], main) (int argc, char *argv[]) {
  testCollection2Called = 1;
  return 0;
}
