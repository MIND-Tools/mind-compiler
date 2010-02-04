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

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;

public abstract class AbstractStringTemplateProcessor
    implements
      BindingController {

  protected final String           templateGroupName;
  private StringTemplateGroup      templateGroup   = null;

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #templateGroupLoaderItf} client interface. */
  public static final String       LOADER_ITF_NAME = "template-loader";

  /**
   * Client interface bound to the {@link XMLNodeFactory node factory}
   * component.
   */
  public StringTemplateGroupLoader templateGroupLoaderItf;

  // ---------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------

  protected AbstractStringTemplateProcessor(final String templateGroupName) {
    this.templateGroupName = templateGroupName;
  }

  // ---------------------------------------------------------------------------
  // Internal stuff
  // ---------------------------------------------------------------------------

  protected synchronized void initTemplateGroup() {
    templateGroup = templateGroupLoaderItf.loadGroup(templateGroupName,
        getTemplateLexer(), null);
    registerCustomRenderer(templateGroup);
  }

  protected synchronized StringTemplate getInstanceOf(final String template) {
    if (templateGroup == null) {
      initTemplateGroup();
    }
    return templateGroup.getInstanceOf(template);
  }

  @SuppressWarnings("unchecked")
  protected Class getTemplateLexer() {
    return AngleBracketTemplateLexer.class;
  }

  protected void registerCustomRenderer(final StringTemplateGroup templateGroup) {
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(LOADER_ITF_NAME)) {
      this.templateGroupLoaderItf = (StringTemplateGroupLoader) value;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }

  }

  public String[] listFc() {
    return listFcHelper(LOADER_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(LOADER_ITF_NAME)) {
      return this.templateGroupLoaderItf;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws NoSuchInterfaceException,
      IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(LOADER_ITF_NAME)) {
      this.templateGroupLoaderItf = null;
    } else {
      throw new NoSuchInterfaceException("There is no interface named '"
          + itfName + "'");
    }
  }
}
