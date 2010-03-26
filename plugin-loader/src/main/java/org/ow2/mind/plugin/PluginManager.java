package org.ow2.mind.plugin;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.plugin.ast.Plugin;

public interface PluginManager {

	/**
	 * Registers a plugin within the given registration context.
	 * @param The unique ID of the plugin to be registered. 
	 * @throws ADLException 
	 */
	public void registerPlugin(String pluginId, Map<Object,Object> context) throws ADLException;

	/**
	 * Returns the set of plugins that matches the fiven extension point.
	 * @param extensionPoint The extension point for matching the plugins
	 * @param context The registration context. 
	 */
	public Plugin[] getPlugins(String extensionPoint, Map<Object, Object> context);
}
