
package org.ow2.mind.idl;

import static java.lang.System.currentTimeMillis;
import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.InputResourcesHelper.getTimestamp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeUtil;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.ForceRegenContextHelper;
import org.ow2.mind.InputResource;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.InputResourcesHelper;
import org.ow2.mind.NodeInputStream;
import org.ow2.mind.idl.ast.IDL;

public class BinaryIDLLoader extends AbstractIDLLoader {

  protected static Logger     logger = FractalADLLogManager
                                         .getLogger("loader.BinaryLoader");

// ---------------------------------------------------------------------------
// Client interfaces
// ---------------------------------------------------------------------------

  /**
   * The {@link IDLLocator} client interface used to locate binary and source
   * IDL files.
   */
  public IDLLocator           idlLocatorItf;

  /**
   * The {@link InputResourceLocator} client interface used to locate and check
   * timestamps of dependencies of ADL AST.
   */
  public InputResourceLocator inputResourceLocatorItf;

  /**
   * The {@link NodeFactory} used to de-serialize binary ADL.
   * 
   * @see NodeInputStream
   */
  public NodeFactory          nodeFactoryItf;

// ---------------------------------------------------------------------------
// Implementation of the Loader interface
// ---------------------------------------------------------------------------

  public IDL load(final String name, final Map<Object, Object> context)
      throws ADLException {

    if (ForceRegenContextHelper.getForceRegen(context)) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". Forced mode, load source");
      return loadSourceIDL(name, context);
    }

    final URL binIDL;
    if (name.startsWith("/"))
      binIDL = idlLocatorItf.findBinaryHeader(name, context);
    else
      binIDL = idlLocatorItf.findBinaryItf(name, context);
    if (binIDL == null) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". binary IDL not found, load source");
      return loadSourceIDL(name, context);
    }

    final URL srcIDL;
    if (name.startsWith("/"))
      srcIDL = idlLocatorItf.findSourceHeader(name, context);
    else
      srcIDL = idlLocatorItf.findSourceItf(name, context);
    if (srcIDL == null) {
      // only binary file is available, load from binary file.
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name
            + "\". source unavailable, load binary");
      return loadBinaryIDL(name, binIDL, context);
    }

    // both binary and source file are available, check timestamps:
    boolean outOfDate;
    long binTimestamp = 0;
    try {
      binTimestamp = getTimestamp(binIDL);
      outOfDate = getTimestamp(srcIDL) >= binTimestamp;
    } catch (final MalformedURLException e) {
      if (logger.isLoggable(Level.WARNING))
        logger.log(Level.WARNING, "Load IDL \"" + name
            + "\". can't determine file timestamps");
      outOfDate = true;
    }
    if (!outOfDate) {
      // if binary file is more recent than source file, check dependencies.

      // load binary IDL to retrieve list of input resources.
      final IDL binAST = loadBinaryIDL(name, binIDL, context);

      final Set<InputResource> dependencies = InputResourcesHelper
          .getInputResources(binAST);
      if (logger.isLoggable(Level.FINEST))
        logger.log(Level.FINEST, "Load IDL \"" + name
            + "\". check dependencies=" + dependencies);
      if (dependencies != null
          && inputResourceLocatorItf.isUpToDate(binTimestamp, dependencies,
              context)) {
        if (logger.isLoggable(Level.FINEST))
          logger.log(Level.FINEST, "Load IDL \"" + name
              + "\". Binary version is up-to-date");

        // binary version is up to date, return it
        return binAST;
      }
    }

    if (logger.isLoggable(Level.FINE))
      logger.log(Level.FINE, "Load IDL \"" + name
          + "\". Binary IDL out of date, load source");
    // binary version is older than source file, load from source
    return loadSourceIDL(name, context);

  }

  protected IDL loadSourceIDL(final String name,
      final Map<Object, Object> context) throws ADLException {
    long t = 0;
    if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

    final IDL idl = clientIDLLoaderItf.load(name, context);

    if (logger.isLoggable(Level.FINER)) {
      t = currentTimeMillis() - t;
      logger.log(Level.FINER, "IDL \"" + name + "\" loaded from source in " + t
          + "ms.");
    }
    return idl;
  }

  protected IDL loadBinaryIDL(final String name, final URL location,
      final Map<Object, Object> context) throws ADLException {
    try {
      final InputStream is = location.openStream();
      final NodeInputStream nis = new NodeInputStream(is, nodeFactoryItf);
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Load IDL \"" + name + "\". Read IDL from "
            + location);

      long t = 0;
      if (logger.isLoggable(Level.FINER)) t = currentTimeMillis();

      final IDL idl = NodeUtil.castNodeError(nis.readNode(), IDL.class);

      if (logger.isLoggable(Level.FINER)) {
        t = currentTimeMillis() - t;
        logger.log(Level.FINER, "Load IDL \"" + name
            + "\".  read from binary file in " + t + "ms.");
      }

      nis.close();

      return idl;
    } catch (final IOException e) {
      throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary IDL " + location);
    } catch (final ClassNotFoundException e) {
      throw new ADLException(GenericErrors.INTERNAL_ERROR, e,
          "Can't read binary IDL " + location);
    }
  }

  // ---------------------------------------------------------------------------
  // Overridden BindingController methods
  // ---------------------------------------------------------------------------

  @Override
  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = (IDLLocator) value;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) value;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = (InputResourceLocator) value;
    } else {
      super.bindFc(itfName, value);
    }

  }

  @Override
  public String[] listFc() {
    return listFcHelper(super.listFc(), IDLLocator.ITF_NAME,
        InputResourceLocator.ITF_NAME, NodeFactory.ITF_NAME);
  }

  @Override
  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      return idlLocatorItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      return inputResourceLocatorItf;
    } else {
      return super.lookupFc(itfName);
    }
  }

  @Override
  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(IDLLocator.ITF_NAME)) {
      idlLocatorItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(InputResourceLocator.ITF_NAME)) {
      inputResourceLocatorItf = null;
    } else {
      super.unbindFc(itfName);
    }
  }
}
