#include "data.h"

// declaration of two private methods in the same statement.
int METH(myPrivateMethod)(int a), METH(myOtherPrivateMethod)(int b);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL(myPrivateMethod)(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}

int METH(myOtherPrivateMethod)(int b) {
	return PRIVATE.b + b;
}
