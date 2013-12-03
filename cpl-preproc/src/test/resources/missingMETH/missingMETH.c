#include "data.h"

#define NULL ((void *) 0)

// what it should be: int METH(myItf, myMethod)(int a, int b) {
int myMethod(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return 0;
}
