#include "data.h"

// declaration of the private method.
int METH(myPrivateMethod)(int a);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE[0].a = a;
	PRIVATE[0].b = b;
	return CALL(myPrivateMethod)(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE[0].a + a;
}
