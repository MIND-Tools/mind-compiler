package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;

/**
 * Base interface for plugin extension elements.
 */
public interface Extension extends Node {
	/**
	 * Sets the extension point corresponding to this extension node.
	 * 
	 * @param point
	 *          The qualified name of the extension point which corresponds to
	 *          this extension.
	 */
	void setPoint(String point);

	String getPoint();
}
