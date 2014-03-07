
// -----------------------------------------------------------------------------
// Implementation of the primitive test.useidl.initf.Relay.
// -----------------------------------------------------------------------------


int METH(relayServiceItf, relayInterfacePointer)(importidl_TargetService targetItf) {
	int res = 1;

	// bind our client interface to the one given to us
	BIND_MY_INTERFACE(targetServiceItf, targetItf);
	if (IS_BOUND(targetServiceItf))
		res = CALL(targetServiceItf, getResult)();

	return res;
}
