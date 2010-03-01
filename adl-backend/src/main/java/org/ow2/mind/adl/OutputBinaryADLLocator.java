
package org.ow2.mind.adl;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.InputResource;
import org.ow2.mind.io.OutputFileLocator;

public class OutputBinaryADLLocator implements ADLLocator, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String CLIENT_LOCATOR_ITF_NAME = "client-locaotr";
  public ADLLocator          clientLocatorItf;
  public OutputFileLocator   outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLocator interface
  // ---------------------------------------------------------------------------

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    return clientLocatorItf.getInputResourcesRoot(context);
  }

  public URL findBinaryADL(final String name, final Map<Object, Object> context) {
    URL binADL = null;
    try {
      final File binADLOutputFile = outputFileLocatorItf.getMetadataOutputFile(
          BasicADLLocator.getADLBinaryName(name), context);
      if (binADLOutputFile.exists()) {
        binADL = binADLOutputFile.toURI().toURL();
      }
    } catch (final IOException e) {
      // ignore
    } catch (final ADLException e) {
      // ignore
    }
    if (binADL == null) {
      binADL = clientLocatorItf.findBinaryADL(name, context);
    }
    return binADL;
  }

  public URL findSourceADL(final String name, final Map<Object, Object> context) {
    return clientLocatorItf.findSourceADL(name, context);
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    return clientLocatorItf.findResource(name, context);
  }

  public InputResource toInputResource(final String name) {
    return clientLocatorItf.toInputResource(name);
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
      clientLocatorItf = (ADLLocator) o;
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
