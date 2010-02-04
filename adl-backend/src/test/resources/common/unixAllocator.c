#include <stdlib.h>

// -----------------------------------------------------------------------------
// Implementation of the allocator interface.
// -----------------------------------------------------------------------------

void *METH(allocator, alloc)(int size) {
  return malloc(size);
}

void METH(allocator, free)(void *addr) {
  free(addr);
}

