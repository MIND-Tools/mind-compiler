
#define MAGIC_NUM 0xab378dc

CONSTRUCTOR() {
	PRIVATE.a = MAGIC_NUM;
}

// -----------------------------------------------------------------------------
// Implementation of the main interface.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(main, main) (int argc, char *argv[]){
  if (PRIVATE.a == MAGIC_NUM)
	return 0;
  else
	return 1;
}
