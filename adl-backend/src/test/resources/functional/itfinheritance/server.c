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
  CALL(s, flush)();
}

void METH(s, println)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    CALL(s, print)(msg);
    CALL(s, print)("\n");
  }

  printf("Server: print done\n");
}


void METH(s, flush)(void)
{
  /* Nothing to do... */
}

void METH(s, resetCount)(void) {
	ATTR(count) = 2;
}
