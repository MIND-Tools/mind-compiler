#include "data.h"

#define NULL ((void *) 0)

/* declaration of the private method. */
int METH(myPrivateMethod)(void* pointer, ...);

int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(void* pointer, ...);

int METH(myItf, myMethod)(int a, int b) {
	int (* METH_PTR(f))(void* pointer, ...);
	METH_PTR(f) = METH(myPrivateMethod);
	
	/* invoke method pointer without 'CALL' construct. */
	f(NULL, &(PRIVATE.a)); /* cannot be detected at compile-time => mind "exception" at runtime */

	/* should be :
	   CALL_PTR(f)(NULL, &(PRIVATE.a)); */

	
	CALL(myOtherPrivateMethod)(1)(NULL, &(PRIVATE.a)); /* cannot be detected at compile-time => mind "exception" at runtime */
	/* should be :
	   CALL_PTR(CALL(myOtherPrivateMethod)(1))(NULL, &(PRIVATE.a));
	   ======================== */
	
	return 0;
}

int METH(myPrivateMethod)(void* pointer, ...) {
	return PRIVATE.a;
}

int (* METH_PTR(METH(myOtherPrivateMethod)(int a)))(void * pointer, ...) {
	PRIVATE.a += a;
	/* return a pointer to the "myPrivateMethod" private method */
	return METH(myPrivateMethod);
}
