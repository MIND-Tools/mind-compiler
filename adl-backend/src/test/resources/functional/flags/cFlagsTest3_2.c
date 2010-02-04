
#ifndef DEFINITION_LEVEL_FLAG
#error "DEFINITION_LEVEL_FLAG macro is not defined"
#endif

#if (DEFINITION_LEVEL_FLAG != 1)
#error "DEFINITION_LEVEL_FLAG macro is not defined correctly"
#endif

#ifndef SOURCE_LEVEL_FLAG_2
#error "SOURCE_LEVEL_FLAG_2 macro is not defined"
#endif

#if (SOURCE_LEVEL_FLAG_2 != 1)
#error "SOURCE_LEVEL_FLAG_2 macro is not defined correctly"
#endif

#ifdef SOURCE_LEVEL_FLAG_1
#error "SOURCE_LEVEL_FLAG_1 macro should not be defined"
#endif

