#include "data.h"

// declaration of the private method.
int METH(myPrivateMethod)(int a);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return myPrivateMethod(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}
