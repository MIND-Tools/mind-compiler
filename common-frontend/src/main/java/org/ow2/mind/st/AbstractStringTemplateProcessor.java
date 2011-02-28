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

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

import com.google.inject.Inject;

public abstract class AbstractStringTemplateProcessor {

  protected final String              templateGroupName;
  private StringTemplateGroup         templateGroup = null;

  @Inject
  protected StringTemplateGroupLoader templateGroupLoaderItf;

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
}
