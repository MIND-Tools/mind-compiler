#include "data.h"

/* declaration of the private method. */
static int myPrivateFunction(int a);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return myPrivateFunction(b);
}

static int myPrivateFunction(int a) {
	return PRIVATE.a + a;
}
