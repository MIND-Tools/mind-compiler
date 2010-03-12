#include <assert.h>
#include <string.h>

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

#define NB_ATTR 3

const char * expectedNames[NB_ATTR] = { "attr1", "attr2", "attr3" };
int expectedIntValues[NB_ATTR] = { 0, 4, 0 };
const char * expectedStringValues[NB_ATTR] = { "", "", "toto" };
int expectedSizes[NB_ATTR] = { sizeof(int), sizeof(int), sizeof(char *) };
enum AttributeType expectedTypes[NB_ATTR] = {INT_ATTR_TYPE, INT_ATTR_TYPE, STRING_ATTR_TYPE};

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]) {
  int nbAttr, err, i;
  const char *attrNames[NB_ATTR];

  nbAttr = CALL(testedAC, listFcAttributes)(NULL);
  assert(nbAttr == NB_ATTR);

  err = CALL(testedAC, listFcAttributes)(attrNames);
  assert(err == NB_ATTR);

  for (i = 0; i < NB_ATTR; i++) {
    enum AttributeType type;
    assert(attrNames[i] != NULL);
    assert(strcmp(attrNames[i], expectedNames[i]) == 0);

    assert(CALL(testedAC, getFcAttributeSize)(attrNames[i]) == expectedSizes[i]);
    assert(CALL(testedAC, getFcAttributeType)(attrNames[i], &type) == FRACTAL_API_OK);
    assert(type == expectedTypes[i]);

    if (expectedTypes[i] == INT_ATTR_TYPE) {
      int attrValue;
      assert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);
      assert(attrValue == expectedIntValues[i]);
    } else {
      char * attrValue;
      assert(expectedTypes[i] == STRING_ATTR_TYPE);
      assert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);
      assert(strcmp(attrValue, expectedStringValues[i]) == 0);
    }

  }

  assert(CALL(testedMain, main)(argc, argv) == 4);

  {
    int attrValue;
    assert(CALL(testedAC, setFcAttribute)("attr1", (void *) 3) == FRACTAL_API_OK);
    assert(CALL(testedAC, getFcAttribute)("attr1", (void **)&attrValue) == FRACTAL_API_OK);
    assert(attrValue == 3);
  }

  assert(CALL(testedMain, main)(argc, argv) == 7);

  return 0;
}
