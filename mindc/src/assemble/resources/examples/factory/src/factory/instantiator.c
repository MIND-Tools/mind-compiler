/**
 * mindc examples
 *
 * Copyright (C) 2010 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 */

#include <stdio.h>

// -----------------------------------------------------------------------------
// Implementation of the entryPoint interface with signature boot.Main.
// -----------------------------------------------------------------------------

// int main(int argc, string[] argv)
int METH(entryPoint, main) (int argc, char *argv[]) {
  int r;
  void *forwarder;
  fractal_api_Component forwarderCompItf;
  fractal_api_BindingController forwarderBCItf;
  boot_Main forwarderMainItf;

  printf("In instantiator: Call applicationEntryPoint.main\n");
  r = CALL(applicationEntryPoint, main)(argc, argv);
  printf("In instantiator: applicationEntryPoint.main retruned\n");

  // create a forwarder component by calling the "newFcInstance" of the
  // "forwarderFactory" required interface.
  r = CALL(forwarderFactory, newFcInstance) (&forwarder);
  if (r != FRACTAL_API_OK) {
    printf("ERROR %d In instantiator: failed to create forwarder component\n", r);
    return r;
  }

  // cast forwarder pointer as a fractal.api.Component interface reference.
  // this supposes that the component instantiated by the forwarderFactory
  // provides the component controller.
  forwarderCompItf = (fractal_api_Component) forwarder;

  // bind the "forwarded" required interface of the forwarder component to the
  // provided interface that is bound to my "applicationEntryPoint" client
  // interface

  // first retrieve the "bindingController" interface of the forwarder
  // interface
  r = CALL_PTR(forwarderCompItf, getFcInterface) ("bindingController", (void **)& forwarderBCItf);
  if (r != FRACTAL_API_OK) {
    printf("ERROR %d In instantiator: failed to retrieve the \"bindingController\" interface of the forwarder component\n", r);
    return r;
  }

  // then call the bindFc method of the binding-controller interface to bind the
  // "forwarded" interface of the forwarder component to the provided interface
  // that is bound to my "applicationEntryPoint" required interface. Use the
  // GET_MY_INTERFACE CPL macro to retrieve a reference to the
  // "applicationEntryPoint" interface.
  r = CALL_PTR(forwarderBCItf, bindFc)("forwarded", GET_MY_INTERFACE(applicationEntryPoint));
  if (r != FRACTAL_API_OK) {
    printf("ERROR %d In instantiator: failed to bind the \"forwarded\" interface of the forwarder component\n", r);
    return r;
  }

  // finaly bind myself to the "entryPoint" provided interface of the forwarder
  // component. Use the BIND_MY_INTERFACE CPL macro to do so.
  r = CALL_PTR(forwarderCompItf, getFcInterface) ("entryPoint", (void **)& forwarderMainItf);
  if (r != FRACTAL_API_OK) {
    printf("ERROR %d In instantiator: failed to retrieve the \"entryPoint\" interface of the forwarder component\n", r);
    return r;
  }
  BIND_MY_INTERFACE(applicationEntryPoint, forwarderMainItf);

  // re-call my forwarderMainItf
  printf("In instantiator: Call applicationEntryPoint.main\n");
  r = CALL(applicationEntryPoint, main)(argc, argv);
  printf("In instantiator: applicationEntryPoint.main retruned\n");

  return r;
}
