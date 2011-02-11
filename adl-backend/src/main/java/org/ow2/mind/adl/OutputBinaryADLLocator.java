
package org.ow2.mind.adl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.ow2.mind.InputResource;
import org.ow2.mind.inject.InjectDelegate;
import org.ow2.mind.io.OutputFileLocator;

import com.google.inject.Inject;

public class OutputBinaryADLLocator implements ADLLocator {

  @InjectDelegate
  protected ADLLocator        clientLocatorItf;

  @Inject
  protected OutputFileLocator outputFileLocatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ADLLocator interface
  // ---------------------------------------------------------------------------

  public Iterable<String> getResourceKind() {
    return clientLocatorItf.getResourceKind();
  }

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
}
