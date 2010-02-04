#include "data.h"

int f(int a) {
	return a+1;
}

int (* g(int b))(int a) {
	b = b+1;
	return f;
}

int (* (*g_ptr)(int b))(int a) = g;

// declaration of the private method.
int METH(myPrivateMethod)(int a);

// declaration of the private method that take an int and return a pointer to
// a private method that take an int and return an int;
int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(int a);

int METH(myItf, myMethod)(int a, int b) {
	// f is a pointer to the myOtherPrivateMethod private method
	int (* METH_PTR((* METH_PTR(f))(int a)))(int a) = METH(myOtherPrivateMethod);
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL_PTR(CALL_PTR(f)(b))(a);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}

int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(int a) {
	PRIVATE.a += a;
	// return a pointer to the "myPrivateMethod" private method
	return METH(myPrivateMethod);
}
