
/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){

  /* call the 'print' method of the 'sa' client interface. */
  CALL(sa, print)("hello world");

  /* call again the same method to look at invocation count */
  CALL(sa, println)("hello world");


  return 0;
}
