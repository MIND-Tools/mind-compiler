#include <mindassert.h>
#include <string.h>
#include "attrType.h"

#define NB_ATTR 5

const char * expectedNames[NB_ATTR] = { "attr1", "attr2", "attr3", "attr5", "attr4" /* uninitialized attributes are moved at the end */};
int expectedIntValues[NB_ATTR] = { 0, 4, 0, 0, 0 };
const char * expectedStringValues[NB_ATTR] = { "", "", "toto", "", "" };
struct s1 expectedStructValues[NB_ATTR] = { {0,0}, {0,0}, {0,0}, {1,2}, {0, 0} };
int expectedSizes[NB_ATTR] = { sizeof(int), sizeof(int), sizeof(char *), sizeof(struct s1), sizeof(struct s1)};
enum AttributeType expectedTypes[NB_ATTR] = {INT_ATTR_TYPE, INT_ATTR_TYPE, STRING_ATTR_TYPE, UNKNOWN_ATTR_TYPE, UNKNOWN_ATTR_TYPE};

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  int nbAttr, err, i;
  const char *attrNames[NB_ATTR];

  nbAttr = CALL(testedAC, listFcAttributes)(NULL);
  mindassert(nbAttr == NB_ATTR);

  err = CALL(testedAC, listFcAttributes)(attrNames);
  mindassert(err == NB_ATTR);

  for (i = 0; i < NB_ATTR; i++) {
    enum AttributeType type;
    mindassert(attrNames[i] != NULL);
    mindassert(strcmp(attrNames[i], expectedNames[i]) == 0);

    mindassert(CALL(testedAC, getFcAttributeSize)(attrNames[i]) == expectedSizes[i]);
    mindassert(CALL(testedAC, getFcAttributeType)(attrNames[i], &type) == FRACTAL_API_OK);
    mindassert(type == expectedTypes[i]);

    if (expectedTypes[i] == INT_ATTR_TYPE) {
      int attrValue;
      mindassert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);
      mindassert(attrValue == expectedIntValues[i]);
    } else if (expectedTypes[i] == STRING_ATTR_TYPE) {
      char * attrValue;
      mindassert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);
      mindassert(strcmp(attrValue, expectedStringValues[i]) == 0);
    } else if (expectedTypes[i] == UNKNOWN_ATTR_TYPE) {
      struct s1 attrValue;
      mindassert(CALL(testedAC, getFcAttribute)(attrNames[i], (void **)&attrValue) == FRACTAL_API_OK);
      mindassert(attrValue.a == expectedStructValues[i].a);
      mindassert(attrValue.b == expectedStructValues[i].b);
    }

  }

  mindassert(CALL(testedMain, main)(argc, argv) == 4);

  {
    int attrValue;
    mindassert(CALL(testedAC, setFcAttribute)("attr1", (void *) 3) == FRACTAL_API_OK);
    mindassert(CALL(testedAC, getFcAttribute)("attr1", (void **)&attrValue) == FRACTAL_API_OK);
    mindassert(attrValue == 3);
  }

  mindassert(CALL(testedMain, main)(argc, argv) == 7);

  return 0;
}
