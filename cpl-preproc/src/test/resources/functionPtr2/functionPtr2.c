#include "data.h"

// declaration of the private method.
int METH(myPrivateMethod)(int a);
int METH(myOtherPrivateMethod)(int a);


struct s {
	int (* METH_PTR(f))(int a);
} aStruct = { METH(myPrivateMethod) };




int METH(myItf, myMethod)(int a, int b) {
	// f_tab is an array of 2 pointers to the two private methods
	int (* METH_PTR((f_tab)[2]))(int a) = { METH(myPrivateMethod), METH(myOtherPrivateMethod) };
	PRIVATE.a = a;
	PRIVATE.b = b;
	CALL_PTR(aStruct.f)(b);
	return CALL_PTR(f_tab[1])(b);
}

int METH(myPrivateMethod)(int a) {
	return PRIVATE.a + a;
}

int METH(myOtherPrivateMethod)(int a) {
	return PRIVATE.a + a;
}
