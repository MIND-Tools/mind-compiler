#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <mindassert.h>

#define CONTROLLED_FLAG "--controlled"
#define FACTORY_FLAG "--factory"
#define FACTORY_OF_CONTROLLED_FLAG "--factoryOfControlled"

typedef struct
{
  struct __component_fractal_api_Factory_itf_desc factoryItf;
  struct __component_memory_api_Allocator_itf_desc allocatorItf;
} factory_type;

static int METH(checkComponent)(fractal_api_Component compItf);
static void printUsage(void);

/* -----------------------------------------------------------------------------
   Implementation of the main interface.
----------------------------------------------------------------------------- */

/* int main(int argc, string[] argv) */
int METH(main, main) (int argc, char *argv[]) {
  int factory=0, controlled=0, factoryOfControlled=0;
  char *libPath;

  void *soHandle;
  char *soError;
  void **compSymbol;

  if (argc < 2 || argc > 5) {
    printUsage();
    return 1;
  }

  if (argc == 2) {
    factory = 0;
    libPath = argv[1];
  } else {
    int i;
    for (i = 1; i < argc; i ++) {
      if (argv[i][0] == '-') {
        if (strcmp(argv[1], FACTORY_FLAG) == 0)
          factory = 1;
        else if (strcmp(argv[1], CONTROLLED_FLAG) == 0)
          controlled = 1;
        else if (strcmp(argv[1], FACTORY_OF_CONTROLLED_FLAG) == 0) {
          factory = 1;
          factoryOfControlled = 1;
        }
      } else {
        if (i != argc -1) {
          printUsage();
          return 1;
        }
        libPath = argv[i];
      }
    }
  }

  soHandle = dlopen(libPath, RTLD_NOW);
  soError = dlerror();
  if (soError != NULL) {
    printf("Unable to load library %s (%s)\n", libPath, soError);
    return 2;
  }

  compSymbol = *((void **)dlsym(soHandle, "__component_toplevel"));
  soError = dlerror();
  if (soError != NULL) {
    printf("Unable to find symbol __component_toplevel in library %s (%s)\n",
        libPath, soError);
    return 2;
  }

  if (controlled) {
    CALL(checkComponent)((fractal_api_Component) compSymbol);
  }

  if (factory) {
    fractal_api_Factory factoryItf;
    int err;
    void *comp;

    if (factoryOfControlled) {
      fractal_api_Component factoryComp = (fractal_api_Component) compSymbol;
      fractal_api_BindingController bc;
      int err;

      err = CALL_PTR(factoryComp, getFcInterface)("bindingController", (void **)&bc);
      mindassert(err == FRACTAL_API_OK);

      err = CALL_PTR(bc, bindFc)("allocator", GET_MY_INTERFACE(allocator));
      mindassert(err == FRACTAL_API_OK);

      err = CALL_PTR(factoryComp, getFcInterface)("factory", (void **)&factoryItf);
      mindassert(err == FRACTAL_API_OK);
    } else {
      factory_type *factComp = (factory_type *) compSymbol;

      memcpy(&(factComp->allocatorItf), GET_MY_INTERFACE(allocator), sizeof(struct __component_memory_api_Allocator_itf_desc));
      factoryItf = &(factComp->factoryItf);
    }

    err = CALL_PTR(factoryItf, newFcInstance)(&comp);
    mindassert(err == FRACTAL_API_OK);

    if (factoryOfControlled) {
      CALL(checkComponent)((fractal_api_Component) comp);
    }

    err = CALL_PTR(factoryItf, destroyFcInstance)(comp);
    mindassert(err == FRACTAL_API_OK);

  }

  return 0;
}

/* -----------------------------------------------------------------------------
   internal function.
----------------------------------------------------------------------------- */

static int METH(checkComponent)(fractal_api_Component compItf)
{
  int nbItf, err, i;
  const char **itfNames;
  void **itfRefs;
  void *itfRef;

  nbItf = CALL_PTR(compItf, listFcInterfaces)(NULL);
  mindassert(nbItf >=0);
  itfNames = (const char **) CALL(allocator, alloc)(nbItf* sizeof(char *));
  itfRefs = (void **) CALL(allocator, alloc)(nbItf* sizeof(void *));

  err = CALL_PTR(compItf, listFcInterfaces)(itfNames);
  mindassert(err == nbItf);

  err = CALL_PTR(compItf, getFcInterfaces)(itfRefs);
  mindassert(err == nbItf);

  for (i = 0; i < nbItf; i++) {
    mindassert(itfNames[i] != NULL);

    err = CALL_PTR(compItf, getFcInterface)(itfNames[i], &itfRef);
    mindassert(err == FRACTAL_API_OK);
    mindassert(itfRef == itfRefs[i]);
  }

  CALL(allocator, free)(itfNames);
  CALL(allocator, free)(itfRefs);

  return err;

}

static void printUsage(void)
{
  printf("dynloadTester [" FACTORY_FLAG "] SO_FILE \n");
}
