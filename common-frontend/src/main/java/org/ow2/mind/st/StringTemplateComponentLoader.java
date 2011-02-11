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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.st;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupInterface;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.st.templates.ast.BoundInterface;
import org.ow2.mind.st.templates.ast.PluginInterface;
import org.ow2.mind.st.templates.ast.ServerInterface;
import org.ow2.mind.st.templates.ast.TemplateComponent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class StringTemplateComponentLoader implements StringTemplateGroupLoader {

  public static final String            STRING_TEMPLATE_LOADER_NAME = "StringTemplateLoader";

  @Inject
  @Named("StringTemplateLoader")
  public Loader                         loaderItf;

  protected StringTemplateErrorListener errors                      = null;

  public StringTemplateComponentLoader() {
    errors = new org.ow2.mind.st.StringTemplateGroup("DefaultGroup")
        .getErrorListener();
  }

  /*
   * (non-Javadoc)
   * @see
   * org.antlr.stringtemplate.StringTemplateGroupLoader#loadGroup(java.lang.
   * String)
   */
  public StringTemplateGroup loadGroup(final String groupName) {
    return loadGroup(groupName, null);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.antlr.stringtemplate.StringTemplateGroupLoader#loadGroup(java.lang.
   * String, org.antlr.stringtemplate.StringTemplateGroup)
   */
  public StringTemplateGroup loadGroup(final String groupName,
      final StringTemplateGroup superGroup) {
    return loadGroup(groupName, null, superGroup);

  }

  /*
   * (non-Javadoc)
   * @see
   * org.antlr.stringtemplate.StringTemplateGroupLoader#loadGroup(java.lang.
   * String, java.lang.Class, org.antlr.stringtemplate.StringTemplateGroup)
   */
  public StringTemplateGroup loadGroup(final String groupName,
      final Class templateLexer, StringTemplateGroup superGroup) {
    org.ow2.mind.st.StringTemplateGroup group = null;
    InputStreamReader br = null;
    // group file format defaults to <...>
    Class<?> lexer = AngleBracketTemplateLexer.class;
    if (templateLexer != null) {
      lexer = templateLexer;
    }

    final TemplateComponent stc = getTemplate(groupName);
    br = new InputStreamReader(new ByteArrayInputStream(stc.getContent()
        .getBytes()));

    // FIXME: Here the down cast might be dangereous.
    group = new org.ow2.mind.st.StringTemplateGroup(br, lexer, errors,
        (org.ow2.mind.st.StringTemplateGroup) superGroup);

    if (superGroup == null) {
      superGroup = group;
    }

    if (stc.getSuperTemplate() != null) {
      superGroup = loadGroup(stc.getSuperTemplate().getName(), templateLexer,
          null);
      group.setSuperGroup(superGroup);
    }

    for (final ServerInterface serverInterface : stc.getServerInterfaces()) {
      final StringTemplateGroupInterface itf = loadInterface(serverInterface
          .getSignature());
      group.implementInterface(itf);
    }

    group.registerRenderer(String.class, new BackendFormatRenderer());

    // Register maps for plugin interfaces
    for (final PluginInterface pluginInterface : stc.getPluginInterfaces()) {
      final PluginInterfaceMap map = new PluginInterfaceMap(
          (org.ow2.mind.st.StringTemplateGroup) superGroup,
          pluginInterface.getRepository(), loadInterface(
              pluginInterface.getSignature()).getName(), this);
      group.defineMap(pluginInterface.getName(), map);
    }

    // Register maps for bound interfaces
    for (final BoundInterface boundInterface : stc.getBoundInterfaces()) {
      final BoundInterfaceMap map = new BoundInterfaceMap(
          (org.ow2.mind.st.StringTemplateGroup) superGroup,
          boundInterface.getBoundTo(), loadInterface(
              boundInterface.getSignature()).getName(), this);
      group.defineMap(boundInterface.getName(), map);
    }

    try {
      br.close();
    } catch (final IOException e) {
      error("Cannot close the input stream reader for template group '"
          + groupName + "'.");
    }
    br = null;

    return group;
  }

  public StringTemplateGroupInterface loadInterface(final String interfaceName) {
    StringTemplateGroupInterface I;
    final InputStreamReader br = locateItf(interfaceName.replace('.', '/')
        + ".sti");
    if (br == null) {
      error("no such interface file " + interfaceName + ".sti");
      return null;
    }
    I = new StringTemplateGroupInterface(br, errors);
    return I;
  }

  // ---------------------------------------------------------------------------
  // Implementation of helper methods
  // ---------------------------------------------------------------------------

  protected TemplateComponent getTemplate(final String groupName) {
    try {
      return (TemplateComponent) loaderItf.load(groupName,
          new HashMap<Object, Object>());
    } catch (final ADLException e) {
      error("Cannot load group file '" + groupName + "'.", e);
      return null;
    }
  }

  protected InputStreamReader locateItf(final String name) {
    return new InputStreamReader(ClassLoaderHelper.getClassLoader(this)
        .getResourceAsStream(name));
  }

  protected void error(final String msg) {
    error(msg, null);
  }

  protected void error(final String msg, final Exception e) {
    if (errors != null) {
      errors.error(msg, e);
    } else {
      System.err.println("StringTemplate: " + msg);
      if (e != null) {
        e.printStackTrace();
      }
    }
  }
}
