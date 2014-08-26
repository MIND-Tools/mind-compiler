#include "data.h"

#define NULL ((void *) 0)

int METH(myItf1, myMeth1)(int c) {
	PRIVATE.c = c;
	return 0;
}
