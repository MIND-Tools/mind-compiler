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
 * Authors: Ali Erdem Ozcan
 */

package org.ow2.mind.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.NodeFactoryImpl;
import org.ow2.mind.plugin.ast.Extension;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BasicTest {
	BasicPluginManager pluginManager;
	NodeFactory nodeFactory;

	Map<Object, Object> context;

	@BeforeMethod(alwaysRun = true)
	public void setUpNodeFactory() {
		// Compilation context
		context = new HashMap<Object, Object>();

		// Components
		pluginManager = new BasicPluginManager();
		nodeFactory = new NodeFactoryImpl(); 

		// Bindings
		pluginManager.nodeFactoryItf = nodeFactory;
	}

	@Test
	public void test1() throws Exception {

		Collection<Extension> extensions = pluginManager.getExtensions("org.ow2.mind.mindc.cpl", context);
		assert (extensions.size() == 1);
		for (Extension extension : extensions) {
			assert (extension.getPoint().equals("org.ow2.mind.mindc.cpl"));
		}
	}
}
