#include "data.h"

#define NULL ((void *) 0)

int foo(void *pointer);

int METH(myItf, myMethod)(int a, int b) {
	/* invoke the foo function as if it was a private method. */
	return CALL(foo)(); /* accesToThis.c:9: warning: implicit declaration of function ‘__component_accesToThis_private_foo’ + link error */
}

int foo(void *pointer) {
	/* have access to the "_mind_this" pointer */
	*((int *) pointer) = 0;
	return 3;
}
