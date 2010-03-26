package org.ow2.mind;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class Loader {
	URLClassLoader cl ;
	public Loader(URL[] urls){
		cl = URLClassLoader.newInstance(urls, null);
		
	}
	
	public Enumeration<URL> getResources(String name) throws IOException{
		return cl.getResources(name);
	}

}
