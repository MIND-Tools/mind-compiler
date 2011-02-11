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

package org.ow2.mind.cli;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.JavaFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorException;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorLoader;
import org.objectweb.fractal.cecilia.targetDescriptor.TargetDescriptorLoaderJavaFactory;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.CFlag;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.LdFlag;
import org.objectweb.fractal.cecilia.targetDescriptor.ast.Target;
import org.ow2.mind.compilation.CompilerContextHelper;
import org.ow2.mind.compilation.DirectiveHelper;
import org.ow2.mind.plugin.util.Assert;

/**
 * Handles "target-descriptor" option. Reads the target descriptor and register
 * it in the context. This handler depends on {@link SrcPathOptionHandler}.
 */
public class TargetDescriptorOptionHandler implements CommandOptionHandler {

  /** The ID of the "target-descriptor" option. */
  public static final String  TARGET_DESCRIPTOR_ID    = "org.ow2.mind.mindc.TargetDescriptor";

  private static final String TARGET_DESC_CONTEXT_KEY = "target_descriptor";

  /**
   * Returns the target descriptor that has been registered in the given
   * context.
   * 
   * @param context the current context.
   * @return the target descriptor or <code>null</code>.
   */
  public static Target getTargetDescriptor(final Map<Object, Object> context) {
    return (Target) context.get(TARGET_DESC_CONTEXT_KEY);
  }

  public void processCommandOption(final CmdOption cmdOption,
      final CommandLine cmdLine, final Map<Object, Object> context)
      throws InvalidCommandLineException {
    Assert.assertEquals(cmdOption.getId(), TARGET_DESCRIPTOR_ID);
    final CmdArgument targetDescOpt = Assert.assertInstanceof(cmdOption,
        CmdArgument.class);

    final String targetDesc = targetDescOpt.getValue(cmdLine);
    if (targetDesc != null) {
      final TargetDescriptorLoader loader = createTargetDescriptorLoader();

      Target targetDescriptor;
      try {
        targetDescriptor = loader.load(targetDesc, context);
      } catch (final TargetDescriptorException e) {
        throw new InvalidCommandLineException(
            "Unable to load target descriptor: " + e.getMessage(), 1);
      }
      if (targetDescriptor.getLinkerScript() != null) {
        final URL linkerScriptURL = SrcPathOptionHandler.getSourceClassLoader(
            context).getResource(targetDescriptor.getLinkerScript().getPath());
        if (linkerScriptURL == null) {
          throw new InvalidCommandLineException("Invalid linker script: '"
              + targetDescriptor.getLinkerScript().getPath()
              + "'. Cannot find file in the source path", 1);
        }
        targetDescriptor.getLinkerScript().setPath(linkerScriptURL.getPath());
      }
      processContext(targetDescriptor, context);
    }
  }

  protected void processContext(final Target targetDesc,
      final Map<Object, Object> context) {
    processCFlags(targetDesc, context);
    processLdFlags(targetDesc, context);
    processCompiler(targetDesc, context);
    processLinker(targetDesc, context);
    processLinkerScript(targetDesc, context);
  }

  protected void processCFlags(final Target target,
      final Map<Object, Object> context) {
    if (target != null && target.getCFlags().length > 0) {
      final CFlag[] flags = target.getCFlags();

      final List<String> targetFlags = new ArrayList<String>();
      for (final CFlag flag : flags) {
        targetFlags.addAll(DirectiveHelper.splitOptionString(flag.getValue()));
      }

      CompilerContextHelper.getCFlags(context);
      List<String> contextFlags = CompilerContextHelper.getCFlags(context);
      ;
      if (contextFlags == null) {
        contextFlags = new ArrayList<String>();
      }
      contextFlags.addAll(targetFlags);
      CompilerContextHelper.setCFlags(context, contextFlags);
    }
  }

  protected void processLdFlags(final Target target,
      final Map<Object, Object> context) {
    if (target != null && target.getLdFlags().length > 0) {
      final LdFlag[] flags = target.getLdFlags();

      final List<String> targetFlags = new ArrayList<String>();
      for (final LdFlag flag : flags) {
        targetFlags.addAll(DirectiveHelper.splitOptionString(flag.getValue()));
      }

      List<String> contextFlags = CompilerContextHelper.getLDFlags(context);
      if (contextFlags == null) {
        contextFlags = new ArrayList<String>();
      }
      contextFlags.addAll(targetFlags);
      CompilerContextHelper.setLDFlags(context, contextFlags);
    }
  }

  protected void processCompiler(final Target target,
      final Map<Object, Object> context) {
    final String opt = CompilerContextHelper.getCompilerCommand(context);
    if (opt == CompilerContextHelper.DEFAULT_COMPILER_COMMAND && target != null
        && target.getCompiler() != null) {
      CompilerContextHelper.setCompilerCommand(context, target.getCompiler()
          .getPath());
    }
  }

  protected void processLinker(final Target target,
      final Map<Object, Object> context) {
    final String opt = CompilerContextHelper.getLinkerCommand(context);
    if (opt == CompilerContextHelper.DEFAULT_LINKER_COMMAND && target != null
        && target.getLinker() != null) {
      CompilerContextHelper.setLinkerCommand(context, target.getLinker()
          .getPath());
    }
  }

  protected void processLinkerScript(final Target target,
      final Map<Object, Object> context) {
    if (target != null) {
      final String opt = CompilerContextHelper.getLinkerScript(context);
      if (opt == null && target.getLinkerScript() != null) {
        CompilerContextHelper.setLinkerScript(context, target.getLinkerScript()
            .getPath());
      }
    }
  }

  protected TargetDescriptorLoader createTargetDescriptorLoader() {
    try {
      final JavaFactory factory = new TargetDescriptorLoaderJavaFactory();
      final Map<?, ?> component = (Map<?, ?>) factory.newComponent();
      return (TargetDescriptorLoader) component.get("loader");
    } catch (final Exception e) {
      throw new CompilerError(GenericErrors.GENERIC_ERROR, e,
          "Unable to instantiate target descriptor loader");
    }
  }
}
