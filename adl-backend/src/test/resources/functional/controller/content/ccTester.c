#include <assert.h>
#include <string.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main)(int argc, char *argv[]) {
  int nbSubComp, err, i, initialSubCompFound;
  fractal_api_Component initialSubComp;
  fractal_api_Component subComps[2];
  void *delegator;

  nbSubComp = CALL(testedCC, getFcSubComponents)(NULL);
  assert(nbSubComp == 1);

  err = CALL(testedCC, getFcSubComponents)(&initialSubComp);
  assert(err == 1);
  assert(initialSubComp != NULL);

  // create a delegator component using factory
  err = CALL(delegatorFactory, newFcInstance)(&delegator);
  assert(err == 0);

  // add it as a sub-component of testedCC
  err = CALL(testedCC, addFcSubComponent)((fractal_api_Component) delegator);
  assert(err == 0);

  err = CALL(testedCC, getFcSubComponents)(NULL);
  assert(err == 2);

  err = CALL(testedCC, getFcSubComponents)(subComps);
  assert(err == 2);

  initialSubCompFound = 0;
  for (i = 0; i < 2; i++) {
    assert(subComps[i] != NULL);

    if (subComps[i] == initialSubComp) {
      assert(initialSubCompFound == 0);
      initialSubCompFound = 1;
    } else {
      assert(subComps[i] == delegator);
    }
  }

  assert(initialSubCompFound == 1);

  // try to readd the same a second time. should return an error
  err = CALL(testedCC, addFcSubComponent)((fractal_api_Component) delegator);
  assert(err != 0);

  err = CALL(testedCC, getFcSubComponents)(NULL);
  assert(err == 2);

  // unbind initial sub-comp
  assert(IS_BOUND(testedMain));
  err = CALL(testedCC, removeFcSubBinding)(NULL, "main");
  assert(err == 0);
  assert(! IS_BOUND(testedMain));

  // bind delegator
  err = CALL(testedCC, addFcSubBinding)(NULL, "main",
      (fractal_api_Component) delegator, "main");
  assert(err == 0);
  assert(IS_BOUND(testedMain));

  err = CALL(testedCC, addFcSubBinding)((fractal_api_Component) delegator,
      "delegator", initialSubComp, "main");
  assert(err == 0);
  assert(IS_BOUND(testedMain));

  err = CALL(testedMain, main)(argc, argv);
  assert(err == 0);

  return 0;
}
