#ifndef _FOO_TEST2_IDT_H
#define _FOO_TEST2_IDT_H

/* This file is a generated file, do not edit. */

#include "mindcommon.h"

#include "test3.idt.h"
#include "foo/foo.h"
#include "test2.itf.h"

typedef enum  {
  A = 0,
  B = 1
} myEnum;

typedef struct s myStruct;

typedef test2 test2ItfRef;

typedef struct my {
  int a;
  int b;
} my_s;

typedef struct my (* my_s_ptr);


#endif /* _FOO_TEST2_IDT_H */
