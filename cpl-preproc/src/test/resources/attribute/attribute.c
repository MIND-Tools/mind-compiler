#include "data.h"

/* declaration of the private method. */
void (__attribute__((noreturn)) METH(myPrivateMethod))(int a);

void METH(myItf, myMethod)(int a, int b) {
  PRIVATE.a = a;
  PRIVATE.b = b;
  CALL(myPrivateMethod)(b);
}

void (__attribute__((noreturn)) METH(myPrivateMethod))(int a) {
  while (1) {
    PRIVATE.a += a;
  }
}
