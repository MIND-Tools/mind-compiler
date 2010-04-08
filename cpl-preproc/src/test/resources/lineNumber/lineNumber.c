#include "data.h"

// declaration of the private method.
int
METH
(
    myPrivateMethod
)
(
    int a
);
#if (__LINE__ != 12)
#error bad line number
#endif

CONSTRUCTOR
(
    void
)
#if (__LINE__ != 20)
#error bad line number
#endif
{
  PRIVATE
  .
  a
  =
  0
  ;
  PRIVATE
  .
  b
  =
  0
  ;
#if (__LINE__ != 36)
#error bad line number
#endif
}

int
METH
(
    myItf
    ,
    myMethod
)
(
    int
    a
    ,
    int
    b
)
#if (__LINE__ != 55)
#error bad line number
#endif
{
	PRIVATE
	.
	a
	=
	    a;
#if (__LINE__ != 64)
#error bad line number
#endif
	PRIVATE
	.
	b
	=
	    b;
#if (__LINE__ != 72)
#error bad line number
#endif
	return CALL
	(
	    myPrivateMethod
    )
    (
        b
    )
    ;
#if (__LINE__ != 83)
#error bad line number
#endif
}

int
METH
(
    myPrivateMethod
)
(
    int
    a
)
#if (__LINE__ != 97)
#error bad line number
#endif
{
	return PRIVATE.a + a;
}
#if (__LINE__ != 103)
#error bad line number
#endif
