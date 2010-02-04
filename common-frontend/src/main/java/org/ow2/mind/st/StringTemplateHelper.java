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

package org.ow2.mind.st;

import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.antlr.stringtemplate.StringTemplate;

public final class StringTemplateHelper {
  private StringTemplateHelper() {
  }

  public static StringTemplate getTemplate(final String groupName,
      final String templateName) {
    final String groupFileName = fullyQualifiedNameToPath(groupName, ".st")
        .substring(1);
    final InputStream groupFile = ClassLoader
        .getSystemResourceAsStream(groupFileName);
    if (groupFile == null) {
      throw new IllegalArgumentException("Can't find template group file \""
          + groupFileName + "\"");
    }
    final StringTemplateGroup group = new StringTemplateGroup(
        new InputStreamReader(groupFile));

    final StringTemplate template = group.getInstanceOf(templateName);
    template.registerRenderer(String.class, new BackendFormatRenderer());

    return template;
  }

  public static StringTemplate getTemplate(final String groupName,
      final String superGroupName, final String templateName) {
    final String groupFileName = fullyQualifiedNameToPath(groupName, ".st")
        .substring(1);
    final InputStream groupFile = ClassLoader
        .getSystemResourceAsStream(groupFileName);
    if (groupFile == null) {
      throw new IllegalArgumentException("Can't find template group file \""
          + groupFileName + "\"");
    }
    final StringTemplateGroup group = new StringTemplateGroup(
        new InputStreamReader(groupFile));

    final String superGroupFileName = fullyQualifiedNameToPath(superGroupName,
        ".st").substring(1);
    final InputStream superGroupFile = ClassLoader
        .getSystemResourceAsStream(superGroupFileName);
    if (superGroupFile == null) {
      throw new IllegalArgumentException("Can't find template group file \""
          + superGroupFileName + "\"");
    }
    final StringTemplateGroup superGroup = new StringTemplateGroup(
        new InputStreamReader(superGroupFile));
    group.setSuperGroup(superGroup);

    final StringTemplate template = group.getInstanceOf(templateName);
    template.registerRenderer(String.class, new BackendFormatRenderer());

    return template;
  }
}
