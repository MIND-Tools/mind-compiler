
// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]){
  int err;
  void *instance;
  err = CALL(factory, newFcInstance)(&instance);
  if (err != 0) return err;

  // cast instance as a pointer to a 'main' interface, and call the 'main' method
  err = CALL_PTR((Main) instance, main)(argc, argv);
  if (err != 0) return err;

  err = CALL(factory, destroyFcInstance)(instance);
  if (err != 0) return err;

  return 0;
}
