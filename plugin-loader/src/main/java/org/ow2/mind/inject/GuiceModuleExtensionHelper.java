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

package org.ow2.mind.inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.plugin.ConfigurationElement;
import org.ow2.mind.plugin.PluginManager;
import org.ow2.mind.plugin.util.Assert;
import org.ow2.mind.plugin.util.BooleanEvaluatorHelper;

import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * Helper class for the Guice Module extension point.
 */
public final class GuiceModuleExtensionHelper {

  /** The extension point ID. */
  public static final String      GUICE_MODULE_EXTENSION_POINT_ID = "org.ow2.mind.plugin.guice-module";

  private static final String     CLASS                           = "class";
  private static final String     ENABLE_WHEN                     = "enableWhen";
  private static final String     OVERRIDE                        = "override";
  private static final String     COMBINE                         = "combine";

  private static Iterable<Module> modules;

  private GuiceModuleExtensionHelper() {
  }

  /**
   * Returns the modules that has been registered using the Guice Module
   * extension point.
   * 
   * @param pluginManager the pluginManager.
   * @param context the compilation context.
   * @return the modules that has been registered using the Guice Module
   *         extension points.
   */
  public static synchronized Iterable<Module> getModules(
      final PluginManager pluginManager, final Map<Object, Object> context) {
    if (modules == null) {
      initializeModules(pluginManager, context);
    }
    return modules;
  }

  private static void initializeModules(final PluginManager pluginManager,
      final Map<Object, Object> context) {
    final Map<String, SimpleModuleDesc> moduleDescs = new HashMap<String, SimpleModuleDesc>();

    for (final ConfigurationElement module : pluginManager
        .getConfigurationElements(GUICE_MODULE_EXTENSION_POINT_ID)) {
      final String clazz = module.getAttribute(CLASS);
      if (moduleDescs.containsKey(clazz)) {
        Assert.fail("Module class '" + clazz + "' is already used");
      }

      final ConfigurationElement condition = module.getChild(ENABLE_WHEN);
      if (condition == null
          || BooleanEvaluatorHelper.evaluate(condition.getChild(),
              pluginManager, context)) {
        moduleDescs.put(clazz, new SimpleModuleDesc(module));
      }
    }

    // process combine directives
    final Map<String, ModuleDesc> combinedModules = new HashMap<String, ModuleDesc>(
        moduleDescs.size());
    for (final SimpleModuleDesc moduleDesc : moduleDescs.values()) {
      final ConfigurationElement combine = moduleDesc.desc.getChild(COMBINE);
      if (combine != null) {
        final String otherClass = combine.getAttribute(CLASS);
        final SimpleModuleDesc otherModuleDesc = moduleDescs.get(otherClass);

        if (otherModuleDesc == null) {
          throw new CompilerError(GenericErrors.GENERIC_ERROR,
              "Unknown module class '" + otherModuleDesc + "'");
        }

        final ModuleDesc combinedModule = combinedModules.get(moduleDesc.clazz);
        if (combinedModule != null) {
          assert combinedModule instanceof CombinedModuleDesc;
          final ModuleDesc otherCombinedModule = combinedModules
              .get(otherClass);
          if (otherCombinedModule != null) {
            if (otherCombinedModule instanceof CombinedModuleDesc) {
              // merge the two combined modules
              ((CombinedModuleDesc) combinedModule)
                  .merge((CombinedModuleDesc) otherCombinedModule);
              for (final SimpleModuleDesc desc : ((CombinedModuleDesc) otherCombinedModule).combinedModules) {
                assert combinedModules.get(desc.clazz) == otherCombinedModule;
                combinedModules.put(desc.clazz, combinedModule);
              }
            }
          } else {
            ((CombinedModuleDesc) combinedModule).add(otherModuleDesc);
            combinedModules.put(otherClass, combinedModule);
          }
        } else { // combinedModule == null
          final ModuleDesc otherCombinedModule = combinedModules
              .get(otherClass);
          if (otherCombinedModule == null
              || otherCombinedModule instanceof SimpleModuleDesc) {
            final CombinedModuleDesc m = new CombinedModuleDesc();
            m.add(moduleDesc);
            m.add(otherModuleDesc);
            combinedModules.put(moduleDesc.clazz, m);
            combinedModules.put(otherClass, m);
          } else {
            assert otherCombinedModule instanceof CombinedModuleDesc;
            ((CombinedModuleDesc) otherCombinedModule).add(moduleDesc);
            combinedModules.put(moduleDesc.clazz, otherCombinedModule);
          }
        }
      } else {
        final ModuleDesc combinedModule = combinedModules.get(moduleDesc.clazz);
        if (combinedModule != null) {
          assert combinedModule instanceof CombinedModuleDesc;
          ((CombinedModuleDesc) combinedModule).add(moduleDesc);
          combinedModules.put(moduleDesc.clazz, combinedModule);
        } else {
          combinedModules.put(moduleDesc.clazz, moduleDesc);
        }
      }
    }

    // process override directives
    for (final SimpleModuleDesc moduleDesc : moduleDescs.values()) {

      final ConfigurationElement override = moduleDesc.desc.getChild(OVERRIDE);
      if (override != null) {
        final String overriddenClass = override.getAttribute(CLASS);
        final ModuleDesc overriddenModule = combinedModules
            .get(overriddenClass);
        if (overriddenModule == null) {
          Assert.fail("Unknown module class '" + overriddenClass + "'");
        } else {
          overriddenModule.overridingModules.add(moduleDesc);
        }
      }
    }

    final Set<Module> result = new HashSet<Module>(combinedModules.size());
    for (final ModuleDesc moduleDesc : combinedModules.values()) {
      if (moduleDesc.isPrimary) {
        result.add(moduleDesc.getModule());
      }
    }

    modules = result;
  }

