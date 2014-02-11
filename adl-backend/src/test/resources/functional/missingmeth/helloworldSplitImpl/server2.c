#include <stdio.h>

/* -----------------------------------------------------------------------------
   Partial implementation of the service interface.
----------------------------------------------------------------------------- */


void METH(s, println)(const char *msg)
{
	printf("%s\n", msg);
}

/* We want an error to be raised and flush to be missing
void METH(s, flush)(void)
{
  // Nothing to do...
}*/
