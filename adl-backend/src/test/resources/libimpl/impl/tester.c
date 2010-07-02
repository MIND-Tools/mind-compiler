#include <assert.h>

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){

  assert(CALL(myLib, add)(3, 4) == 7);
  return 0;
}
