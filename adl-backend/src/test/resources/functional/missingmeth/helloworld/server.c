#include <stdio.h>

/* -----------------------------------------------------------------------------
   Partial implementation of the service interface.
----------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s, print)(const char *msg)
{
    printf("%s\n", msg);
}

/* We want an error to be raised and println and flush to be missing
void METH(s, println)(const char *msg)
{
	printf("%s\n", msg);
}


void METH(s, flush)(void)
{

}*/
