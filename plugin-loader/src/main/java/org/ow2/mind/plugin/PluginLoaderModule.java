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

package org.ow2.mind.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Guice module for PluginLoader.
 */
public class PluginLoaderModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PluginManager.class).to(BasicPluginManager.class);
    bind(ClassLoader.class).annotatedWith(
        Names.named(BasicPluginManager.PLUGIN_CLASS_LOADER)).toInstance(
        this.getClass().getClassLoader());
  }
}
