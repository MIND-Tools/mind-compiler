
package org.ow2.mind.idl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.ow2.mind.InputResource;
import org.ow2.mind.inject.InjectDelegate;
import org.ow2.mind.io.OutputFileLocator;

import com.google.inject.Inject;

public class OutputBinaryIDLLocator implements IDLLocator {

  @InjectDelegate
  protected IDLLocator        clientLocatorItf;

  @Inject
  protected OutputFileLocator outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLocator interface
  // ---------------------------------------------------------------------------

  public Iterable<String> getResourceKind() {
    return clientLocatorItf.getResourceKind();
  }

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
}
