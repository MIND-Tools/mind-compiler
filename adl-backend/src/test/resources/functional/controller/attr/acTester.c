#include <assert.h>
#include <string.h>

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

#define NB_ATTR 2

const char * expectedNames[NB_ATTR] = { "attr1", "attr2" };
int expectedValues[NB_ATTR] = { 0, 4 };
int expectedSizes[NB_ATTR] = { 4, 4 };

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]) {
  int nbAttr, err, i;
  const char *attrNames[NB_ATTR];
  int attrValue;

  nbAttr = CALL(testedAC, listFcAttributes)(NULL);
  assert(nbAttr == NB_ATTR);

  err = CALL(testedAC, listFcAttributes)(attrNames);
  assert(err == NB_ATTR);

  for (i = 0; i < NB_ATTR; i++) {
    assert(attrNames[i] != NULL);
    assert(strcmp(attrNames[i], expectedNames[i]) == 0);

    assert(CALL(testedAC, getFcAttributeSize)(attrNames[i]) == expectedSizes[i]);

    assert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);

    assert(attrValue == expectedValues[i]);
  }

  assert(CALL(testedMain, main)(argc, argv) == 4);

  assert(CALL(testedAC, setFcAttribute)("attr1", (void *) 3) == FRACTAL_API_OK);
  assert(CALL(testedAC, getFcAttribute)("attr1", (void **)&attrValue) == FRACTAL_API_OK);
  assert(attrValue == 3);

  assert(CALL(testedMain, main)(argc, argv) == 7);

  return 0;
}
