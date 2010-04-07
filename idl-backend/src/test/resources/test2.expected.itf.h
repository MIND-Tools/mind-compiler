#ifndef TEST2_H
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
  void *isBound;
};
typedef struct __component_test2_itf_desc *test2;

#ifndef __COMPONENT_IS_BOUND_DEFINED
#define __COMPONENT_IS_BOUND_DEFINED

struct __component_generic_itf_desc {
  void *selfData;
  void *meth;
  void *isBound;
};

__MIND_ATTRIBUTE_UNUSED
static __MIND_INLINE int __component_is_bound(void *itfPtr) {
  if (itfPtr == NULL) {
    return 0;
  }
  while (((struct __component_generic_itf_desc *) itfPtr)->isBound != NULL
     && ((struct __component_generic_itf_desc *) itfPtr)->isBound 
        != ((struct __component_generic_itf_desc *) itfPtr)->selfData) {
    itfPtr = ((struct __component_generic_itf_desc *) itfPtr)->isBound;
  }
  if (((struct __component_generic_itf_desc *) itfPtr)->isBound == NULL) {
    return 0;
  } else {
    return 1;
  }
}

#endif


#endif /* TEST2_H */
