package org.ow2.mind;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.xml.XMLNode;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.plugin.ast.Extension;
import org.ow2.mind.plugin.ast.ExtensionPoint;
import org.ow2.mind.plugin.ast.Plugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
public class PluginLoader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static final String PLUGIN_DTD = "classpath://org/ow2/mind/plugin/plugin.dtd";

	/**
	 * Client interface for node factory.
	 */
	public XMLNodeFactory nodeFactory ;
	
	protected DocumentBuilder builder = null;
	public PluginLoader(){
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Plugin load(String file) throws ADLException{
		Document document = null;
		try {
			try {
				document = builder.parse(new FileInputStream(file));
			} catch (FileNotFoundException e) {
	      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
        "Unable to find the file '"+file+"'.");
			} catch (IOException e) {
	      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
	          "Unable to access the file '"+file+"'.");
			}
		} catch (SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to parse the XML file '"+file+"'.");
		}
		Element root = document.getDocumentElement();
		// Setting the base information
		Plugin plugin = (Plugin)newNode("plugin");
		plugin.setId(root.getAttribute("id"));
		plugin.setName(root.getAttribute("name"));
		
		// Setting the extension points
		NodeList nodes = root.getChildNodes();
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			if(node instanceof Element){
				Element element = (Element)node;
				if(element.getNodeName().equals("extension")){
					Extension extension = (Extension)newNode("extension");
					extension.setPoint(element.getAttribute("point"));
					extension.astSetDecoration("xml-element", element);
					plugin.addExtension(extension);
				}
				else if(element.getNodeName().equals("extension-point")){
					ExtensionPoint extensionPoint = (ExtensionPoint)newNode("extensionPoint");
					extensionPoint.setId(element.getAttribute("id"));
					extensionPoint.setDtd(element.getAttribute("dtd"));
					extensionPoint.astSetDecoration("xml-element", element);
					plugin.addExtensionPoint(extensionPoint);
				}
			}
		}
		return plugin;
	}
	
	protected XMLNode newNode(final String name) {
    XMLNode node;
    try {
      node = nodeFactory.newXMLNode(PLUGIN_DTD, name);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to create node");
    }
    return node;
  }

}


