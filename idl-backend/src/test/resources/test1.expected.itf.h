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
  int (* e[(- (2))+((5)+(((uint8_t ) 0xff)))]);
};

struct __component_test1_vTable {
  void (* (* m1)(void *_mind_this , const uint8_t a, int (*b) )) ;
  unsigned float (* m2)(void *_mind_this , float f1 ) ;
};

struct __component_test1_itf_desc {
  void *selfData;
  struct __component_test1_vTable *meths;
  void *isBound;
};
typedef struct __component_test1_itf_desc *test1;

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


#endif /* TEST1_H */
