package org.ow2.mind.plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.PluginLoader;
import org.ow2.mind.plugin.ast.Extension;
import org.ow2.mind.plugin.ast.ExtensionPoint;
import org.ow2.mind.plugin.ast.Plugin;

public class BasicPluginManager implements PluginManager {

	/**
	 * The name of the plugin-loader client interface.
	 */
	public static final String PLUGIN_LOADER_ITF = "plugin-loader";
	/**
	 * The plugin-loader client interface.
	 */
	public PluginLoader pluginLoader;

	public Plugin[] getPlugins(String extensionPoint, Map<Object, Object> context) {
		// TODO Auto-generated method stub
		List<Plugin> pluginList = (List<Plugin>) PluginManagerHelper.getContextMap(
				context).get(extensionPoint);
		return pluginList.toArray(new Plugin[0]);
	}

	/*
	 * FIXME: The current version doesn't use the context-based plugin management.
	 */
	public void registerPlugin(String pluginId, Map<Object, Object> context)
			throws ADLException {
		URL[] pluginUrls = new URL[1];
		try {
			pluginUrls[0] = new URL("file://"
					+ ClassLoader.getSystemResource(pluginId).getPath() + "/");
		} catch (MalformedURLException e) {
			throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
					"Unable to access the plugin '" + pluginId + "'.");
		}
		URL pluginUrl = URLClassLoader.newInstance(pluginUrls).getResource(
				"plugin.xml");

		Plugin plugin = pluginLoader.load(pluginUrl.getFile());

		Map<String, List<Plugin>> pluginMap = PluginManagerHelper
				.getContextMap(context);
		// Register new extension points.
		for (ExtensionPoint extensionPoint : plugin.getExtensionPoints()) {
			String extensionPointName = plugin.getId() + '.' + extensionPoint.getId();
			List<Plugin> extensionList = pluginMap.get(extensionPointName);
			// If this extension point is not already registered, create a new plugin
			// list.
			// Otherwise, nothing to do.
			if (extensionList == null) {
				extensionList = new ArrayList<Plugin>();
				pluginMap.put(extensionPointName, extensionList);
			}
		}

		for (Extension extension : plugin.getExtensions()) {
			List<Plugin> pluginList = pluginMap.get(extension.getPoint());
			// Check if there is already an extension point which is defined for this
			// extension.
			if (pluginList == null) {
				throw new CompilerError(GenericErrors.INTERNAL_ERROR,
						"No extension point registered for the extension '"
								+ extension.getPoint() + "' provided by the plugin '"
								+ pluginId + "'.");
			}
			// Add the plugin to the list.
			pluginList.add(plugin);
		}

		System.out.println(plugin.getName());
	}
}
