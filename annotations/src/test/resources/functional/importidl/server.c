
#include <stdio.h>

int METH(targetServiceItf, getResult)() {
	return 0;
}

importidl_TargetService METH(interfaceTransferItf, getTargetServiceInterface)() {
	importidl_TargetService myService = GET_MY_INTERFACE(targetServiceItf);
	return myService;
}
