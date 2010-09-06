#include <mindassert.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){

  mindassert(GET_COLLECTION_SIZE(sa) == 2);

  /* call the 'print' method of the 'sa' client interface. */
  CALL(sa[0], print)("hello world");

  /* call again the same method to look at invocation count */
  CALL(sa[1], println)("hello world");

  return 0;
}
