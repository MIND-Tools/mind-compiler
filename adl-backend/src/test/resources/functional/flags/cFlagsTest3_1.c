
#ifndef DEFINITION_LEVEL_FLAG
#error "DEFINITION_LEVEL_FLAG macro is not defined"
#endif

#if (DEFINITION_LEVEL_FLAG != 1)
#error "DEFINITION_LEVEL_FLAG macro is not defined correctly"
#endif

#ifndef SOURCE_LEVEL_FLAG_1
#error "SOURCE_LEVEL_FLAG_1 macro is not defined"
#endif

#if (SOURCE_LEVEL_FLAG_1 != 1)
#error "SOURCE_LEVEL_FLAG_1 macro is not defined correctly"
#endif

#ifdef SOURCE_LEVEL_FLAG_2
#error "SOURCE_LEVEL_FLAG_2 macro should not be defined"
#endif

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]){
  return 0;
}
