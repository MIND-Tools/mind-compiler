package org.ow2.mind.plugin.ast;

/**
 * Base interface for extension-point element
 */
public interface ExtensionPointContainer {
	void addExtensionPoint(ExtensionPoint extensionPoint);
	void removeExtensionPoint(ExtensionPoint extensionPoint);
	ExtensionPoint[] getExtensionPoints();
}
