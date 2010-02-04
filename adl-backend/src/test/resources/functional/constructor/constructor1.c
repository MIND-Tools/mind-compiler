
int a = 0;

CONSTRUCTOR() {
	a = 1;
}

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]){
  if (a == 1)
	return 0;
  else
	return 1;
}
