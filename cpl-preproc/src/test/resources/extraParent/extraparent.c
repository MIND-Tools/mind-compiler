#include "data.h"

/* declaration of the private method. */
int METH(myPrivateMethod)(int a);

/* add extra parenthesis around METH method declarator */
int (METH(myItf, myMethod))(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL(myPrivateMethod)(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}
