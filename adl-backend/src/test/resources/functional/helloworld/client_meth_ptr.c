
void METH(call_print)(string s);

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){

  void (* METH_PTR(call_print_ptr))(string s);
  call_print_ptr = METH(call_print);

  /* call the 'print' method of the 'sa' client interface. */
  CALL_PTR(call_print_ptr)("hello world");

  /* call again the same method to look at invocation count */
  CALL(sa, println)("hello world");


  return 0;
}

void METH(call_print)(string s) {
  CALL(sa, print)(s);
}
