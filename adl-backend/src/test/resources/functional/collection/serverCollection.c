#include <stdio.h>

/* -----------------------------------------------------------------------------
   Implementation of the service interface.
----------------------------------------------------------------------------- */

/* void print(string msg) */
void METH(s[0], print)(const char *msg)
{
  int i;

  printf("Server0: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s", msg);
  }

  printf("Server0: print done\n");
}

void METH(s[0], println)(const char *msg)
{
  int i;

  printf("Server0: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s\n", msg);
  }

  printf("Server0: print done\n");
}


void METH(s[0], flush)(void)
{
  /* Nothing to do... */
}



void METH(s[1], print)(const char *msg)
{
  int i;

  printf("Server1: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s", msg);
  }

  printf("Server1: print done\n");
}

void METH(s[1], println)(const char *msg)
{
  int i;

  printf("Server1: begin printing...\n");
  for (i = 0; i < ATTR(count); ++i) {
    printf("%s\n", msg);
  }

  printf("Server1: print done\n");
}


void METH(s[1], flush)(void)
{
  /* Nothing to do... */
}
