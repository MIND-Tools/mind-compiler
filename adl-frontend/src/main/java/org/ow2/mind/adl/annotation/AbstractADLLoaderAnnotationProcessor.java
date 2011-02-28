/**
 * Copyright (C) 2009 STMicroelectronics
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

package org.ow2.mind.adl.annotation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.adl.util.FractalADLLogManager;
import org.ow2.mind.adl.DefinitionCache;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.idl.InterfaceSignatureResolver;
import org.ow2.mind.adl.parser.ADLParserContextHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.IDLCache;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.parser.IDLParserContextHelper;

import com.google.inject.Inject;

/**
 * Base class for the implementation of annotation processors integrated in the
 * ADL loader chain. This abstract class provides some helper methods.
 * 
 * @see ADLLoaderAnnotationProcessor
 * @see ADLLoaderProcessor
 */
public abstract class AbstractADLLoaderAnnotationProcessor
    implements
      ADLLoaderAnnotationProcessor {

  protected static Logger               logger = FractalADLLogManager
                                                   .getLogger("annotations");

  @Inject
  protected ErrorManager                errorManagerItf;

  @Inject
  protected NodeFactory                 nodeFactoryItf;

  @Inject
  protected NodeMerger                  nodeMergerItf;

  @Inject
  protected DefinitionCache             definitionCacheItf;

  @Inject
  protected Loader                      loaderItf;

  @Inject
  protected IDLCache                    idlCacheItf;

  @Inject
  protected IDLLoader                   idlLoaderItf;

  @Inject
  protected DefinitionReferenceResolver defRefResolverItf;

  @Inject
  protected InterfaceSignatureResolver  itfSignatureResolverItf;

  @Inject
  protected StringTemplateGroupLoader   templateLoaderItf;

  // ---------------------------------------------------------------------------
  // Utility methods
  // ---------------------------------------------------------------------------

  /**
   * Returns <code>true</code> if a definition with the given name as already
   * been loaded.
   * 
   * @return <code>true</code> if a definition with the given name as already
   *         been loaded.
   */
  protected boolean isAlreadyGenerated(final String name,
      final Map<Object, Object> context) {
    return ADLParserContextHelper.isRegisteredADL(name, context)
        || definitionCacheItf.getInCache(name, context) != null;
  }

  /**
   * Load an ADL definition from the given (generated) source. If a definition
   * is already known in cache for the given name, this method returns the
   * definition in cache. <br>
   * If the loading of the definition raise an exception, a temporary file is
   * generated in which the given sources are dumped. This simplifies the
   * debugging of the generator.
   * 
   * @param name the name of the ADL;
   * @param adlSource the source code of the ADL;
   * @param context context map.
   * @return the loaded ADL.
   * @throws ADLException if a error occurs.
   * @see #isAlreadyGenerated(String, Map)
   * @see ADLParserContextHelper#registerADL(String, String, Map)
   */
  protected Definition loadFromSource(final String name,
      final String adlSource, final Map<Object, Object> context)
      throws ADLException {
    Definition def = definitionCacheItf.getInCache(name, context);
    if (def != null) {
      return def;
    }
    ADLParserContextHelper.registerADL(name, adlSource, context);
    final int nbErrors = errorManagerItf.getErrors().size();
    boolean containsErrors = false;
    try {
      def = loaderItf.load(name, context);
      containsErrors = errorManagerItf.getErrors().size() != nbErrors;
    } catch (final ADLException e) {
      containsErrors = true;
    }

    if (containsErrors) {
      // The loading of the generated ADL fails.
      // Print the ADL content in a temporary file to ease its debugging.
      try {
        final File f = File.createTempFile("GeneratedADL", ".adl");
        logger.warning("Loading of generated ADL " + name
            + " fails. Dump ADL sources in " + f);
        final FileWriter fw = new FileWriter(f);
        fw.write(adlSource);
        fw.close();
      } catch (final IOException e1) {
        // ignore
      }
      if (def == null) {
        def = ASTHelper.newUnresolvedDefinitionNode(nodeFactoryItf, name);
      }
    }
    return def;
  }

  protected Definition loadFromAST(final Definition def,
      final Map<Object, Object> context) throws ADLException {
    final Definition d = definitionCacheItf.getInCache(def.getName(), context);
    if (d != null) {
      return d;
    }
    ADLParserContextHelper.registerADL(def, context);
    return loaderItf.load(def.getName(), context);
  }

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
    IDL idl = idlCacheItf.getInCache(name, context);
    if (idl != null) {
      return idl;
    }
    IDLParserContextHelper.registerIDL(name, idlSource, context);
    final int nbErrors = errorManagerItf.getErrors().size();
    boolean containsErrors = false;
    try {
      idl = idlLoaderItf.load(name, context);
      containsErrors = errorManagerItf.getErrors().size() != nbErrors;
    } catch (final ADLException e) {
      containsErrors = true;
    }
    if (containsErrors) {
      // The loading of the generated ADL fails.
      // Print the IDL content in a temporary file to ease its debugging.
      try {
        final File f = File.createTempFile("GeneratedIDL", ".idl");
        logger.warning("Loading of generated IDL " + name
            + " fails. Dump IDL sources in " + f);
        final FileWriter fw = new FileWriter(f);
        fw.write(idlSource);
        fw.close();
      } catch (final IOException e1) {
        // ignore
      }
    }
    if (idl == null) {
      idl = IDLASTHelper.newUnresolvedIDLNode(nodeFactoryItf, name);
    }
    return idl;
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
}
