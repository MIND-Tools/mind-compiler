#include <stdio.h>

/* -----------------------------------------------------------------------------
   Implementation of the service interface.
----------------------------------------------------------------------------- */

void METH(s, println)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s\n", msg);
  }

  printf("Server: print done\n");
}


void METH(s, flush)(void)
{
  /* Nothing to do... */
}
