#ifndef PRIVATE_DATA_DEF_C
#define PRIVATE_DATA_DEF_C

#include "macro_def.h"

/**
 * Used to check which test is currently being processed. "extern" and "privateArray" both
 * have special behavior needing special handling.
 */
#define extern 1
#define privateArray 2

#if (COMPONENT_NAME == extern)
// do nothing, and cleanup
#undef extern
#elif (COMPONENT_NAME == privateArray)
/*
 * Duplicate-definition bug fix for some families of compilers (such as IAR)
 * singleton_instance is defined in the according .c file, used after the Mind Preprocessing
 * The typedef is redundant, however we have to "redefine" it here since the header doesn't
 * know the SINGLETON_PRIVATE_DATA_T type before Mind Preprocessing (MPP) (and CPP is used before
 * on the sources to generate .i files without the SINGLETON_PRIVATE_DATA_T type, leading to an error).
 */
#ifndef SINGLETON_PRIVATE_DATA
#define SINGLETON_PRIVATE_DATA
typedef struct {
 int a, b;
}  SINGLETON_PRIVATE_DATA_T[0];
#endif /* SINGLETON_PRIVATE_DATA */

SINGLETON_PRIVATE_DATA_T PRIVATE;

// Note: we #undef privateArray in the end just in case
#else
/*
 * Duplicate-definition bug fix for some families of compilers (such as IAR)
 * singleton_instance is defined in the according .c file, used after the Mind Preprocessing
 * The typedef is redundant, however we have to "redefine" it here since the header doesn't
 * know the SINGLETON_PRIVATE_DATA_T type before Mind Preprocessing (MPP) (and CPP is used before
 * on the sources to generate .i files without the SINGLETON_PRIVATE_DATA_T type, leading to an error).
 */
#ifndef SINGLETON_PRIVATE_DATA
#define SINGLETON_PRIVATE_DATA
typedef struct {
 int a, b;
}  SINGLETON_PRIVATE_DATA_T;
#endif /* SINGLETON_PRIVATE_DATA */

// cleanup and COMPONENT_NAME being used to calculate PRIVATE we need to reset it
// otherwise the variable would be called '__component_2_private_data' instead of '__component_privateArray_private_data'
#undef privateArray

SINGLETON_PRIVATE_DATA_T PRIVATE;

#endif /* $IS_NOT_EXTERN == 0 */

#endif /* PRIVATE_DATA_DEF_C */
