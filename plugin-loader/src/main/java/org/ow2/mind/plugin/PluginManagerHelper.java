package org.ow2.mind.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.mind.plugin.ast.Plugin;

public final class PluginManagerHelper {
	private PluginManagerHelper() {
	}

	public static final String PLUGIN_MANAGER_CONTEXT_MAP = "plugin-manager-context-map";

	/**
	 * Registers a new pluginMap to the context.
	 * 
	 * @param pluginMap
	 *          the plugin map.
	 * @param context
	 *          the context.
	 */
	public static void setContextMap(Map<String, List<Plugin>> pluginMap,
			Map<Object, Object> context) {
		context.put(PLUGIN_MANAGER_CONTEXT_MAP, pluginMap);
	}

	/**
	 * Returns the plugin map which is registered in the context. If there is no
	 * pluginMap already registered in the context, then a new one is created and
	 * registered before returning it.
	 * 
	 * @param context
	 *          the context.
	 * @return the plugin map. If there is no pluginMap already registered in the
	 *         context, then a new one is created and registered before returning
	 *         it.
	 */
	public static Map<String, List<Plugin>> getContextMap(Map<Object, Object> context) {
		@SuppressWarnings("unchecked")
		Map<String, List<Plugin>> pluginMap = (Map<String, List<Plugin>>) context
				.get(PLUGIN_MANAGER_CONTEXT_MAP);
		if (pluginMap == null) {
			pluginMap = new HashMap<String, List<Plugin>>();
			setContextMap(pluginMap, context);
		}
		return pluginMap;
	}
}
