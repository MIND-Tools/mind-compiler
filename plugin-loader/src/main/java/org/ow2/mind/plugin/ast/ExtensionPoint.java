package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;

/**
 * Base interface for the extension-point element.
 */
public interface ExtensionPoint extends Node{
	void setId(String id);
	
	String getId();
	
	void setDtd(String dtd);
	
	String getDtd();
}
