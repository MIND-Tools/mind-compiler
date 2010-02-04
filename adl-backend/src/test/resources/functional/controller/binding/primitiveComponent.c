
// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]) {
  CALL(clientCollectionMain[0], main) (argc, argv);
  CALL(clientCollectionMain[1], main) (argc, argv);
  CALL(clientCollectionMain[2], main) (argc, argv);
  return CALL(clientMain, main) (argc, argv);
}
