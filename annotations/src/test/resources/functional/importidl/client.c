
int METH(main, main)(int argc, char **argv) {

	int res = 2;

	/* get server business interface */
	importidl_TargetService targetService = CALL(interfaceTransferItf, getTargetServiceInterface)();

	/* transfer it to relay */
	res = CALL(relayServiceItf, relayInterfacePointer)(targetService);

	return res;
}
