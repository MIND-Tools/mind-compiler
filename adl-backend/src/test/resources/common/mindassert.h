#include <stdio.h>

#define mindassert(expr) \
  ((expr) \
   ? (void) 0 \
   : printf("%s:%d Assertion %s failed", __FILE__, __LINE__, #expr))
