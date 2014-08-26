#include <stdio.h>

/* -----------------------------------------------------------------------------
   Partial implementation of the service interface.
----------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s, print)(const char *msg)
{
    printf("%s\n", msg);
}
