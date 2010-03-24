#ifndef TEST1_H
#define TEST1_H

/* This file is a generated file, do not edit. */

#include "mindcommon.h"

#include <stdint.h>
#include "foo/test2.idt.h"

typedef const int myint;

typedef const int (* const mypointer)[];

struct s {
  myint a;
  myint b;
  int c[10][20];
  int d[10][20];
  int (* e[30]);
};

struct __component_test1_vTable {
  void (* (* m1)(void *_mind_this   , const uint8_t a, int (*b) )) ;
  unsigned float (* m2)(void *_mind_this   , float f1 ) ;
};

struct __component_test1_itf_desc {
  void *selfData;
  struct __component_test1_vTable *meths;
};
typedef struct __component_test1_itf_desc *test1;

#endif /* TEST1_H */
