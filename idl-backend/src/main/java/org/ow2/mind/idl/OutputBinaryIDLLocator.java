
package org.ow2.mind.idl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResource;
import org.ow2.mind.io.OutputFileLocator;

public class OutputBinaryIDLLocator implements IDLLocator, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_LOCATOR_ITF_NAME = "client-locaotr";
  public IDLLocator          clientLocatorItf;
  public OutputFileLocator   outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLocator interface
  // ---------------------------------------------------------------------------

  public URL findSourceItf(final String name, final Map<Object, Object> context) {
    return clientLocatorItf.findSourceItf(name, context);
  }

  public URL findBinaryItf(final String name, final Map<Object, Object> context) {
    URL binADL = null;
    try {
      final File binADLOutputFile = outputFileLocatorItf.getMetadataOutputFile(
          BasicIDLLocator.getItfBinaryName(name), context);
      if (binADLOutputFile.exists()) {
        binADL = binADLOutputFile.toURI().toURL();
      }
    } catch (final IOException e) {
      // ignore
    }
    if (binADL == null) {
      binADL = clientLocatorItf.findBinaryItf(name, context);
    }
    return binADL;
  }

  public URL findSourceHeader(final String path,
      final Map<Object, Object> context) {
    return clientLocatorItf.findSourceHeader(path, context);
  }

  public URL findBinaryHeader(final String path,
      final Map<Object, Object> context) {
    URL binADL = null;
    try {
      final File binADLOutputFile = outputFileLocatorItf.getMetadataOutputFile(
          BasicIDLLocator.getHeaderBinaryName(path), context);
      if (binADLOutputFile.exists()) {
        binADL = binADLOutputFile.toURI().toURL();
      }
    } catch (final IOException e) {
      // ignore
    }
    if (binADL == null) {
      binADL = clientLocatorItf.findBinaryHeader(path, context);
    }
    return binADL;
  }

  public InputResource toInterfaceInputResource(final String name) {
    return clientLocatorItf.toInterfaceInputResource(name);
  }

  public InputResource toSharedTypeInputResource(final String name) {
    return clientLocatorItf.toSharedTypeInputResource(name);
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return clientLocatorItf.findResource(name, context);
  }

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    return clientLocatorItf.getInputResourcesRoot(context);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(CLIENT_LOCATOR_ITF_NAME, OutputFileLocator.ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (s.equals(CLIENT_LOCATOR_ITF_NAME)) {
      return clientLocatorItf;
    } else if (s.equals(OutputFileLocator.ITF_NAME)) {
      return outputFileLocatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (s.equals(CLIENT_LOCATOR_ITF_NAME)) {
      clientLocatorItf = (IDLLocator) o;
    } else if (s.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = (OutputFileLocator) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (s.equals(CLIENT_LOCATOR_ITF_NAME)) {
      clientLocatorItf = null;
    } else if (s.equals(OutputFileLocator.ITF_NAME)) {
      outputFileLocatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }
}
