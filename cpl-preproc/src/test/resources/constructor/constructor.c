#include "data.h"

CONSTRUCTOR(void) {
	PRIVATE.a = 0;
}

DESTRUCTOR(void) {
	PRIVATE.a = 0;
}

// declaration of the private method.
int METH(myPrivateMethod)(int a);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL(myPrivateMethod)(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}
