#include <stdio.h>

/* -----------------------------------------------------------------------------
   Implementation of the service interface.
----------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s, print)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s", msg);
  }

  printf("Server: print done\n");
}
