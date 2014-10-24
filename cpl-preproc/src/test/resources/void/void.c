#include "data.h"

/* declaration of the private method. */
int METH(myPrivateMethod)(void);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL(myPrivateMethod)();
}

int METH(myPrivateMethod)(void) {
	return PRIVATE.a + PRIVATE.b;
}
