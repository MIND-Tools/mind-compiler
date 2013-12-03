#include "data.h"

#define NULL ((void *) 0)

int METH(myItf0, myMeth0)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return 0;
}
