#ifndef PRIVATE_DATA_DEF_C
#define PRIVATE_DATA_DEF_C

#include "macro_def.h"

/**
 * "Trick" to check which test is currently being processed. "extern" and "privateArray" both
 * have special behavior needing special handling.
 */
#define extern 1
#define privateArray 2

/************************************** Configuration **************************************/
#if (COMPONENT_NAME == extern)
/* do not declare a "SINGLETON_PRIVATE_DATA_T PRIVATE;" variable:
   symbol is already defined in extern/data.h: "extern struct s PRIVATE;" */
#undef extern
#elif (COMPONENT_NAME == privateArray)
/* The privateArray test case is the only one using... an array, instead of the usual struct. */
#ifndef SINGLETON_PRIVATE_DATA
#define SINGLETON_PRIVATE_DATA
typedef struct {
	int a, b;
}  SINGLETON_PRIVATE_DATA_T[0];
#endif /* SINGLETON_PRIVATE_DATA */

/* The PRIVATE data variable */
SINGLETON_PRIVATE_DATA_T PRIVATE;

#else
/* Nominal case for all tests except "extern" and "privateArray". */
#ifndef SINGLETON_PRIVATE_DATA
#define SINGLETON_PRIVATE_DATA
typedef struct {
	int a, b, c;
}  SINGLETON_PRIVATE_DATA_T;
#endif /* SINGLETON_PRIVATE_DATA */

/* cleanup, and as COMPONENT_NAME is being used to calculate PRIVATE, we need to reset it
 otherwise the variable would be called '__component_2_private_data' instead of '__component_privateArray_private_data' */
#undef privateArray

/* The PRIVATE data variable */
SINGLETON_PRIVATE_DATA_T PRIVATE;

#endif /* $IS_NOT_EXTERN == 0 */

#endif /* PRIVATE_DATA_DEF_C */
