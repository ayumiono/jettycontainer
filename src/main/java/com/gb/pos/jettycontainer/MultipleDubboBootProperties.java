package com.gb.pos.jettycontainer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 
 * 解决System.getProperty()在多环境下的问题
 * @author xuelong.chen
 *
 */
public class MultipleDubboBootProperties extends Properties{

	private static final long serialVersionUID = 7596776216977856200L;
	
	private Map<ClassLoader, Properties> pros;
	
	private Properties systemPros;
	
	@Override
	public Object get(Object key) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(pros.get(classLoader) == null) {
			Properties p = new Properties(null);
			for (Enumeration<?> e = systemPros.keys() ; e.hasMoreElements() ;) {
	            String k = (String)e.nextElement();
	            p.put(k, systemPros.get(k));
	        }
			pros.put(classLoader, p);
		}
		return this.pros.get(classLoader).get(key);
	}
	
	public MultipleDubboBootProperties() {
		this.pros = new HashMap<>();
		this.systemPros = System.getProperties();
		this.pros.put(this.getClass().getClassLoader(), System.getProperties());
	}
	
	@Override
	public String getProperty(String key) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(pros.get(classLoader) == null) {
			Properties p = new Properties(null);
			for (Enumeration<?> e = systemPros.keys() ; e.hasMoreElements() ;) {
	            String k = (String)e.nextElement();
	            p.put(k, systemPros.get(k));
	        }
			pros.put(classLoader, p);
		}
		return this.pros.get(classLoader).getProperty(key);
	}
	
	@Override
	public String getProperty(String key, String defaultValue) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(pros.get(classLoader) == null) {
			Properties p = new Properties(null);
			for (Enumeration<?> e = systemPros.keys() ; e.hasMoreElements() ;) {
	            String k = (String)e.nextElement();
	            p.put(k, systemPros.get(k));
	        }
			pros.put(classLoader, p);
		}
		String value = this.pros.get(classLoader).getProperty(key);
		return value == null ? defaultValue : value;
	}
	
	@Override
	public Object put(Object key, Object value) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(pros.get(classLoader) == null) {
			Properties p = new Properties(null);
			for (Enumeration<?> e = systemPros.keys() ; e.hasMoreElements() ;) {
	            String k = (String)e.nextElement();
	            p.put(k, systemPros.get(k));
	        }
			pros.put(classLoader, p);
		}
		return pros.get(classLoader).put(key, value);
	}
	
	
	@Override
	public synchronized String toString() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(pros.get(classLoader) == null) {
			Properties p = new Properties(null);
			for (Enumeration<?> e = systemPros.keys() ; e.hasMoreElements() ;) {
	            String k = (String)e.nextElement();
	            p.put(k, systemPros.get(k));
	        }
			pros.put(classLoader, p);
		}
		return this.pros.get(classLoader).toString();
	}
}
