#include <stdio.h>

/* -----------------------------------------------------------------------------
   Partial implementation of the service interface.
----------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s[0], print)(const char *msg)
{
    printf("%s\n", msg);
}

/* void print(string msg) */
void METH(s[1], print)(const char *msg)
{
    printf("%s\n", msg);
}

/* We want an error to be raised and println and flush to be missing for both collection indexes
void METH(s[0], println)(const char *msg)
{
	printf("%s\n", msg);
}


void METH(s[0], flush)(void)
{
  // Nothing to do...
}


void METH(s[1], println)(const char *msg)
{
	printf("%s\n", msg);
}


void METH(s[1], flush)(void)
{
  // Nothing to do...
}*/

