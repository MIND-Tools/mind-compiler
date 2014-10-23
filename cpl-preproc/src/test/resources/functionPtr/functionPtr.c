#include "data.h"

/* declaration of the private method. */
int METH(myPrivateMethod)(int a);

/* declaration of the private method that take an int and return a pointer to
   a private method that take an int and return an int; */
int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(int a);

int METH(myItf, myMethod)(int a, int b) {
	int (* METH_PTR(f))(int a);
	PRIVATE.a = a;
	PRIVATE.b = b;
	f = CALL(myOtherPrivateMethod)(a);
	return CALL_PTR(f)(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}

int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(int a) {
	PRIVATE.a += a;
	/* return a pointer to the "myPrivateMethod" private method */
	return METH(myPrivateMethod);
}
