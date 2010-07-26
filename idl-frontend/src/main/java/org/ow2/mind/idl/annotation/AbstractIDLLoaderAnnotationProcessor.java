/**
 * Copyright (C) 2010 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.idl.annotation;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.BasicErrorLocator;
import org.objectweb.fractal.adl.error.ErrorLocator;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLCache;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.parser.IDLParserContextHelper;

/**
 * Base class for the implementation of annotation processors integrated in the
 * IDL loader chain. This abstract class provides some helper methods.
 * 
 * @see IDLLoaderAnnotationProcessor
 * @see IDLLoaderProcessor
 */
public abstract class AbstractIDLLoaderAnnotationProcessor
    implements
      IDLLoaderAnnotationProcessor,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The {@link ErrorManager} client interface used to log errors. */
  public ErrorManager              errorManagerItf;

  /** The client interface used to create new AST nodes. */
  public NodeFactory               nodeFactoryItf;

  /** The client interface used to merge AST nodes. */
  public NodeMerger                nodeMergerItf;

  /** The {@link IDLCache} client interface. */
  public IDLCache                  idlCacheItf;

  /** The {@link IDLLoader} client interface. */
  public IDLLoader                 idlLoaderItf;

  /** The {@link StringTemplateGroupLoader} client interface. */
  public StringTemplateGroupLoader templateLoaderItf;

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if an IDL with the given name as already been
   * loaded.
   * 
   * @return <code>true</code> if an IDL with the given name as already been
   *         loaded.
   */
  protected boolean isIDLAlreadyGenerated(final String name,
      final Map<Object, Object> context) {
    return IDLParserContextHelper.isRegisteredIDL(name, context)
        || idlCacheItf.getInCache(name, context) != null;
  }

  /**
   * Load an IDL definition from the given (generated) source. If a definition
   * is already known in cache for the given name, this method returns the IDL
   * in cache. <br>
   * If the loading of the IDL raise an exception, a temporary file is generated
   * in which the given sources are dumped. This simplifies the debugging of the
   * generator.
   * 
   * @param name the name of the IDL;
   * @param idlSource the source code of the IDL;
   * @param context context map.
   * @return the loaded IDL.
   * @throws ADLException if a error occurs.
   * @see #isIDLAlreadyGenerated(String, Map)
   * @see IDLParserContextHelper#registerIDL(String, String, Map)
   */
  protected IDL loadIDLFromSource(final String name, final String idlSource,
      final Map<Object, Object> context) throws ADLException {
    final IDL idl = idlCacheItf.getInCache(name, context);
    if (idl != null) {
      return idl;
    }
    IDLParserContextHelper.registerIDL(name, idlSource, context);
    try {
      return idlLoaderItf.load(name, context);
    } catch (final ADLException e) {
      // The loading of the generated ADL fails.
      // Print the ADL content in a temporary file to ease its debugging.
      try {
        final File f = File.createTempFile("GeneratedIDL", ".idl");
        final FileWriter fw = new FileWriter(f);
        fw.write(idlSource);
        fw.close();

        // update the error locator to point to the temporary file.
        final ErrorLocator l = e.getError().getLocator();
        final ErrorLocator l1 = new BasicErrorLocator(f.getPath(),
            l.getBeginLine(), l.getBeginColumn());
        e.getError().setLocator(l1);
      } catch (final IOException e1) {
        // ignore
      }
      throw e;
    }
  }

  protected IDL loadIDLFromAST(final IDL idl, final Map<Object, Object> context)
      throws ADLException {
    final IDL d = idlCacheItf.getInCache(idl.getName(), context);
    if (d != null) {
      return d;
    }
    IDLParserContextHelper.registerIDL(idl, context);
    return idlLoaderItf.load(idl.getName(), context);
  }

  /**
   * Returns the StringTemplate template with the given
   * <code>templateName</code> name and found in the
   * <code>templateGroupName</code> group.
   * 
   * @param templateGroupName the groupName from which the template must be
   *          loaded.
   * @param templateName the name of the template.
   * @return a StringTemplate template
   * @see StringTemplateGroupLoader
   */
  protected StringTemplate getTemplate(final String templateGroupName,
      final String templateName) {
    final StringTemplateGroup templateGroup = templateLoaderItf.loadGroup(
        templateGroupName, AngleBracketTemplateLexer.class, null);
    registerCustomRenderer(templateGroup);
    return templateGroup.getInstanceOf(templateName);
  }

  /**
   * This method can be overridden by sub-classes to register a custom renderer
   * when a templateGroup is loaded by {@link #getTemplate(String, String)}.
   * 
   * @param templateGroup the loaded templateGroup.
   * @see StringTemplateGroup#registerRenderer(Class, Object)
   */
  protected void registerCustomRenderer(final StringTemplateGroup templateGroup) {
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(ErrorManager.ITF_NAME, NodeFactory.ITF_NAME,
        NodeMerger.ITF_NAME, IDLCache.ITF_NAME, IDLLoader.ITF_NAME,
        "template-loader");
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      return errorManagerItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      return nodeFactoryItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      return nodeMergerItf;
    } else if (itfName.equals(IDLCache.ITF_NAME)) {
      return idlCacheItf;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      return idlLoaderItf;
    } else if (itfName.equals("template-loader")) {
      return templateLoaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void bindFc(final String itfName, final Object serverItf)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = (ErrorManager) serverItf;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = (NodeFactory) serverItf;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = (NodeMerger) serverItf;
    } else if (itfName.equals(IDLCache.ITF_NAME)) {
      idlCacheItf = (IDLCache) serverItf;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = (IDLLoader) serverItf;
    } else if (itfName.equals("template-loader")) {
      templateLoaderItf = (StringTemplateGroupLoader) serverItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(ErrorManager.ITF_NAME)) {
      errorManagerItf = null;
    } else if (itfName.equals(NodeFactory.ITF_NAME)) {
      nodeFactoryItf = null;
    } else if (itfName.equals(NodeMerger.ITF_NAME)) {
      nodeMergerItf = null;
    } else if (itfName.equals(IDLCache.ITF_NAME)) {
      idlCacheItf = null;
    } else if (itfName.equals(IDLLoader.ITF_NAME)) {
      idlLoaderItf = null;
    } else if (itfName.equals("template-loader")) {
      templateLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }
}
