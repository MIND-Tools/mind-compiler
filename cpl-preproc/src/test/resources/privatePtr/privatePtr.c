#include "data.h"

// declaration of the private method.
int METH(myPrivateMethod)(struct s *ptr, int a);

int METH(myItf, myMethod)(int a, int b) {
	(&PRIVATE)->a = a;
	PRIVATE.b = b;
	// cast (struct s *) is required because private is not declared as
	// "struct s {int a, b;} RPIVATE"
	return CALL(myPrivateMethod)((struct s *)&PRIVATE, 0);
}

int METH(myPrivateMethod)(struct s *ptr, int a) {
	return ptr->a + a;
}
