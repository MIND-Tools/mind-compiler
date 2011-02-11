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

package org.ow2.mind.preproc;

import java.io.IOException;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.TokenStream;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.util.BooleanEvaluatorHelper;
import org.ow2.mind.preproc.parser.AbstractCPLParser;

public final class ExtensionHelper {

  public static final String  CPL_EXTENSION = "org.ow2.mind.preproc.cpl-parser";
  private static final String FACTORY       = "factory";
  private static final String CLASS         = "class";
  private static final String ENABLE_WHEN   = "enableWhen";

  private static CPLPreprocessorFactory getFactory(
      final PluginManager pluginManagerItf, final Map<Object, Object> context) {
    final Iterable<ConfigurationElement> extensions = pluginManagerItf
        .getConfigurationElements(CPL_EXTENSION, FACTORY);
    CPLPreprocessorFactory ppFactory = null;
    for (final ConfigurationElement extension : extensions) {
      final ConfigurationElement condition = extension.getChild(ENABLE_WHEN);
      if (condition == null
          || BooleanEvaluatorHelper.evaluate(condition.getChild(),
              pluginManagerItf, context)) {
        if (ppFactory != null) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR,
              "There are more than one extensions for the extension-point '"
                  + CPL_EXTENSION + "'. This is illegal.");
        }

        ppFactory = extension.createInstance(CLASS,
            CPLPreprocessorFactory.class);
      }
    }

    if (ppFactory == null) {
      ppFactory = new DefaultCPLPreprocessorFactory();
    }

    return ppFactory;
  }

  public static Lexer getLexer(final PluginManager pluginManagerItf,
      final String inputPath, final Map<Object, Object> context)
      throws IOException {
    return getFactory(pluginManagerItf, context).getLexer(
        new ANTLRFileStream(inputPath));
  }

  public static AbstractCPLParser getParser(
      final PluginManager pluginManagerItf, final TokenStream tokens,
      final Map<Object, Object> context) {
    return getFactory(pluginManagerItf, context).getParser(tokens);
  }

}
