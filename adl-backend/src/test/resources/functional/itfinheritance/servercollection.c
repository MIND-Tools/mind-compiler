#include <stdio.h>

/* -----------------------------------------------------------------------------
   Implementation of the service interface.
----------------------------------------------------------------------------- */

//------------------------
// Collection element 0
//------------------------

/* void print(string msg) */
void METH(s[0], print)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s", msg);
  }

  printf("Server: print done\n");
  CALL(s[0], flush)();
}

void METH(s[0], println)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    CALL(s[0], print)(msg);
    CALL(s[0], print)("\n");
  }

  printf("Server: print done\n");
}


void METH(s[0], flush)(void)
{
  /* Nothing to do... */
}

void METH(s[0], resetCount)(void) {
	ATTR(count) = 2;
}

//------------------------
// Collection element 1
//------------------------

/* void print(string msg) */
void METH(s[1], print)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s", msg);
  }

  printf("Server: print done\n");
  CALL(s[1], flush)();
}

void METH(s[1], println)(const char *msg)
{
  int i;

  printf("Server: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    CALL(s[1], print)(msg);
    CALL(s[1], print)("\n");
  }

  printf("Server: print done\n");
}


void METH(s[1], flush)(void)
{
  /* Nothing to do... */
}

void METH(s[1], resetCount)(void) {
	ATTR(count) = 2;
}
