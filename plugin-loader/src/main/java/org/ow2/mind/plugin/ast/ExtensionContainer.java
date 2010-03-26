package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;

/**
 * Container interface for extension elements.
 */
public interface ExtensionContainer {
	void addExtension(Extension extension);
	void removeExtension(Extension extension);
	Extension[] getExtensions();
}
