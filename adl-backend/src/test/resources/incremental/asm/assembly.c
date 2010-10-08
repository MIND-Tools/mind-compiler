
#include "mindassert.h"

// supposed to be defines in assembly code.
extern int myGlobalInt;

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){

  mindassert(myGlobalInt == 0xaaaaaaaa);

  return 0;
}