  private static abstract class ModuleDesc {
    Module                 module;
    boolean                loadingModule;
    Collection<ModuleDesc> overridingModules = new ArrayList<ModuleDesc>();
    boolean                isPrimary         = true;

    Module getModule() {
      if (module == null) {
        if (loadingModule) {
          // cycle in module inheritance graph
          Assert.fail("Cycle in module inheritance graph.");
        }

        loadingModule = true;

        try {
          final Module localModule = createLocalModule();

          if (!overridingModules.isEmpty()) {
            final List<Module> modules = new ArrayList<Module>(
                overridingModules.size());
            for (final ModuleDesc desc : overridingModules) {
              modules.add(desc.getModule());
            }
            module = Modules.override(localModule).with(modules);

          } else {
            module = localModule;
          }

        } finally {
          loadingModule = false;
        }
      }
      return module;
    }

    abstract Module createLocalModule();
  }

  private static class SimpleModuleDesc extends ModuleDesc {
    final String               clazz;
    final ConfigurationElement desc;

    SimpleModuleDesc(final ConfigurationElement desc) {
      this.clazz = desc.getAttribute(CLASS);
      this.desc = desc;
      this.isPrimary = desc.getChild(OVERRIDE) == null;
    }

    @Override
    Module createLocalModule() {
      return desc.createInstance(CLASS, Module.class);
    };
  }

  private static class CombinedModuleDesc extends ModuleDesc {

    Collection<SimpleModuleDesc> combinedModules = new ArrayList<SimpleModuleDesc>();

    @Override
    Module createLocalModule() {
      final List<Module> modules = new ArrayList<Module>(combinedModules.size());
      for (final SimpleModuleDesc desc : combinedModules) {
        modules.add(desc.getModule());
      }
      return Modules.combine(modules);
    }

    void add(final SimpleModuleDesc moduleDesc) {
      combinedModules.add(moduleDesc);
      isPrimary &= moduleDesc.isPrimary;
    }

    void merge(final CombinedModuleDesc moduleDesc) {
      combinedModules.addAll(moduleDesc.combinedModules);
      isPrimary &= moduleDesc.isPrimary;
    }
  }
}
