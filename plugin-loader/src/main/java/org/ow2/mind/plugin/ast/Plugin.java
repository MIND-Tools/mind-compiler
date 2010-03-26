package org.ow2.mind.plugin.ast;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;

/**
 * Base interface implemented by all plugin components.
 */
public interface Plugin extends ExtensionPointContainer, ExtensionContainer, Node{
	void setId(String id);
	
	String getId();
	
	void setName(String name);
	
	String getName();
}
