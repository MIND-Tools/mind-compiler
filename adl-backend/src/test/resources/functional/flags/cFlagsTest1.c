
#ifndef SOURCE_LEVEL_FLAG
#error "SOURCE_LEVEL_FLAG macro is not defined"
#endif

#if (SOURCE_LEVEL_FLAG != 1)
#error "SOURCE_LEVEL_FLAG macro is not defined correctly"
#endif

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]){
  return 0;
}
