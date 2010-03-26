package org.ow2.mind.plugin;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.PluginLoader;
import org.ow2.mind.plugin.ast.Plugin;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BasicTest {
	XMLNodeFactory nodeFactory;
	PluginLoader pluginLoader;
	BasicPluginManager pluginManager;

	Map<Object, Object> context;

	@BeforeMethod(alwaysRun = true)
	public void setUpNodeFactory() {
		// Compilation context
		context = new HashMap<Object, Object>();

		// Components
		nodeFactory = new XMLNodeFactoryImpl();
		pluginLoader = new PluginLoader();
		pluginManager = new BasicPluginManager();

		// Bindings
		pluginLoader.nodeFactory = nodeFactory;
		pluginManager.pluginLoader = pluginLoader;
	}

	@Test
	public void test1() throws Exception {
		String[] pluginIds = { "org.ow2.mind.mindc", "com.st.p2012.memory" };

		for (String pluginId : pluginIds) {
			pluginManager.registerPlugin(pluginId, context);
		}
		Plugin[] plugins = pluginManager.getPlugins("org.ow2.mind.mindc.cpl",
				context);
		assert (plugins.length == 1);
		assert (plugins[0].getId().equals("com.st.p2012.memory"));
	}
}
