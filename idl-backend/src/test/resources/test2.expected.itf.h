|#ifndef TEST2_H
#define TEST2_H

/* This file is a generated file, do not edit. */

#include "mindcommon.h"

#include "test1.itf.h"




#include <stdarg.h> 

struct __component_test2_vTable {
  void (* (* m1)(void *_mind_this , const uint8_t a, int (*b) )) ;
  unsigned float (* m2)(void *_mind_this , float f1 ) ;
  void (* (* m3)(void *_mind_this , struct s (* s_ptr), __MIND_STRING_TYPEDEF s )) ;
  void (* m4)(void *_mind_this , int a , ...) ;
};

struct __component_test2_itf_desc {
  void *selfData;
  struct __component_test2_vTable *meths;
};
typedef struct __component_test2_itf_desc *test2;

#endif /* TEST2_H */
