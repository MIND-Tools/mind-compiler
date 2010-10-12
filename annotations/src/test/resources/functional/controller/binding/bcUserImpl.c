#include <string.h>

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  CALL(clientCollectionMain[0], main) (argc, argv);
  CALL(clientCollectionMain[1], main) (argc, argv);
  CALL(clientCollectionMain[2], main) (argc, argv);
  return CALL(clientMain, main) (argc, argv);
}


/* -----------------------------------------------------------------------------
   Implementation of the bindingController interface.
----------------------------------------------------------------------------- */

int METH(bindingController, listFc)(string clientItfNames[])
{
  if (clientItfNames != NULL) {
    clientItfNames[0] = "clientMain";
    clientItfNames[1] = "clientCollectionMain[0]";
    clientItfNames[2] = "clientCollectionMain[1]";
    clientItfNames[3] = "clientCollectionMain[2]";
  }
  return 4;
}

int METH(bindingController, lookupFc)(string clientItfName,
    void **interfaceReference)
{
  if (strcmp(clientItfName, "clientMain") == 0) {
    *interfaceReference = GET_MY_INTERFACE(clientMain);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[0]") == 0) {
    *interfaceReference = GET_MY_INTERFACE(clientCollectionMain[0]);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[1]") == 0) {
    *interfaceReference = GET_MY_INTERFACE(clientCollectionMain[1]);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[2]") == 0) {
    *interfaceReference = GET_MY_INTERFACE(clientCollectionMain[2]);
    return FRACTAL_API_OK;
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}

int METH(bindingController, bindFc)(string clientItfName, void *serverItf)
{
  if (strcmp(clientItfName, "clientMain") == 0) {
    BIND_MY_INTERFACE(clientMain, serverItf);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[0]") == 0) {
    BIND_MY_INTERFACE(clientCollectionMain[0], serverItf);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[1]") == 0) {
    BIND_MY_INTERFACE(clientCollectionMain[1], serverItf);
    return FRACTAL_API_OK;
  } else if (strcmp(clientItfName, "clientCollectionMain[2]") == 0) {
    BIND_MY_INTERFACE(clientCollectionMain[2], serverItf);
    return FRACTAL_API_OK;
  }
  return FRACTAL_API_NO_SUCH_INTERFACE;
}

int METH(bindingController, unbindFc)(string clientItfName)
{
  return CALL(bindingController, bindFc)(clientItfName, NULL);
}
